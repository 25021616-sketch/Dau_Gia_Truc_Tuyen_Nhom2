package Team2_CS2_Auction.Controller;

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

    private final UserService userService = new UserService();

    // =========================
    // CHUYỂN SANG ĐĂNG NHẬP
    // =========================
    @FXML
    public void handleSwitchToLogin(ActionEvent event) {

        switchScene(
                event,
                "dang_nhap.fxml",
                "Trang Đăng nhập"
        );
    }

    // =========================
    // ĐĂNG KÝ
    // =========================
    @FXML
    public void handleRegister(ActionEvent event) {

        String username = Ten_dang_ki.getText().trim();
        String phone = Sdt_dang_ki.getText().trim();

        // KHÔNG trim password
        String password = Dat_mat_khau.getText();
        String confirmPassword = Nhap_lai_mat_khau.getText();

        try {

            // GỌI SERVICE VALIDATE CHI TIẾT
            userService.handleRegisterLogic(
                    username,
                    phone,
                    password,
                    confirmPassword
            );

            // THÀNH CÔNG
            showStyledAlert(
                    "✅ Thành công",
                    "Đăng ký tài khoản thành công!",
                    Alert.AlertType.INFORMATION
            );

            switchScene(
                    event,
                    "dang_nhap.fxml",
                    "Trang Đăng nhập"
            );

        } catch (Exception e) {

            // HIỆN CHI TIẾT LỖI
            showStyledAlert(
                    "⚠ Thông báo",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );
        }
    }

    // =========================
    // ALERT UI
    // =========================
    private void showStyledAlert(
            String title,
            String message,
            Alert.AlertType type
    ) {

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

        Button okButton =
                (Button) pane.lookupButton(ButtonType.OK);

        if (okButton != null) {

            okButton.setStyle(
                    "-fx-background-color: #20335e;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }

        alert.showAndWait();
    }
}
