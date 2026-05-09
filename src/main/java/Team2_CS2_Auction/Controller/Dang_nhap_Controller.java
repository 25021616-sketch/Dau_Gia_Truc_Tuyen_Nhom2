package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.UserRole;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Service.UserService;

// ✅ IMPORT SESSION
import Team2_CS2_Auction.Session.Session;

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

        // =========================
        // 1. LẤY DỮ LIỆU TỪ FORM
        // =========================
        String username = Ten_dang_nhap.getText().trim();
        String password = Mat_khau.getText();

        // 🔥 tránh lỗi null checkbox
        boolean isAdminLogin =
                Dang_nhap_Admin != null
                        && Dang_nhap_Admin.isSelected();

        // =========================
        // 2. CHECK RỖNG
        // =========================
        if (username.isEmpty() || password.isEmpty()) {

            showAlert(
                    "Thiếu thông tin",
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        // =========================
        // 3. KIỂM TRA LOGIN
        // =========================
        User user = userService.login(username, password);

        // DEBUG
        System.out.println("LOGIN RESULT: " + user);

        // =========================
        // 4. LOGIN THẤT BẠI
        // =========================
        if (user == null) {

            showAlert(
                    "Đăng nhập thất bại",
                    "Sai tên đăng nhập hoặc mật khẩu.",
                    Alert.AlertType.ERROR
            );

            return;
        }

        // ==================================================
        // 5. LƯU USER HIỆN TẠI VÀO SESSION
        // ==================================================
        // Sau này toàn hệ thống sẽ biết:
        // - ai đang đăng nhập
        // - ai đăng sản phẩm
        // - ai đấu giá
        // - phân quyền admin/user
        // ==================================================
        Session.currentUser = user;

        // DEBUG
        System.out.println(
                "Current User: "
                        + Session.currentUser.getUsername()
        );

        // =========================
        // 6. LOGIN ADMIN
        // =========================
        if (isAdminLogin) {

            UserRole role = user.getRole();

            if (role == UserRole.ADMIN) {

                showAlert(
                        "Thành công",
                        "Đăng nhập Admin thành công.",
                        Alert.AlertType.INFORMATION
                );

                navigateTo(
                        event,
                        FXML_ADMIN_HOME,
                        "Trang Quản Trị"
                );

            } else {

                showAlert(
                        "Từ chối truy cập",
                        "Tài khoản này không có quyền Admin.",
                        Alert.AlertType.WARNING
                );
            }

        } else {

            // =========================
            // 7. LOGIN USER THƯỜNG
            // =========================
            showAlert(
                    "Thành công",
                    "Đăng nhập thành công.",
                    Alert.AlertType.INFORMATION
            );

            navigateTo(
                    event,
                    FXML_USER_HOME,
                    "Trang Người Dùng"
            );
        }
    }

    @FXML
    public void handleSwitchToRegister(ActionEvent event) {

        navigateTo(
                event,
                FXML_REGISTER,
                "Đăng ký tài khoản"
        );
    }

    private void navigateTo(
            ActionEvent event,
            String fxmlFile,
            String title
    ) {

        try {

            switchScene(event, fxmlFile, title);

        } catch (Exception e) {

            showAlert(
                    "Lỗi hệ thống",
                    "Không thể tải trang: " + fxmlFile,
                    Alert.AlertType.ERROR
            );

            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToMain(ActionEvent event) {

        navigateTo(
                event,
                FXML_USER_HOME,
                "Màn hình chính"
        );
    }

    private void showAlert(
            String title,
            String message,
            Alert.AlertType type
    ) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🔥 QUAN TRỌNG
        alert.showAndWait();
    }
}