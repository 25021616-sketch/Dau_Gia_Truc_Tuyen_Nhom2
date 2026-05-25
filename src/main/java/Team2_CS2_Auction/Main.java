package Team2_CS2_Auction;

import Team2_CS2_Auction.Networking.NetworkManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

    // =========================================================
    // Lưu thông tin server để các Controller có thể dùng lại
    // =========================================================
    private static String lastServerHost = "127.0.0.1";
    private static int lastServerPort = 8080;

    public static String getLastServerHost() { return lastServerHost; }
    public static int getLastServerPort()    { return lastServerPort;  }

    @Override
    public void start(Stage stage) {

        // =========================================================
        // Hiển thị màn hình loading ngay lập tức (không bị treo)
        // =========================================================
        Label statusLabel = new Label("🔍 Đang tìm kiếm máy chủ trong mạng LAN...");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #20335e; -fx-font-family: 'Segoe UI';");
        VBox loadingPane = new VBox(statusLabel);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: #f0f4ff;");
        stage.setScene(new Scene(loadingPane, 500, 200));
        stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - Đang khởi động...");
        stage.show();

        // =========================================================
        // Chạy UDP Discovery trên background thread (không block UI)
        // =========================================================
        new Thread(() -> {
            System.out.println("[Discovery] Đang tìm kiếm Server tự động trong mạng LAN...");
            String discoveredIp = Team2_CS2_Auction.Networking.DiscoveryClient.discoverServerIp();
            String input;

            if (discoveredIp != null && !discoveredIp.trim().isEmpty()) {
                System.out.println("[Discovery] Tìm thấy Server tại IP: " + discoveredIp + "! Đang tự động kết nối...");
                input = discoveredIp;
                Platform.runLater(() ->
                    statusLabel.setText("✅ Tìm thấy máy chủ: " + discoveredIp + " — Đang kết nối..."));
            } else {
                System.out.println("[Discovery] Không tìm thấy Server tự động. Hiển thị hộp thoại nhập thủ công.");
                // Phải hiện dialog trên JavaFX thread
                final String[] inputHolder = {null};
                Platform.runLater(() -> {
                    TextInputDialog dialog = new TextInputDialog("127.0.0.1");
                    dialog.setTitle("Kết nối đến Máy Chủ");
                    dialog.setHeaderText("Hệ Thống Đấu Giá Trực Tuyến");
                    dialog.setContentText("Không tìm thấy Server tự động.\nNhập IP hoặc địa chỉ Server\n(VD: 192.168.1.5 hoặc 0.tcp.ngrok.io:12345):");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        stage.close();
                        return;
                    }
                    inputHolder[0] = result.get().trim();
                    if (inputHolder[0].isEmpty()) inputHolder[0] = "127.0.0.1";
                    connectAndLoadUI(stage, inputHolder[0]);
                });
                return; // Background thread kết thúc, Platform.runLater xử lý tiếp
            }

            final String finalInput = input;
            Platform.runLater(() -> connectAndLoadUI(stage, finalInput));

        }).start();
    }

    /** Kết nối tới server và tải giao diện đăng nhập */
    private void connectAndLoadUI(Stage stage, String input) {
        // Tách host và port nếu người dùng nhập dạng host:port
        if (input.contains(":")) {
            String[] parts = input.split(":");
            lastServerHost = parts[0].trim();
            try { lastServerPort = Integer.parseInt(parts[1].trim()); } catch (Exception ignored) {}
        } else {
            lastServerHost = input;
            lastServerPort = 8080;
        }

        final String host = lastServerHost;
        final int port = lastServerPort;

        // Kết nối TCP trên background thread để không treo UI
        new Thread(() -> {
            System.out.println("Đang kết nối tới: " + host + ":" + port + " ...");
            NetworkManager.getInstance().connect(host, port);

            // Sau khi kết nối xong, load FXML trên JavaFX thread
            Platform.runLater(() -> {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_nhap.fxml"));
                    Parent root = fxmlLoader.load();

                    Scene scene = new Scene(root);
                    stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - ĐHQGHN");
                    stage.setMaximized(true);
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    System.err.println("Lỗi: Không tìm thấy file FXML!");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    @Override
    public void stop() {
        NetworkManager.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}