package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.UserService;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionServer server;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = GsonUtil.getGson();
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionServiceImpl();

    public ClientHandler(Socket socket, AuctionServer server) {
        this.socket = socket;
        this.server = server;
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
                    
                } catch (Exception e) {
                    // THẤT BẠI: Chỉ gửi lỗi cho người đặt giá này
                    NetworkMessage errResponse = new NetworkMessage("BID_FAILED", e.getMessage());
                    sendMessage(gson.toJson(errResponse));
                    System.out.println("Đặt giá thất bại: " + e.getMessage());
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
