package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Model.user.UserRole;
import Team2_CS2_Auction.Service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class Dang_nhap_Controller extends Base_Admin_Controller {

    private static final String FXML_REGISTER = "dang_ky_tai_khoan.fxml";
    private static final String FXML_USER_HOME = "Man_hinh_chinh_Users.fxml";
    private static final String FXML_ADMIN_HOME = "Trang_chu_Admin.fxml";

    @FXML private CheckBox Dang_nhap_Admin;
    @FXML private PasswordField Mat_khau;
    @FXML private TextField Ten_dang_nhap;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = (Ten_dang_nhap.getText() != null) ? Ten_dang_nhap.getText().trim() : "";
        String password = (Mat_khau.getText() != null) ? Mat_khau.getText() : "";
        boolean isAdminLogin = (Dang_nhap_Admin != null && Dang_nhap_Admin.isSelected());

        try {
            // Sửa tên hàm thành handleLoginLogic để khớp với Service mới
            User user = userService.handleLoginLogic(username, password, isAdminLogin);

            // Chuyển trang dựa trên Role từ Enum của Model User
            if (user.getRole() == UserRole.ADMIN) {
                showStyledAlert("Admin", "Xin chào quản trị viên " + user.getUsername() + "!", Alert.AlertType.INFORMATION);
                navigateTo(event, FXML_ADMIN_HOME, "Hệ thống quản trị");
            } else {
                showStyledAlert("Thành công", "Đăng nhập thành công! Chào mừng " + user.getUsername(), Alert.AlertType.INFORMATION);
                navigateTo(event, FXML_USER_HOME, "Sàn đấu giá");
            }

        } catch (Exception e) {
            // Hiển thị thông báo lỗi (VD: "Sai tên đăng nhập", "Không có quyền Admin", v.v.)
            showStyledAlert("Thông báo", e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleSwitchToRegister(ActionEvent event) {
        navigateTo(event, FXML_REGISTER, "Đăng ký thành viên");
    }

    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            switchScene(event, fxmlFile, title);
        } catch (Exception e) {
            showStyledAlert("Lỗi", "Không thể tải trang: " + fxmlFile, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Dùng Alert có Style cho đồng bộ với trang Đăng ký
    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #20335e; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        alert.showAndWait();
    }
}