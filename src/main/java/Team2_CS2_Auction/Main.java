package Team2_CS2_Auction;

import Team2_CS2_Auction.Networking.NetworkManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // =========================================================
        // 1. KẾT NỐI SERVER NGAY KHI MỞ APP
        // =========================================================
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("Cấu hình kết nối");
        dialog.setHeaderText("Kết nối đến Server");
        dialog.setContentText("Nhập IP Server (Ví dụ: 192.168.1.10):");

        Optional<String> result = dialog.showAndWait();
        String serverIp = result.orElse("127.0.0.1");

        NetworkManager.getInstance().connect(serverIp, 8080);

        try {
            // 2. Tải file FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_nhap.fxml"));
            Parent root = fxmlLoader.load();

            // 3. Tạo Scene
            Scene scene = new Scene(root);

            // 4. Thiết lập tiêu đề và hiển thị
            stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - ĐHQGHN");
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file FXML. Hãy kiểm tra lại đường dẫn!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Ngắt kết nối khi đóng ứng dụng để giải phóng tài nguyên
        NetworkManager.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}