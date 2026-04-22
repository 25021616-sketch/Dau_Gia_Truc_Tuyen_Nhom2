package Team2_CS2_Auction.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;

public class Dang_nhap_Controller extends Base_Admin_Controller {

    // Định nghĩa các đường dẫn file FXML để dễ quản lý và bảo trì
    private static final String FXML_REGISTER = "dang_ky_tai_khoan.fxml";
    private static final String FXML_USER_HOME = "Man_hinh_chinh_Users.fxml";
    private static final String FXML_ADMIN_HOME = "Trang_chu_Admin.fxml";

    @FXML
    private CheckBox Dang_nhap_Admin;

    @FXML
    private PasswordField Mat_khau;

    @FXML
    private TextField Ten_dang_nhap;

    /**
     * Xử lý sự kiện khi người dùng nhấn nút Đăng Nhập
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = Ten_dang_nhap.getText().trim();
        String password = Mat_khau.getText().trim();
        boolean isAdminLogin = Dang_nhap_Admin.isSelected();

        // 1. Kiểm tra đầu vào
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // 2. Logic điều hướng
        if (isAdminLogin) {
            // Ở đây bạn có thể thêm bước xác thực quyền Admin từ Database
            // trước khi gọi switchScene
            navigateTo(event, FXML_ADMIN_HOME, "Hệ thống Quản trị viên");
        } else {
            navigateTo(event, FXML_USER_HOME, "Trang chủ Người dùng");
        }
    }

    /**
     * Chuyển sang màn hình đăng ký
     */
    @FXML
    public void handleSwitchToRegister(ActionEvent event) {
        navigateTo(event, FXML_REGISTER, "Đăng ký tài khoản");
    }

    /**
     * Quay lại màn hình chính (Dành cho nút quay lại hoặc thoát)
     */
    @FXML
    public void handleBackToMain(ActionEvent event) {
        navigateTo(event, FXML_USER_HOME, "Màn hình chính");
    }

    /**
     * Phương thức hỗ trợ điều hướng để tránh lặp code
     */
    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            switchScene(event, fxmlFile, title);
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Không thể tải trang: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị thông báo nhanh cho người dùng
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}