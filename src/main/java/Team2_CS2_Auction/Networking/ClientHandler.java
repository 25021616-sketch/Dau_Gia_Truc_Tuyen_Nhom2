package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.auction.AutoBid;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Repository.AutoBidRepository;
import Team2_CS2_Auction.Repository.UserRepository;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.Service.UserService;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionServer server;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = GsonUtil.getGson();
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionServiceImpl();
    
    private int loggedInUserId = -1;
    private final AutoBidRepository autoBidRepository = new AutoBidRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AuctionRepository auctionRepository = new AuctionRepositoryImpl();

    public ClientHandler(Socket socket, AuctionServer server) {
        this.socket = socket;
        this.server = server;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                System.out.println("Server nhận: " + jsonMessage);
                
                // Parse message to NetworkMessage
                NetworkMessage message = gson.fromJson(jsonMessage, NetworkMessage.class);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client mất kết nối: " + e.getMessage());
        } finally {
            closeConnection();
            server.removeClient(this);
        }
    }
    
    private void handleMessage(NetworkMessage message) {
        String action = message.getAction();
        
        switch (action) {
            case "LOGIN":
                try {
                    // Phân tích payload
                    JsonObject payload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String username = payload.get("username").getAsString();
                    String password = payload.get("password").getAsString();
                    boolean isAdminLogin = payload.has("isAdminLogin") && payload.get("isAdminLogin").getAsBoolean();

                    // Gọi logic từ UserService (truy xuất DB)
                    User user = userService.handleLoginLogic(username, password, isAdminLogin);
                    this.loggedInUserId = user.getId();
                    
                    // GỬI DTO THAY VÌ GỬI TRỰC TIẾP MODEL ĐỂ TRÁNH LỖI GSON
                    UserDTO dto = UserDTO.fromUser(user);
                    NetworkMessage response = new NetworkMessage("LOGIN_SUCCESS", gson.toJson(dto));
                    sendMessage(gson.toJson(response));
                    System.out.println("Đăng nhập thành công: " + username);
                } catch (Exception e) {
                    // Gửi lỗi về Client
                    NetworkMessage response = new NetworkMessage("LOGIN_FAILED", e.getMessage());
                    sendMessage(gson.toJson(response));
                    System.out.println("Đăng nhập thất bại: " + e.getMessage());
                }
                break;
            case "PLACE_BID":
                try {
                    JsonObject bidPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String auctionId = bidPayload.get("auctionId").getAsString();
                    double bidAmount = bidPayload.get("bidAmount").getAsDouble();
                    int userId = bidPayload.get("userId").getAsInt();

                    // Tạo dummy Member chỉ với ID để qua bài test của Service
                    Member dummyBidder = new Member(userId, "Dummy", "HIDDEN", "000");

                    // Gọi Database
                    auctionService.placeBid(dummyBidder, auctionId, bidAmount);

                    // THÀNH CÔNG: BROADCAST CHO MỌI NGƯỜI CÙNG BIẾT
                    JsonObject broadcastPayload = new JsonObject();
                    broadcastPayload.addProperty("auctionId", auctionId);
                    broadcastPayload.addProperty("newPrice", bidAmount);
                    broadcastPayload.addProperty("winnerId", userId);
                    
                    server.broadcast("NEW_BID", broadcastPayload);
                    System.out.println("Đã broadcast giá mới cho " + auctionId + ": $" + bidAmount);
                    
                    // Kích hoạt tiến trình thầu tự động trên luồng phụ
                    int prodId = Integer.parseInt(auctionId.replace("AUC_", ""));
                    new Thread(() -> triggerAutoBidsStart(prodId, userId, bidAmount)).start();

                } catch (Exception e) {
                    // THẤT BẠI: Chỉ gửi lỗi cho người đặt giá này
                    NetworkMessage errResponse = new NetworkMessage("BID_FAILED", e.getMessage());
                    sendMessage(gson.toJson(errResponse));
                    System.out.println("Đặt giá thất bại: " + e.getMessage());
                }
                break;
            case "GET_AUTO_BID_STATUS":
                try {
                    JsonObject statusPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String auctionId = statusPayload.get("auctionId").getAsString();
                    int userId = statusPayload.get("userId").getAsInt();
                    int prodId = Integer.parseInt(auctionId.replace("AUC_", ""));

                    AutoBid ab = autoBidRepository.getByUserAndProduct(userId, prodId);
                    if (ab != null && ab.isActive()) {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_STATUS_RESP", gson.toJson(ab))));
                    } else {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_STATUS_RESP", "")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "ACTIVATE_AUTO_BID":
                try {
                    JsonObject actPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String auctionId = actPayload.get("auctionId").getAsString();
                    int userId = actPayload.get("userId").getAsInt();
                    int stepMult = actPayload.get("stepMultiplier").getAsInt();
                    double maxLimit = actPayload.get("maxLimit").getAsDouble();
                    int prodId = Integer.parseInt(auctionId.replace("AUC_", ""));

                    AutoBid ab = new AutoBid(userId, prodId, stepMult, maxLimit, true);
                    boolean ok = autoBidRepository.saveOrUpdate(ab);
                    if (ok) {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_SUCCESS", gson.toJson(ab))));
                        System.out.println("[AUTO-BID] Đã bật Auto Bid cho User " + userId + " tại SP " + prodId);
                        
                        // Kiểm tra xem có tự động thầu ngay lập tức không (nếu người dùng chưa dẫn đầu)
                        int currentWinnerId = userRepository.getHighestBidderId("AUC_" + prodId);
                        new Thread(() -> triggerAutoBidsStart(prodId, currentWinnerId, -1.0)).start();
                    } else {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_FAILED", "Không thể lưu cấu hình đấu giá tự động!")));
                    }
                } catch (Exception e) {
                    sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_FAILED", "Lỗi: " + e.getMessage())));
                }
                break;
            case "DEACTIVATE_AUTO_BID":
                try {
                    JsonObject deactPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String auctionId = deactPayload.get("auctionId").getAsString();
                    int userId = deactPayload.get("userId").getAsInt();
                    int prodId = Integer.parseInt(auctionId.replace("AUC_", ""));

                    boolean ok = autoBidRepository.deactivate(userId, prodId);
                    if (ok) {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_CANCELLED", "Đã hủy kích hoạt đấu giá tự động.")));
                        System.out.println("[AUTO-BID] Đã hủy Auto Bid cho User " + userId + " tại SP " + prodId);
                    } else {
                        sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_FAILED", "Không thể tắt đấu giá tự động!")));
                    }
                } catch (Exception e) {
                    sendMessage(gson.toJson(new NetworkMessage("AUTO_BID_FAILED", "Lỗi: " + e.getMessage())));
                }
                break;
            case "TEST":
                System.out.println("Payload TEST nhận được: " + message.getPayload());
                // Phản hồi lại Client
                NetworkMessage response = new NetworkMessage("TEST_RESPONSE", "Server đã nhận được!");
                sendMessage(gson.toJson(response));
                break;
            default:
                System.out.println("Action không hợp lệ: " + action);
                break;
        }
    }



    private void triggerAutoBidsStart(int productId, int currentWinnerId, double currentPrice) {
        try {
            String auctionId = "AUC_" + productId;
            Auction currentAuction = auctionRepository.findById(auctionId);
            if (currentAuction == null) return;
            double stepPrice = currentAuction.getStepPrice();
            double actualCurrentPrice = (currentPrice < 0) ? currentAuction.getCurrentPrice() : currentPrice;
            
            triggerAutoBids(productId, currentWinnerId, actualCurrentPrice, stepPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void triggerAutoBids(int productId, int currentWinnerId, double currentPrice, double stepPrice) {
        try {
            String auctionId = "AUC_" + productId;

            // Lấy tất cả Auto Bid đang hoạt động cho sản phẩm này kèm balance (Chỉ tốn đúng 1 kết nối mạng)
            List<AutoBid> activeBids = autoBidRepository.getActiveAutoBidsByProduct(productId);

            for (AutoBid activeBid : activeBids) {
                // Bỏ qua nếu người thầu hiện tại đang dẫn đầu (không tự thầu đè lên chính mình)
                if (activeBid.getUserId() == currentWinnerId) {
                    continue;
                }

                // Tính toán giá thầu tự động mới
                double targetPrice = currentPrice + (activeBid.getStepMultiplier() * stepPrice);

                // Kiểm tra giới hạn tối đa (Không tốn kết nối mạng vì maxLimit có sẵn)
                if (targetPrice > activeBid.getMaxLimit()) {
                    // Tắt Auto Bid
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED", "Đấu giá tự động đã bị dừng do giá thầu mới $" + targetPrice + " vượt giới hạn tối đa $" + activeBid.getMaxLimit() + " của bạn!");
                    continue;
                }

                // Kiểm tra số dư khả dụng (balance - locked) vì balance trong AutoBid là tổng balance
                double balance = activeBid.getBalance();
                double lockedBalance = userRepository.getLockedBalance(activeBid.getUserId());
                double availableBalance = balance - lockedBalance;
                if (availableBalance < targetPrice) {
                    // Tắt Auto Bid
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED", 
                        "[Đấu giá tự động đã dừng] Số dư khả dụng (" + availableBalance + ") không đủ để thầu mức " + targetPrice);
                    continue;
                }

                // Hợp lệ -> Tiến hành đặt thầu tự động!
                Member activeBidder = new Member(activeBid.getUserId(), "User_" + activeBid.getUserId(), "123456", "000");
                try {
                    auctionService.placeBid(activeBidder, auctionId, targetPrice);

                    // Broadcast giá mới cho toàn mạng
                    JsonObject broadcastPayload = new JsonObject();
                    broadcastPayload.addProperty("auctionId", auctionId);
                    broadcastPayload.addProperty("newPrice", targetPrice);
                    broadcastPayload.addProperty("winnerId", activeBid.getUserId());
                    
                    server.broadcast("NEW_BID", broadcastPayload);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_PLACED", String.valueOf(targetPrice));
                    System.out.println("[AUTO-BID] Tự động thầu thành công cho User ID: " + activeBid.getUserId() + " tại giá $" + targetPrice);

                    // Đệ quy kích hoạt tiếp với giá mới (Hoàn toàn không tốn kết nối findById!)
                    triggerAutoBids(productId, activeBid.getUserId(), targetPrice, stepPrice);
                    return; // Dừng vòng lặp này vì luồng đệ quy sẽ xử lý tiếp các thầu sau
                } catch (Exception ex) {
                    System.err.println("[AUTO-BID] Đặt thầu tự động lỗi: " + ex.getMessage());
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED", "Đấu giá tự động đã dừng do lỗi: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String jsonMsg) {
        if (out != null) {
            out.println(jsonMsg);
        }
    }

    public void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
