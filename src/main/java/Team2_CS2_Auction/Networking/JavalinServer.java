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
    private final Map<WsContext, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Gson gson = GsonUtil.getGson();
    private final UserService userService = new UserService();

    public void start(int port) {
        app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(port);

        System.out.println("=================================================");
        System.out.println("  SERVER ĐẤU GIÁ (JAVALIN) ĐÃ KHỞI ĐỘNG - CỔNG " + port);
        System.out.println("  API & WebSocket đang chờ kết nối...");
        System.out.println("=================================================");

        app.post("/api/login", ctx -> {
            try {
                String body = ctx.body();
                JsonObject payload = gson.fromJson(body, JsonObject.class);
                String username = payload.get("username").getAsString();
                String password = payload.get("password").getAsString();
                boolean isAdminLogin = payload.has("isAdminLogin") && payload.get("isAdminLogin").getAsBoolean();

                User user = userService.handleLoginLogic(username, password, isAdminLogin);
                UserDTO dto = UserDTO.fromUser(user);
                
                ctx.status(200).contentType("application/json").result(gson.toJson(dto));
                System.out.println("REST: Đăng nhập thành công -> " + username);
            } catch (Exception e) {
                ctx.status(401).result(e.getMessage());
                System.out.println("REST: Đăng nhập thất bại -> " + e.getMessage());
            }
        });

        app.ws("/ws/auction", ws -> {
            ws.onConnect(ctx -> {
                ctx.session.setIdleTimeout(java.time.Duration.ofHours(24));
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
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(jsonMsg);
                }
            } catch (Exception e) {
                // 1 client bị lỗi/ngắt không được làm ảnh hưởng các client còn lại
                System.err.println("[Broadcast] Lỗi gửi tới client: " + e.getMessage());
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
                try {
                    handler.sendMessage(jsonMsg);
                } catch (Exception e) {
                    System.err.println("[SendToUser] Lỗi gửi tới userId=" + userId + ": " + e.getMessage());
                }
            }
        }
    }
}
