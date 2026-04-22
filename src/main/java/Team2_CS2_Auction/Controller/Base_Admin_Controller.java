package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public abstract class Base_Admin_Controller {
    /**
     * Hàm dùng chung để chuyển trang (Switch Scene)
     * @param event: Sự kiện từ nút bấm
     * @param fxmlFileName: Tên file giao diện
     * @param title: Tiêu đề cửa sổ
     */
    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        try {
            // 1. Xác định đường dẫn file FXML
            String path = "/Team2_CS2_Auction/example/myauctionapp/" + fxmlFileName;

            // 2. Khởi tạo bộ tải
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // 3. Lấy Stage hiện tại
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 4. Cập nhật nội dung (Root) thay vì tạo Scene mới
            // Cách này giúp giữ nguyên trạng thái Maximized mà không cần thiết lập lại
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            // 5. Cập nhật tiêu đề và ÉP BUỘC full màn hình
            stage.setTitle(title);
            stage.setMaximized(true);

            // 6. Hiển thị
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi chuyển trang: " + fxmlFileName + ". Kiểm tra lại file trong resources!");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Không tìm thấy file FXML: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}