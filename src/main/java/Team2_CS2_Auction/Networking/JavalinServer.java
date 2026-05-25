package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavalinServer {
    private Javalin app;
    // Map Context to ClientHandler to maintain state (like loggedInUserId) per connection
    private final Map<WsContext, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Gson gson = GsonUtil.getGson();
    private final UserService userService = new UserService();

    public void start(int port) {
        app = Javalin.create(config -> {
            // Cấu hình CORS nếu sau này có Web Frontend
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(port);

        System.out.println("=================================================");
        System.out.println("  SERVER ĐẤU GIÁ (JAVALIN) ĐÃ KHỞI ĐỘNG - CỔNG " + port);
        System.out.println("  API & WebSocket đang chờ kết nối...");
        System.out.println("=================================================");

        // ==========================================
        // 1. REST API: Đăng nhập
        // ==========================================
        app.post("/api/login", ctx -> {
            try {
                String body = ctx.body();
                JsonObject payload = gson.fromJson(body, JsonObject.class);
                String username = payload.get("username").getAsString();
                String password = payload.get("password").getAsString();
                boolean isAdminLogin = payload.has("isAdminLogin") && payload.get("isAdminLogin").getAsBoolean();

                User user = userService.handleLoginLogic(username, password, isAdminLogin);
                UserDTO dto = UserDTO.fromUser(user);
                
                // Trả về JSON chuẩn của REST
                ctx.status(200).json(dto);
                System.out.println("REST: Đăng nhập thành công -> " + username);
            } catch (Exception e) {
                // Trả về mã lỗi HTTP 401 Unauthorized kèm message
                ctx.status(401).result(e.getMessage());
                System.out.println("REST: Đăng nhập thất bại -> " + e.getMessage());
            }
        });

        // ==========================================
        // 2. WebSocket: Real-time Bidding & Events
        // ==========================================
        app.ws("/ws/auction", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("[+] WebSocket Client kết nối: " + ctx.session.getRemoteAddress());
                ClientHandler handler = new ClientHandler(ctx, this);
                clients.put(ctx, handler);
            });

            ws.onMessage(ctx -> {
                String message = ctx.message();
                ClientHandler handler = clients.get(ctx);
                if (handler != null) {
                    handler.handleIncomingMessage(message);
                }
            });

            ws.onClose(ctx -> {
                System.out.println("[-] WebSocket Client ngắt kết nối: " + ctx.session.getRemoteAddress());
                clients.remove(ctx);
            });

            ws.onError(ctx -> {
                System.out.println("[!] WebSocket Client lỗi: " + ctx.error());
                clients.remove(ctx);
            });
        });
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    /** Broadcast tin nhắn cho tất cả client đang kết nối WebSocket */
    public void broadcast(NetworkMessage message) {
        String jsonMsg = gson.toJson(message);
        for (WsContext ctx : clients.keySet()) {
            if (ctx.session.isOpen()) {
                ctx.send(jsonMsg);
            }
        }
    }

    public void broadcast(String action, Object payloadObj) {
        String payloadJson = gson.toJson(payloadObj);
        NetworkMessage msg = new NetworkMessage(action, payloadJson);
        broadcast(msg);
    }

    /** Gửi tin nhắn cho một user cụ thể đang kết nối WebSocket */
    public void sendToUser(int userId, String action, Object payloadObj) {
        String payloadJson = gson.toJson(payloadObj);
        NetworkMessage msg = new NetworkMessage(action, payloadJson);
        String jsonMsg = gson.toJson(msg);
        
        for (ClientHandler handler : clients.values()) {
            if (handler.getLoggedInUserId() == userId) {
                handler.sendMessage(jsonMsg);
            }
        }
    }
}
