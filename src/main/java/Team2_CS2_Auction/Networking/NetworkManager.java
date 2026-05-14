package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    
    private final List<NetworkListener> listeners = new ArrayList<>();
    private final Gson gson = GsonUtil.getGson();

    private NetworkManager() {}

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
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Đã kết nối tới Server: " + host + ":" + port);

            // Start listening thread
            listenerThread = new Thread(this::listenToServer);
            listenerThread.setDaemon(true); // Terminate when main app closes
            listenerThread.start();

        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
            notifyConnectionError();
        }
    }

    private void listenToServer() {
        try {
            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                // Parse JSON to NetworkMessage
                NetworkMessage message = gson.fromJson(jsonMessage, NetworkMessage.class);
                notifyListeners(message);
            }
        } catch (IOException e) {
            System.out.println("Kết nối tới Server bị đóng hoặc có lỗi: " + e.getMessage());
            notifyConnectionError();
        } finally {
            disconnect();
        }
    }

    public void send(String action, Object payloadObj) {
        if (out != null) {
            String payloadJson = gson.toJson(payloadObj);
            NetworkMessage msg = new NetworkMessage(action, payloadJson);
            String jsonMsg = gson.toJson(msg);
            out.println(jsonMsg);
        } else {
            System.err.println("Chưa kết nối tới server. Không thể gửi tin nhắn.");
        }
    }

    private void notifyListeners(NetworkMessage message) {
        // We might want to run this on JavaFX Application Thread depending on use case,
        // but let's notify directly. JavaFX controllers should use Platform.runLater
        for (NetworkListener listener : new ArrayList<>(listeners)) {
            listener.onMessageReceived(message);
        }
    }

    private void notifyConnectionError() {
        for (NetworkListener listener : new ArrayList<>(listeners)) {
            listener.onConnectionError();
        }
    }

    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Đã ngắt kết nối khỏi Server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
