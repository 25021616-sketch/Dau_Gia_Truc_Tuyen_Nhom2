package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.UserRole;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Session.Session;
import Team2_CS2_Auction.Networking.NetworkManager;
import Team2_CS2_Auction.Networking.NetworkMessage;
import Team2_CS2_Auction.Networking.NetworkListener;
import Team2_CS2_Auction.Networking.UserDTO;
import Team2_CS2_Auction.Networking.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
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
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;
    @FXML private TextField txtMatKhauShow;
    @FXML private Button btnTogglePassword;
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Tải trước giao diện trang chủ trong bộ nhớ đệm ngay khi vừa mở màn hình đăng nhập để tối ưu tốc độ vào
        preLoadScene(FXML_USER_HOME);
    }

    @Override
    protected void onResume() {
        if (lblMessage != null) {
            lblMessage.setText("");
        }
        // Phục hồi lại trạng thái của các nút bấm và ô nhập liệu
        restoreLoginUIState();

        // Reset trạng thái hiển thị mật khẩu về mặc định (ẩn) khi quay lại
        isPasswordVisible = false;
        if (txtMatKhauShow != null) {
            txtMatKhauShow.setText("");
            txtMatKhauShow.setVisible(false);
            txtMatKhauShow.setManaged(false);
        }
        if (Mat_khau != null) {
            Mat_khau.setText("");
            Mat_khau.setVisible(true);
            Mat_khau.setManaged(true);
        }
        if (btnTogglePassword != null) {
            btnTogglePassword.setText("👁");
            btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
        }
    }

    @FXML
    private void handleTogglePassword(ActionEvent event) {
        if (isPasswordVisible) {
            // Đang hiển thị mật khẩu -> Chuyển sang ẩn mật khẩu
            Mat_khau.setText(txtMatKhauShow.getText());

            txtMatKhauShow.setVisible(false);
            txtMatKhauShow.setManaged(false);
            Mat_khau.setVisible(true);
            Mat_khau.setManaged(true);

            btnTogglePassword.setText("👁");
            btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 15 0 0;");
            Mat_khau.requestFocus();
            Mat_khau.selectEnd();
            Mat_khau.deselect();

            isPasswordVisible = false;
        } else {
            // Đang ẩn mật khẩu -> Chuyển sang hiển thị mật khẩu
            txtMatKhauShow.setText(Mat_khau.getText());

            Mat_khau.setVisible(false);
            Mat_khau.setManaged(false);
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
    private void handleLogin(ActionEvent event) {
        String username = Ten_dang_nhap.getText().trim();
        
        // Đồng bộ mật khẩu trước khi xử lý đăng nhập
        String password;
        if (isPasswordVisible) {
            password = txtMatKhauShow.getText();
            Mat_khau.setText(password);
        } else {
            password = Mat_khau.getText();
            txtMatKhauShow.setText(password);
        }
        
        boolean isAdminLogin = Dang_nhap_Admin != null && Dang_nhap_Admin.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            if (lblMessage != null) {
                lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                lblMessage.setText("⚠ Vui lòng nhập tài khoản và mật khẩu!");
            }
            return;
        }

        // Tối ưu hóa UI: Vô hiệu hóa các trường nhập và thay đổi văn bản nút để thông báo tiến trình cho người dùng
        if (btnLogin != null) {
            btnLogin.setDisable(true);
            btnLogin.setText("ĐANG ĐĂNG NHẬP...");
        }
        Ten_dang_nhap.setDisable(true);
        Mat_khau.setDisable(true);
        if (txtMatKhauShow != null) txtMatKhauShow.setDisable(true);
        if (Dang_nhap_Admin != null) Dang_nhap_Admin.setDisable(true);
        if (lblMessage != null) {
            lblMessage.setTextFill(javafx.scene.paint.Color.web("#20335e"));
            lblMessage.setText("⏳ Đang kết nối tới máy chủ...");
        }

        // Khởi động Background Thread để làm việc với mạng (Loại bỏ hoàn toàn cảm giác lag đơ)
        new Thread(() -> {
            NetworkManager nm = NetworkManager.getInstance();

            // Nếu chưa kết nối thì kết nối trực tiếp không trì hoãn (Loại bỏ Thread.sleep cũ gây trễ)
            if (!nm.isConnected()) {
                nm.connect("localhost", 8080);
            }

            // Kiểm tra kết nối
            if (!nm.isConnected()) {
                Platform.runLater(() -> {
                    // Phục hồi lại trạng thái nút bấm
                    restoreLoginUIState();
                    if (lblMessage != null) {
                        lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                        lblMessage.setText("❌ Lỗi kết nối: Không thể kết nối tới máy chủ!");
                    }
                });
                return;
            }

            // Giao tiếp qua Socket
            Platform.runLater(() -> {
                if (lblMessage != null) {
                    lblMessage.setTextFill(javafx.scene.paint.Color.web("#20335e"));
                    lblMessage.setText("⏳ Đang xác thực tài khoản...");
                }

                NetworkListener listener = new NetworkListener() {
                    @Override
                    public void onMessageReceived(NetworkMessage message) {
                        if ("LOGIN_SUCCESS".equals(message.getAction())) {
                            Platform.runLater(() -> {
                                try {
                                    Gson gson = GsonUtil.getGson();
                                    UserDTO userDTO = gson.fromJson(message.getPayload(), UserDTO.class);
                                    User user = userDTO.toUser(); // Chuyển đổi từ DTO sang Model
                                    Session.currentUser = user;

                                    if (lblMessage != null) {
                                        lblMessage.setTextFill(javafx.scene.paint.Color.web("#2E7D32"));
                                        lblMessage.setText("✅ Đăng nhập thành công! Đang vào...");
                                    }

                                    if (isAdminLogin) {
                                        if (user.getRole() == UserRole.ADMIN) {
                                            restoreLoginUIState();
                                            navigateTo(event, FXML_ADMIN_HOME, "Hệ thống quản trị");
                                        } else {
                                            restoreLoginUIState();
                                            if (lblMessage != null) {
                                                lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                                                lblMessage.setText("⚠ Tài khoản không có quyền Admin!");
                                            }
                                        }
                                    } else {
                                        restoreLoginUIState();
                                        navigateTo(event, FXML_USER_HOME, "Sàn đấu giá");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    restoreLoginUIState();
                                    if (lblMessage != null) {
                                        lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                                        lblMessage.setText("❌ Lỗi xử lý dữ liệu: " + e.getMessage());
                                    }
                                }
                            });
                            nm.removeListener(this);
                        } else if ("LOGIN_FAILED".equals(message.getAction())) {
                            Platform.runLater(() -> {
                                restoreLoginUIState();
                                if (lblMessage != null) {
                                    lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                                    lblMessage.setText("❌ " + message.getPayload());
                                }
                            });
                            nm.removeListener(this);
                        }
                    }

                    @Override
                    public void onConnectionError() {
                        Platform.runLater(() -> {
                            restoreLoginUIState();
                            if (lblMessage != null) {
                                lblMessage.setTextFill(javafx.scene.paint.Color.web("#C62828"));
                                lblMessage.setText("❌ Mất kết nối tới máy chủ!");
                            }
                        });
                        nm.removeListener(this);
                    }
                };

                nm.addListener(listener);

                // Gửi thông tin đăng nhập
                JsonObject payload = new JsonObject();
                payload.addProperty("username", username);
                payload.addProperty("password", password);
                payload.addProperty("isAdminLogin", isAdminLogin);
                
                nm.send("LOGIN", payload);
            });
        }).start();
    }

    private void restoreLoginUIState() {
        if (btnLogin != null) {
            btnLogin.setDisable(false);
            btnLogin.setText("Đăng Nhập");
        }
        Ten_dang_nhap.setDisable(false);
        Mat_khau.setDisable(false);
        if (txtMatKhauShow != null) txtMatKhauShow.setDisable(false);
        if (Dang_nhap_Admin != null) Dang_nhap_Admin.setDisable(false);
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

    @FXML public void handleBackToMain(ActionEvent event) { navigateTo(event, FXML_USER_HOME, "Màn hình chính"); }
}