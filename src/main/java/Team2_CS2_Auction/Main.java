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
        Label statusLabel = new Label("🔍 Đang tìm kiếm máy chủ trong mạng LAN...");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #20335e; -fx-font-family: 'Segoe UI';");
        VBox loadingPane = new VBox(statusLabel);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: #f0f4ff;");
        stage.setScene(new Scene(loadingPane, 500, 200));
        stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - Đang khởi động...");
        stage.show();

        // Tự động kết nối: ưu tiên DB, sau đó UDP broadcast, cuối cùng mới yêu cầu nhập tay
        new Thread(() -> {
            String serverAddress = readServerAddressFromDb();
            if (serverAddress != null) {
                final String addr1 = serverAddress;
                Platform.runLater(() -> {
                    statusLabel.setText("✅ Tìm thấy máy chủ: " + addr1 + " — Đang kết nối...");
                    connectAndLoadUI(stage, addr1);
                });
                return;
            }

            String discoveredIp = Team2_CS2_Auction.Networking.DiscoveryClient.discoverServerIp();
            if (discoveredIp != null && !discoveredIp.trim().isEmpty()) {
                final String addr2 = discoveredIp;
                Platform.runLater(() -> {
                    statusLabel.setText("✅ Tìm thấy máy chủ: " + addr2 + " — Đang kết nối...");
                    connectAndLoadUI(stage, addr2);
                });
                return;
            }

            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog("127.0.0.1");
                dialog.setTitle("Kết nối đến Máy Chủ");
                dialog.setHeaderText("Hệ Thống Đấu Giá Trực Tuyến");
                dialog.setContentText("Không tìm thấy Server tự động.\nNhập IP hoặc địa chỉ Server\n(VD: 192.168.1.5 hoặc 0.tcp.ngrok.io:12345):");
                Optional<String> result = dialog.showAndWait();
                if (result.isEmpty()) { stage.close(); return; }
                String input = result.get().trim();
                if (input.isEmpty()) input = "127.0.0.1";
                connectAndLoadUI(stage, input);
            });

        }).start();
    }

    /** Đọc địa chỉ Server (ip:port) từ bảng server_config trong Database */
    private String readServerAddressFromDb() {
        try (java.sql.Connection conn = Team2_CS2_Auction.util.DBConnection.getConnection()) {
            // Tạo bảng nếu chưa có (đề phòng Client chạy trước Server)
            conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS server_config (" +
                "  id INT PRIMARY KEY, " +
                "  ip_address VARCHAR(255) NOT NULL, " +
                "  port INT NOT NULL, " +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            ).executeUpdate();

            java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT ip_address, port FROM server_config WHERE id = 1"
            );
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String ip = rs.getString("ip_address");
                int port = rs.getInt("port");
                if (ip != null && !ip.equals("127.0.0.1") && !ip.isEmpty()) {
                    return ip + ":" + port;
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] Lỗi đọc cấu hình: " + e.getMessage());
        }
        return null;
    }

    /** Kết nối tới server và tải giao diện đăng nhập */
    private void connectAndLoadUI(Stage stage, String input) {
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

        // WebSocket sẽ được kết nối sau khi đăng nhập thành công trong Dang_nhap_Controller
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_nhap.fxml"));
                Parent root = fxmlLoader.load();

                Scene scene = new Scene(root);
                stage.hide();
                stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - ĐHQGHN");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();

            } catch (IOException e) {
                System.err.println("Lỗi: Không tìm thấy file FXML!");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        NetworkManager.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}