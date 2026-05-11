package Team2_CS2_Auction.Networking;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Biến này lưu trữ Controller nào đang "nghe" dữ liệu ở thời điểm hiện tại
    private Consumer<String> messageListener;

    // Singleton: Đảm bảo chỉ có 1 trạm mạng duy nhất
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // Gọi hàm này ở hàm start() của Main.java
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Luồng chạy ngầm liên tục nghe ngóng Server
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Nhận từ Server: " + serverMessage);
                        // Nếu có Controller nào đang đăng ký nghe, thì ném tin nhắn cho nó
                        if (messageListener != null) {
                            final String msg = serverMessage;
                            // Bắt buộc dùng Platform.runLater để an toàn cho JavaFX
                            Platform.runLater(() -> messageListener.accept(msg));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Mất kết nối tới Server.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
        } catch (IOException e) {
            System.err.println("Không thể kết nối! Hãy chắc chắn Server đang chạy.");
        }
    }

    // Các Controller gọi hàm này để gửi tin nhắn đi (Ví dụ gửi chuỗi JSON)
    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }

    // Các Controller gọi hàm này để nhận tin nhắn về
    public void setListener(Consumer<String> listener) {
        this.messageListener = listener;
    }
}