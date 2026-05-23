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

    @FXML private TextField txtMatKhauShow;
    @FXML private TextField txtConfirmMatKhauShow;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnToggleConfirmPassword;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

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

    @FXML
    private void handleTogglePassword(ActionEvent event) {
        if (isPasswordVisible) {
            Dat_mat_khau.setText(txtMatKhauShow.getText());
            txtMatKhauShow.setVisible(false);
            txtMatKhauShow.setManaged(false);
            Dat_mat_khau.setVisible(true);
            Dat_mat_khau.setManaged(true);
            btnTogglePassword.setText("👁");
            btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
            Dat_mat_khau.requestFocus();
            Dat_mat_khau.selectEnd();
            Dat_mat_khau.deselect();
            isPasswordVisible = false;
        } else {
            txtMatKhauShow.setText(Dat_mat_khau.getText());
            Dat_mat_khau.setVisible(false);
            Dat_mat_khau.setManaged(false);
            txtMatKhauShow.setVisible(true);
            txtMatKhauShow.setManaged(true);
            btnTogglePassword.setText("👁");
            btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #20335e; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
            txtMatKhauShow.requestFocus();
            txtMatKhauShow.selectEnd();
            txtMatKhauShow.deselect();
            isPasswordVisible = true;
        }
    }

    @FXML
    private void handleToggleConfirmPassword(ActionEvent event) {
        if (isConfirmPasswordVisible) {
            Nhap_lai_mat_khau.setText(txtConfirmMatKhauShow.getText());
            txtConfirmMatKhauShow.setVisible(false);
            txtConfirmMatKhauShow.setManaged(false);
            Nhap_lai_mat_khau.setVisible(true);
            Nhap_lai_mat_khau.setManaged(true);
            btnToggleConfirmPassword.setText("👁");
            btnToggleConfirmPassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
            Nhap_lai_mat_khau.requestFocus();
            Nhap_lai_mat_khau.selectEnd();
            Nhap_lai_mat_khau.deselect();
            isConfirmPasswordVisible = false;
        } else {
            txtConfirmMatKhauShow.setText(Nhap_lai_mat_khau.getText());
            Nhap_lai_mat_khau.setVisible(false);
            Nhap_lai_mat_khau.setManaged(false);
            txtConfirmMatKhauShow.setVisible(true);
            txtConfirmMatKhauShow.setManaged(true);
            btnToggleConfirmPassword.setText("👁");
            btnToggleConfirmPassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #20335e; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
            txtConfirmMatKhauShow.requestFocus();
            txtConfirmMatKhauShow.selectEnd();
            txtConfirmMatKhauShow.deselect();
            isConfirmPasswordVisible = true;
        }
    }

    // =========================
    // ĐĂNG KÝ
    // =========================
    @FXML
    public void handleRegister(ActionEvent event) {

        String username = Ten_dang_ki.getText().trim();
        String phone = Sdt_dang_ki.getText().trim();

        // Đồng bộ mật khẩu trước khi lấy giá trị
        String password = isPasswordVisible ? txtMatKhauShow.getText() : Dat_mat_khau.getText();
        String confirmPassword = isConfirmPasswordVisible ? txtConfirmMatKhauShow.getText() : Nhap_lai_mat_khau.getText();

        Dat_mat_khau.setText(password);
        Nhap_lai_mat_khau.setText(confirmPassword);

        try {
            // Vô hiệu hóa UI khi đang thực hiện logic đăng ký
            setRegisterFieldsDisable(true);

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
                    setRegisterFieldsDisable(false);
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
            // Mở khóa UI nếu thất bại
            setRegisterFieldsDisable(false);
        }
    }

    private void setRegisterFieldsDisable(boolean disable) {
        if (Ten_dang_ki != null) Ten_dang_ki.setDisable(disable);
        if (Sdt_dang_ki != null) Sdt_dang_ki.setDisable(disable);
        if (Dat_mat_khau != null) Dat_mat_khau.setDisable(disable);
        if (txtMatKhauShow != null) txtMatKhauShow.setDisable(disable);
        if (Nhap_lai_mat_khau != null) Nhap_lai_mat_khau.setDisable(disable);
        if (txtConfirmMatKhauShow != null) txtConfirmMatKhauShow.setDisable(disable);
    }

    @Override
    protected void onResume() {
        if (lblMessage != null) {
            lblMessage.setText("");
        }
        if (Dang_ky != null) {
            Dang_ky.setDisable(false);
        }
        setRegisterFieldsDisable(false);

        // Reset trạng thái hiển thị mật khẩu về mặc định (ẩn) khi quay lại màn đăng ký
        isPasswordVisible = false;
        isConfirmPasswordVisible = false;

        if (txtMatKhauShow != null) {
            txtMatKhauShow.setText("");
            txtMatKhauShow.setVisible(false);
            txtMatKhauShow.setManaged(false);
        }
        if (Dat_mat_khau != null) {
            Dat_mat_khau.setText("");
            Dat_mat_khau.setVisible(true);
            Dat_mat_khau.setManaged(true);
        }
        if (btnTogglePassword != null) {
            btnTogglePassword.setText("👁");
            btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
        }

        if (txtConfirmMatKhauShow != null) {
            txtConfirmMatKhauShow.setText("");
            txtConfirmMatKhauShow.setVisible(false);
            txtConfirmMatKhauShow.setManaged(false);
        }
        if (Nhap_lai_mat_khau != null) {
            Nhap_lai_mat_khau.setText("");
            Nhap_lai_mat_khau.setVisible(true);
            Nhap_lai_mat_khau.setManaged(true);
        }
        if (btnToggleConfirmPassword != null) {
            btnToggleConfirmPassword.setText("👁");
            btnToggleConfirmPassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
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
