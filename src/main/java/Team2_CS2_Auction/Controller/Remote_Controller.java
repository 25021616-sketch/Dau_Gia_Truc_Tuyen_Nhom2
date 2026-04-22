package Team2_CS2_Auction.Controller;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class Remote_Controller implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Có thể để trống hoặc setup chung
    }

    /**
     * Hàm chuyển cảnh dùng chung cho tất cả các Controller con.
     * Sử dụng Event tổng quát để hỗ trợ cả ActionEvent và MouseEvent.
     * * @param event        Sự kiện (click chuột, nhấn nút...)
     * @param fxmlFileName Tên file FXML đích (VD: "danh_sach_theo_doi.fxml")
     * @param title        Tiêu đề của cửa sổ mới
     */
    protected void switchScene(Event event, String fxmlFileName, String title) {
        try {
            // Đường dẫn gốc tới thư mục chứa các file FXML của bạn
            String basePath = "/Team2_CS2_Auction/example/myauctionapp/";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(basePath + fxmlFileName));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ sự kiện
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Tạo Scene mới
            Scene scene = new Scene(root);

            // Set tiêu đề nếu có
            if (title != null && !title.isEmpty()) {
                stage.setTitle(title);
            }

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println("Lỗi: Không tìm thấy file " + fxmlFileName);
            e.printStackTrace();
        }
    }
}