package Team2_CS2_Auction;

import Team2_CS2_Auction.Networking.NetworkManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // =========================================================
        // Hỏi địa chỉ Server khi khởi động
        // Hỗ trợ 2 dạng:
        //   - IP thường:  192.168.1.10
        //   - ngrok:      0.tcp.ngrok.io:12345
        // =========================================================
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("Kết nối đến Máy Chủ");
        dialog.setHeaderText("Hệ Thống Đấu Giá Trực Tuyến");
        dialog.setContentText("Nhập IP hoặc địa chỉ Server\n(VD: 192.168.1.5  hoặc  0.tcp.ngrok.io:12345):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            stage.close();
            return;
        }

        String input = result.get().trim();
        if (input.isEmpty()) input = "127.0.0.1";

        // Tách host và port nếu người dùng nhập dạng host:port
        String serverHost;
        int serverPort = 8080;
        if (input.contains(":")) {
            String[] parts = input.split(":");
            serverHost = parts[0].trim();
            try { serverPort = Integer.parseInt(parts[1].trim()); } catch (Exception ignored) {}
        } else {
            serverHost = input;
        }

        System.out.println("Đang kết nối tới: " + serverHost + ":" + serverPort + " ...");
        NetworkManager.getInstance().connect(serverHost, serverPort);

        // =========================================================
        // Mở giao diện chính
        // =========================================================
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
    }

    @Override
    public void stop() {
        NetworkManager.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}