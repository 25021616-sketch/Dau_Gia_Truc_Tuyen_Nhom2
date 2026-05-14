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

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = Ten_dang_nhap.getText().trim();
        String password = Mat_khau.getText();
        boolean isAdminLogin = Dang_nhap_Admin != null && Dang_nhap_Admin.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            showStyledAlert("Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu.", Alert.AlertType.WARNING);
            return;
        }

        NetworkManager nm = NetworkManager.getInstance();
        if (!nm.isConnected()) {
            showStyledAlert("Lỗi kết nối", "Chưa kết nối được tới máy chủ. Vui lòng kiểm tra ServerMain!", Alert.AlertType.ERROR);
            return;
        }

        NetworkListener listener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                if ("LOGIN_SUCCESS".equals(message.getAction())) {
                    Platform.runLater(() -> {
                        try {
                            Gson gson = GsonUtil.getGson();
                            UserDTO userDTO = gson.fromJson(message.getPayload(), UserDTO.class);
                            User user = userDTO.toUser(); // Chuyển từ DTO về Model gốc
                            Session.currentUser = user;

                            if (isAdminLogin) {
                                if (user.getRole() == UserRole.ADMIN) {
                                    showStyledAlert("Admin", "Xin chào quản trị viên " + user.getUsername() + "!", Alert.AlertType.INFORMATION);
                                    navigateTo(event, FXML_ADMIN_HOME, "Hệ thống quản trị");
                                } else {
                                    showStyledAlert("Từ chối", "Tài khoản không có quyền Admin.", Alert.AlertType.WARNING);
                                }
                            } else {
                                showStyledAlert("Thành công", "Đăng nhập thành công! Chào mừng " + user.getUsername(), Alert.AlertType.INFORMATION);
                                navigateTo(event, FXML_USER_HOME, "Sàn đấu giá");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showStyledAlert("Lỗi dữ liệu", "Lỗi phân tích JSON: " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                    nm.removeListener(this);
                } else if ("LOGIN_FAILED".equals(message.getAction())) {
                    Platform.runLater(() -> showStyledAlert("Thất bại", message.getPayload(), Alert.AlertType.ERROR));
                    nm.removeListener(this);
                }
            }

            @Override
            public void onConnectionError() {
                Platform.runLater(() -> showStyledAlert("Lỗi", "Mất kết nối với máy chủ", Alert.AlertType.ERROR));
                nm.removeListener(this);
            }
        };

        nm.addListener(listener);

        // Đóng gói Payload đơn giản
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("isAdminLogin", isAdminLogin);
        
        nm.send("LOGIN", payload);
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