package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkManager {
    private static NetworkManager instance;
    private final HttpClient httpClient;
    private WebSocket webSocket;
    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();
    private final Gson gson = GsonUtil.getGson();

    private String currentHost;
    private int currentPort;

    private NetworkManager() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void addListener(NetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return webSocket != null && !webSocket.isOutputClosed() && !webSocket.isInputClosed();
    }

    public UserDTO login(String host, int port, String username, String password, boolean isAdminLogin) throws Exception {
        this.currentHost = host;
        this.currentPort = port;

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("isAdminLogin", isAdminLogin);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host + ":" + port + "/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), UserDTO.class);
        } else {
            throw new Exception(response.body());
        }
    }

    public void connect(String host, int port) {
        this.currentHost = host;
        this.currentPort = port;
        try {
            URI wsUri = URI.create("ws://" + host + ":" + port + "/ws/auction");
            this.webSocket = httpClient.newWebSocketBuilder()
                    .buildAsync(wsUri, new WebSocket.Listener() {
                        StringBuilder textBuilder = new StringBuilder();

                        @Override
                        public void onOpen(WebSocket webSocket) {
                            System.out.println("Đã kết nối WebSocket tới: " + wsUri);
                            WebSocket.Listener.super.onOpen(webSocket);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            textBuilder.append(data);
                            if (last) {
                                String jsonMessage = textBuilder.toString();
                                textBuilder.setLength(0); // Clear builder
                                handleIncomingMessage(jsonMessage);
                            }
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            System.err.println("Lỗi WebSocket: " + error.getMessage());
                            notifyConnectionError();
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            System.out.println("WebSocket đóng kết nối: " + reason);
                            notifyConnectionError();
                            return null;
                        }
                    }).join();
        } catch (Exception e) {
            System.err.println("Không thể kết nối WebSocket: " + e.getMessage());
            notifyConnectionError();
        }
    }

    private void handleIncomingMessage(String jsonMessage) {
        try {
            NetworkMessage message = gson.fromJson(jsonMessage, NetworkMessage.class);
            for (NetworkListener listener : listeners) {
                listener.onMessageReceived(message);
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON từ Server: " + e.getMessage());
        }
    }

    public void send(String action, Object payloadObj) {
        if (webSocket != null) {
            String payloadJson = gson.toJson(payloadObj);
            NetworkMessage msg = new NetworkMessage(action, payloadJson);
            String jsonMsg = gson.toJson(msg);
            webSocket.sendText(jsonMsg, true);
        } else {
            System.err.println("Chưa kết nối WebSocket. Không thể gửi tin nhắn.");
        }
    }

    private void notifyConnectionError() {
        for (NetworkListener listener : listeners) {
            listener.onConnectionError();
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client đóng ứng dụng");
            System.out.println("Đã ngắt kết nối WebSocket.");
        }
    }
}
