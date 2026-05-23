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
    @FXML private Label lblMessage;

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
            if (lblMessage != null) {
                lblMessage.setTextFill(javafx.scene.paint.Color.web("#2E7D32"));
                lblMessage.setText("✅ Đăng ký tài khoản thành công! Đang chuyển hướng...");
            }

            if (Dang_ky != null) Dang_ky.setDisable(true);

            // Tự động chuyển cảnh mượt mà sau 1 giây
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    if (Dang_ky != null) Dang_ky.setDisable(false);
                    switchScene(
                            event,
                            "dang_nhap.fxml",
                            "Trang Đăng nhập"
                    );
                });
            }).start();

        } catch (Exception e) {

            // HIỆN CHI TIẾT LỖI INLINE
            if (lblMessage != null) {
                lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                lblMessage.setText("⚠ " + e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        if (lblMessage != null) {
            lblMessage.setText("");
        }
        if (Dang_ky != null) {
            Dang_ky.setDisable(false);
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
