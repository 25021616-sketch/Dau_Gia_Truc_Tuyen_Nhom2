package Team2_CS2_Auction.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class Dang_nhap_Controller extends Base_Admin_Controller {

    // Khai báo fx:id từ file FXML
    @FXML private TextField Ten_dang_nhap;
    @FXML private PasswordField Mat_khau;
    @FXML private CheckBox Dang_nhap_Admin;
    @FXML private Button btnBack; // Nút mũi tên quay lại trong FXML

    // Tên các file FXML phải khớp chính xác (phân biệt hoa thường)
    private static final String FXML_REGISTER = "dang_ky_tai_khoan.fxml";
    private static final String FXML_USER_HOME = "Man_hinh_chinh_Users.fxml";
    private static final String FXML_ADMIN_HOME = "Trang_chu_Admin.fxml";

    /**
     * Xử lý Đăng nhập
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = Ten_dang_nhap.getText().trim();
        String password = Mat_khau.getText().trim();
        boolean isAdminLogin = Dang_nhap_Admin.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Thực hiện chuyển trang dựa trên vai trò
        if (isAdminLogin) {
            System.out.println("Đang vào hệ thống Admin...");
            switchScene(event, FXML_ADMIN_HOME, "Hệ thống Quản trị viên");
        } else {
            System.out.println("Đang vào hệ thống Người dùng...");
            switchScene(event, FXML_USER_HOME, "Nhà Sưu Tầm Tinh Hoa - Trang chủ");
        }
    }

    /**
     * Quay lại màn hình chính (Dành cho nút mũi tên ← trên giao diện đăng nhập)
     */
    @FXML
    private void handleBackToMain(ActionEvent event) {
        switchScene(event, FXML_USER_HOME, "Nhà Sưu Tầm Tinh Hoa");
    }

    /**
     * Chuyển sang màn hình đăng ký
     */
    @FXML
    public void handleSwitchToRegister(ActionEvent event) {
        switchScene(event, FXML_REGISTER, "Đăng ký tài khoản");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}