package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.auction.AutoBid;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Repository.AutoBidRepository;
import Team2_CS2_Auction.Repository.UserRepository;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import io.javalin.websocket.WsContext;
import java.util.List;

public class ClientHandler {
    private final WsContext ctx;
    private final JavalinServer server;
    private final Gson gson = GsonUtil.getGson();
    private final AuctionService auctionService = new AuctionServiceImpl();
    
    private int loggedInUserId = -1;
    private final AutoBidRepository autoBidRepository = new AutoBidRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AuctionRepository auctionRepository = new AuctionRepositoryImpl();

    public ClientHandler(WsContext ctx, JavalinServer server) {
        this.ctx = ctx;
        this.server = server;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    /** Xử lý tin nhắn WebSocket đến từ client */
    public void handleIncomingMessage(String jsonMessage) {
        try {
            NetworkMessage message = gson.fromJson(jsonMessage, NetworkMessage.class);
            handleMessage(message);
        } catch (Exception e) {
            System.err.println("Lỗi parse tin nhắn WebSocket: " + e.getMessage());
        }
    }
    
    private void handleMessage(NetworkMessage message) {
        String action = message.getAction();
        
        switch (action) {
            case "LOGIN_WEBSOCKET":
                // Gắn session WebSocket này với userId sau khi REST login thành công
                try {
                    JsonObject payload = gson.fromJson(message.getPayload(), JsonObject.class);
                    this.loggedInUserId = payload.get("userId").getAsInt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
                
            case "PLACE_BID":
                try {
                    JsonObject bidPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String auctionId = bidPayload.get("auctionId").getAsString();
                    double bidAmount = bidPayload.get("bidAmount").getAsDouble();
                    int userId = bidPayload.get("userId").getAsInt();

                    Member dummyBidder = new Member(userId, "Dummy", "HIDDEN", "000");
                    java.time.LocalDateTime newEndTime = auctionService.placeBid(dummyBidder, auctionId, bidAmount);

                    JsonObject broadcastPayload = new JsonObject();
                    broadcastPayload.addProperty("auctionId", auctionId);
                    broadcastPayload.addProperty("newPrice", bidAmount);
                    broadcastPayload.addProperty("winnerId", userId);
                    if (newEndTime != null) {
                        broadcastPayload.addProperty("newEndTime", newEndTime.toString());
                    }

                    server.broadcast("NEW_BID", broadcastPayload);
                    sendBalanceUpdated(userId);

                    int prodId = Integer.parseInt(auctionId.replace("AUC_", ""));
                    new Thread(() -> triggerAutoBidsStart(prodId, userId, bidAmount)).start();

                } catch (Exception e) {
                    NetworkMessage errResponse = new NetworkMessage("BID_FAILED", e.getMessage());
                    sendMessage(gson.toJson(errResponse));
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
            case "CANCEL_AUCTION":
                try {
                    JsonObject cancelPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String cancelAuctionId = cancelPayload.get("auctionId").getAsString();
                    int cancelSellerId = cancelPayload.get("sellerId").getAsInt();

                    List<Integer> bidders = auctionRepository.getDistinctBidderIds(cancelAuctionId);
                    auctionService.cancelAuction(cancelAuctionId);

                    String productName = cancelPayload.has("productName") ? cancelPayload.get("productName").getAsString() : "Không rõ";
                    String type = (cancelPayload.has("relist") && cancelPayload.get("relist").getAsBoolean()) ? "RELISTED" : "DELETED";

                    JsonObject notif = new JsonObject();
                    notif.addProperty("auctionId", cancelAuctionId);
                    notif.addProperty("type", type);
                    notif.addProperty("productName", productName);
                    for (int bidderId : bidders) {
                        if (bidderId != cancelSellerId) {
                            server.sendToUser(bidderId, "PRODUCT_DELETED", notif);
                        }
                    }

                    server.broadcast("PRODUCT_UPDATED", "");
                    sendMessage(gson.toJson(new NetworkMessage("CANCEL_SUCCESS", cancelAuctionId)));
                } catch (Exception e) {
                    sendMessage(gson.toJson(new NetworkMessage("CANCEL_FAILED", "Lỗi: " + e.getMessage())));
                }
                break;

            case "RELIST_AUCTION":
                try {
                    JsonObject relistPayload = gson.fromJson(message.getPayload(), JsonObject.class);
                    String oldAuctionId = relistPayload.get("auctionId").getAsString();
                    int relistSellerId = relistPayload.get("sellerId").getAsInt();

                    List<Integer> oldBidders = auctionRepository.getDistinctBidderIds(oldAuctionId);
                    Auction oldAuction = auctionService.getAuctionById(oldAuctionId);
                    if (oldAuction == null) throw new Exception("Không tìm thấy phiên đấu giá!");

                    auctionService.cancelAuction(oldAuctionId);

                    String newStartStr = relistPayload.get("startTime").getAsString();
                    String newEndStr   = relistPayload.get("endTime").getAsString();
                    java.time.LocalDateTime newStart = java.time.LocalDateTime.parse(newStartStr);
                    java.time.LocalDateTime newEnd   = java.time.LocalDateTime.parse(newEndStr);

                    Team2_CS2_Auction.Model.user.Member seller = new Member(relistSellerId, "Seller_" + relistSellerId, "HIDDEN", "000");
                    auctionService.createAuction(
                        seller,
                        oldAuction.getItem().getTenSanPham(),
                        oldAuction.getItem().getLoaiSanPham(),
                        oldAuction.getItem().getMoTa(),
                        oldAuction.getItem().getImagePath(),
                        String.valueOf((long) oldAuction.getCurrentPrice()),
                        String.valueOf((long) oldAuction.getStepPrice()),
                        newStart,
                        newEnd
                    );

                    JsonObject notifRelist = new JsonObject();
                    notifRelist.addProperty("auctionId", oldAuctionId);
                    notifRelist.addProperty("type", "RELISTED");
                    notifRelist.addProperty("productName", oldAuction.getItem().getTenSanPham());
                    for (int bidderId : oldBidders) {
                        if (bidderId != relistSellerId) {
                            server.sendToUser(bidderId, "PRODUCT_DELETED", notifRelist);
                        }
                    }

                    server.broadcast("PRODUCT_UPDATED", "");
                    sendMessage(gson.toJson(new NetworkMessage("RELIST_SUCCESS", "Đã đăng lại thành công!")));
                } catch (Exception e) {
                    sendMessage(gson.toJson(new NetworkMessage("RELIST_FAILED", "Lỗi: " + e.getMessage())));
                }
                break;

            case "PRODUCT_UPDATED":
                server.broadcast("PRODUCT_UPDATED", "");
                break;
            case "TEST":
                System.out.println("Payload TEST nhận được: " + message.getPayload());
                // Phản hồi lại Client
                NetworkMessage response = new NetworkMessage("TEST_RESPONSE", "Server đã nhận được!");
                sendMessage(gson.toJson(response));
                break;
            default:
                System.out.println("Action không hợp lệ trên WebSocket: " + action);
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
        // Sử dụng vòng lặp thay vì đệ quy để tránh tạo vô số Thread và cạn kiệt pool kết nối DB.
        // Giới hạn tối đa 50 vòng để ngăn vòng thầu vô tận khi 2 người cùng auto-bid nhau.
        final int MAX_ROUNDS = 50;
        int round = 0;
        int winnerId = currentWinnerId;
        double price = currentPrice;

        while (round < MAX_ROUNDS) {
            round++;
            String auctionId = "AUC_" + productId;

            List<AutoBid> activeBids;
            try {
                activeBids = autoBidRepository.getActiveAutoBidsByProduct(productId);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            boolean anyBidPlaced = false;

            for (AutoBid activeBid : activeBids) {
                if (activeBid.getUserId() == winnerId) continue;

                double targetPrice = price + (activeBid.getStepMultiplier() * stepPrice);

                if (targetPrice > activeBid.getMaxLimit()) {
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED",
                        "Đấu giá tự động đã bị dừng do giá $" + targetPrice + " vượt giới hạn $" + activeBid.getMaxLimit());
                    continue;
                }

                double balance = activeBid.getBalance();
                double lockedBalance = userRepository.getLockedBalance(activeBid.getUserId());
                double availableBalance = balance - lockedBalance;
                if (availableBalance < targetPrice) {
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED",
                        "[Auto-Bid dừng] Số dư khả dụng (" + availableBalance + ") không đủ để thầu mức " + targetPrice);
                    continue;
                }

                Member activeBidder = new Member(activeBid.getUserId(), "User_" + activeBid.getUserId(), "123456", "000");
                try {
                    java.time.LocalDateTime newEndTime = auctionService.placeBid(activeBidder, auctionId, targetPrice);

                    JsonObject broadcastPayload = new JsonObject();
                    broadcastPayload.addProperty("auctionId", auctionId);
                    broadcastPayload.addProperty("newPrice", targetPrice);
                    broadcastPayload.addProperty("winnerId", activeBid.getUserId());
                    if (newEndTime != null) {
                        broadcastPayload.addProperty("newEndTime", newEndTime.toString());
                    }
                    server.broadcast("NEW_BID", broadcastPayload);

                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_PLACED", String.valueOf(targetPrice));
                    sendBalanceUpdatedToUser(activeBid.getUserId());

                    winnerId = activeBid.getUserId();
                    price = targetPrice;
                    anyBidPlaced = true;
                    break; // Chỉ xử lý 1 auto-bid mỗi vòng, vòng kế tiếp xử lý tiếp

                } catch (Exception ex) {
                    System.err.println("[AUTO-BID] Lỗi đặt thầu: " + ex.getMessage());
                    autoBidRepository.deactivate(activeBid.getUserId(), productId);
                    server.sendToUser(activeBid.getUserId(), "AUTO_BID_CANCELLED",
                        "Auto-Bid dừng do lỗi: " + ex.getMessage());
                }
            }

            if (!anyBidPlaced) break;
        }

        if (round >= MAX_ROUNDS) {
            System.out.println("[AUTO-BID] Đã đạt giới hạn " + MAX_ROUNDS + " vòng tự động. Dừng để bảo vệ hệ thống.");
        }
    }


    /** Lấy số dư mới nhất từ DB và gửi BALANCE_UPDATED cho chính user của session này */
    private void sendBalanceUpdated(int userId) {
        new Thread(() -> sendBalanceUpdatedToUser(userId)).start();
    }

    /** Lấy số dư từ DB và gửi BALANCE_UPDATED cho một user cụ thể qua WebSocket */
    private void sendBalanceUpdatedToUser(int targetUserId) {
        try {
            double newBalance = userRepository.getBalance(targetUserId);
            JsonObject balancePayload = new JsonObject();
            balancePayload.addProperty("userId", targetUserId);
            balancePayload.addProperty("balance", newBalance);
            server.sendToUser(targetUserId, "BALANCE_UPDATED", balancePayload);
            System.out.println("[BALANCE] Đã gửi số dư cập nhật cho User " + targetUserId + ": $" + newBalance);
        } catch (Exception e) {
            System.err.println("[BALANCE] Lỗi cập nhật số dư cho User " + targetUserId + ": " + e.getMessage());
        }
    }

    public void sendMessage(String jsonMsg) {
        if (ctx.session.isOpen()) {
            ctx.send(jsonMsg);
        }
    }
}
