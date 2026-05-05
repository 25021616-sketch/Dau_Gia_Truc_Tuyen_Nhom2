package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class Dang_ki_tai_khoan_Controller extends Base_Admin_Controller {

    @FXML private Button Dang_ky;
    @FXML private Hyperlink Dang_nhap_ngay;
    @FXML private PasswordField Dat_mat_khau;
    @FXML private PasswordField Nhap_lai_mat_khau;
    @FXML private TextField Sdt_dang_ki;
    @FXML private TextField Ten_dang_ki;

    private UserService userService = new UserService();

    // ==========================
    // CHUYỂN SANG ĐĂNG NHẬP
    // ==========================
    @FXML
    public void handleSwitchToLogin(ActionEvent event) {
        switchScene(event, "dang_nhap.fxml", "Trang Đăng nhập");
    }

    // ==========================
    // ĐĂNG KÝ
    // ==========================
    @FXML
    public void handleRegister(ActionEvent event) {

        String username = Ten_dang_ki.getText().trim();
        String phone = Sdt_dang_ki.getText().trim();
        String password = Dat_mat_khau.getText().trim();
        String confirmPassword = Nhap_lai_mat_khau.getText().trim();

        // 1. Kiểm tra rỗng
        if (username.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            showStyledAlert(
                    "⚠ Thiếu thông tin",
                    "Vui lòng nhập đầy đủ tất cả các trường.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 2. Username quá ngắn
        if (username.length() < 4) {
            showStyledAlert(
                    "⚠ Tên đăng nhập không hợp lệ",
                    "Tên đăng nhập phải từ 4 ký tự trở lên.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 3. Username chứa ký tự lạ
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showStyledAlert(
                    "⚠ Tên đăng nhập không hợp lệ",
                    "Chỉ được dùng chữ cái, số và dấu gạch dưới (_).",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 4. SĐT sai định dạng
        if (!phone.matches("^0\\d{9}$")) {
            showStyledAlert(
                    "⚠ Số điện thoại không hợp lệ",
                    "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 5. SĐT đã tồn tại
        if (userService.existsByPhone(phone)) {
            showStyledAlert(
                    "⚠ Số điện thoại đã tồn tại",
                    "Mỗi số điện thoại chỉ được dùng cho 1 tài khoản.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 6. Password quá ngắn
        if (password.length() < 6) {
            showStyledAlert(
                    "⚠ Mật khẩu yếu",
                    "Mật khẩu phải có ít nhất 6 ký tự.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 7. Password phải có chữ + số
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            showStyledAlert(
                    "⚠ Mật khẩu chưa đủ mạnh",
                    "Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 8. Nhập lại mật khẩu không khớp
        if (!password.equals(confirmPassword)) {
            showStyledAlert(
                    "❌ Mật khẩu không khớp",
                    "Vui lòng nhập lại đúng mật khẩu.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        // 9. Tạo tài khoản
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(password);

        boolean result = userService.register(user);

        if (result) {
            showStyledAlert(
                    "✅ Thành công",
                    "Đăng ký tài khoản thành công!",
                    Alert.AlertType.INFORMATION
            );

            switchScene(event, "dang_nhap.fxml", "Trang Đăng nhập");

        } else {
            showStyledAlert(
                    "❌ Thất bại",
                    "Tên đăng nhập đã tồn tại hoặc lỗi hệ thống.",
                    Alert.AlertType.ERROR
            );
        }
    }

    // ==========================
    // ALERT ĐẸP
    // ==========================
    private void showStyledAlert(String title, String message, Alert.AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane pane = alert.getDialogPane();

        pane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 15;"
        );

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);

        okButton.setStyle(
                "-fx-background-color: #20335e;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }
}