package Team2_CS2_Auction;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // 1. Tải file FXML
            // Lưu ý: Đảm bảo đường dẫn này khớp chính xác với cấu trúc trong thư mục src/main/resources
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Phien_Dau_Gia.fxml"));
            Parent root = fxmlLoader.load();

            // 2. Tạo Scene
            // Khi làm Full màn hình, bạn không cần truyền kích thước vào Scene(root, width, height)
            Scene scene = new Scene(root);

            // 3. Thiết lập tiêu đề
            stage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - ĐHQGHN");

            // 4. Kích hoạt chế độ phóng lớn toàn màn hình (vẫn thấy thanh Taskbar)
            stage.setMaximized(true);

            // TÙY CHỌN: Nếu bạn muốn mất luôn cả thanh Taskbar (Full Screen thực thụ)
            // stage.setFullScreen(true);

            // 5. Gắn scene vào stage và hiển thị
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file FXML. Hãy kiểm tra lại đường dẫn!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}