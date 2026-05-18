package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    private ServerSocket serverSocket;
    private boolean isRunning;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Gson gson = GsonUtil.getGson();

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("=================================================");
            System.out.println("  SERVER ĐẤU GIÁ ĐÃ KHỞI ĐỘNG - CỔNG " + port);
            System.out.println("  Đang chờ Client kết nối...");
            System.out.println("=================================================");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();

                // Tự động chấp nhận, in log ra Terminal
                System.out.println("[+] Client kết nối: " + clientIp
                        + " | Tổng: " + (clients.size() + 1));

                ClientHandler handler = new ClientHandler(clientSocket, this);
                synchronized (clients) {
                    clients.add(handler);
                }
                new Thread(handler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Lỗi Server: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            synchronized (clients) {
                for (ClientHandler handler : clients) {
                    handler.closeConnection();
                }
                clients.clear();
            }
            System.out.println("Server đã dừng.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(NetworkMessage message) {
        String jsonMsg = gson.toJson(message);
        synchronized (clients) {
            for (ClientHandler handler : clients) {
                handler.sendMessage(jsonMsg);
            }
        }
    }

    public void broadcast(String action, Object payloadObj) {
        String payloadJson = gson.toJson(payloadObj);
        NetworkMessage msg = new NetworkMessage(action, payloadJson);
        broadcast(msg);
    }

    public void removeClient(ClientHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
            System.out.println("[-] Client ngắt kết nối. Còn lại: " + clients.size() + " client(s).");
        }
    }
}
