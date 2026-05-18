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
            System.out.println("Server Đấu Giá đang chạy tại cổng " + port + "...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Phát hiện kết nối mới từ: " + clientIp);

                // Chạy một luồng riêng để hỏi cấp phép (không làm đơ Server)
                new Thread(() -> {
                    // Nếu là 127.0.0.1 (chính máy Server) thì tự động cho phép, đỡ phải hỏi nhiều
                    boolean isLocal = clientIp.equals("127.0.0.1") || clientIp.equals("localhost");
                    int dialogResult = javax.swing.JOptionPane.YES_OPTION;
                    
                    if (!isLocal) {
                        // Hiển thị Popup Swing tại màn hình Server
                        dialogResult = javax.swing.JOptionPane.showConfirmDialog(
                                null,
                                "Máy tính có IP: " + clientIp + " đang muốn kết nối vào hệ thống Đấu Giá.\nBạn có cho phép không?",
                                "Yêu cầu kết nối từ Client",
                                javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE
                        );
                    }

                    if (dialogResult == javax.swing.JOptionPane.YES_OPTION) {
                        System.out.println("-> Đã CẤP PHÉP cho IP: " + clientIp);
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        synchronized (clients) {
                            clients.add(handler);
                        }
                        
                        Thread clientThread = new Thread(handler);
                        clientThread.start();
                    } else {
                        System.out.println("-> TỪ CHỐI kết nối từ IP: " + clientIp);
                        try {
                            clientSocket.close();
                        } catch (IOException e) { }
                    }
                }).start();
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
            System.out.println("Đã đóng kết nối với một Client. Tổng số Client hiện tại: " + clients.size());
        }
    }
}
