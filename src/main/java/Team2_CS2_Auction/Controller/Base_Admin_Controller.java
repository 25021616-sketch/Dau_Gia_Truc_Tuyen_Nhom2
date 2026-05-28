package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Repository.UserRepository;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public abstract class Base_Admin_Controller {

    private static final String BASE_PATH = "/Team2_CS2_Auction/example/myauctionapp/";

    @FXML protected Label lblUsername;
    @FXML protected Label lblBalance;

    private final UserRepository userRepository = new UserRepository();

    public void updateBalanceDisplay() {
        if (Team2_CS2_Auction.Session.Session.currentUser == null) return;

        final String username = Team2_CS2_Auction.Session.Session.currentUser.getUsername();
        final int userId = Team2_CS2_Auction.Session.Session.currentUser.getId();

        // Chạy query trên background thread để không block JavaFX UI thread
        new Thread(() -> {
            try {
                double balance = userRepository.getBalance(userId);
                Platform.runLater(() -> {
                    if (lblUsername != null) lblUsername.setText(username);
                    if (lblBalance != null) lblBalance.setText(String.format("%.2f $", balance));
                });
            } catch (Exception e) {
                System.err.println("[Balance] Lỗi lấy số dư: " + e.getMessage());
            }
        }, "balance-fetch-thread").start();
    }

    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        navigate(event, fxmlFileName, title, null);
    }

    public void switchSceneWithData(ActionEvent event, String fxmlFileName, String title, Object data) {
        navigate(event, fxmlFileName, title, data);
    }

    /** Cache Scene và Controller đã tải để chuyển màn hình không bị lag */
    private static final java.util.Map<String, Parent> sceneCache = new java.util.HashMap<>();
    private static final java.util.Map<String, Object> controllerCache = new java.util.HashMap<>();

    private static Object currentController = null;

    public static Object getCurrentController() {
        return currentController;
    }

    public static void preLoadScene(String fxmlFileName) {
        if (sceneCache.containsKey(fxmlFileName)) return;
        Platform.runLater(() -> {
            try {
                java.net.URL fxmlLocation = Base_Admin_Controller.class.getResource(BASE_PATH + fxmlFileName);
                if (fxmlLocation != null) {
                    FXMLLoader loader = new FXMLLoader(fxmlLocation);
                    Parent root = loader.load();
                    Object controller = loader.getController();
                    sceneCache.put(fxmlFileName, root);
                    controllerCache.put(fxmlFileName, controller);
                }
            } catch (IOException e) {
                System.err.println("Lỗi tải trước giao diện " + fxmlFileName + ": " + e.getMessage());
            }
        });
    }

    private void navigate(ActionEvent event, String fxmlFileName, String title, Object data) {
        try {
            if (currentController instanceof Base_Admin_Controller) {
                ((Base_Admin_Controller) currentController).cleanup();
            }

            Parent root = sceneCache.get(fxmlFileName);
            Object controller = controllerCache.get(fxmlFileName);

            // Phien_Dau_Gia.fxml không được cache vì mỗi lần vào là một phiên khác nhau
            boolean shouldCache = !"Phien_Dau_Gia.fxml".equals(fxmlFileName);

            if (root == null) {
                FXMLLoader loader = getFXMLLoader(fxmlFileName);
                if (loader == null) return;
                root = loader.load();
                controller = loader.getController();

                if (shouldCache) {
                    sceneCache.put(fxmlFileName, root);
                    controllerCache.put(fxmlFileName, controller);
                }
            }

            currentController = controller;

            passDataToController(controller, data);
            displayScene(event, root, title);

            if (controller instanceof Base_Admin_Controller) {
                Base_Admin_Controller baseCtrl = (Base_Admin_Controller) controller;
                baseCtrl.updateBalanceDisplay();
                baseCtrl.onResume();
            }

        } catch (java.io.IOException e) {
            System.err.println("Lỗi nạp giao diện (" + fxmlFileName + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hook gọi ngay trước khi rời màn hình — dùng để tạm dừng timer/socket.
     */
    protected void cleanup() {}

    /**
     * Hook gọi ngay sau khi màn hình được hiển thị lại từ Cache — dùng để cập nhật dữ liệu.
     */
    protected void onResume() {}

    private FXMLLoader getFXMLLoader(String fxmlFileName) {
        URL fxmlLocation = getClass().getResource(BASE_PATH + fxmlFileName);
        if (fxmlLocation == null) {
            System.err.println("Không tìm thấy file FXML: " + BASE_PATH + fxmlFileName);
            return null;
        }
        return new FXMLLoader(fxmlLocation);
    }

    private void passDataToController(Object controller, Object data) {
        if (data == null || controller == null) return;

        if (controller instanceof Phien_Dau_Gia_Controller && data instanceof Auction) {
            ((Phien_Dau_Gia_Controller) controller).setAuctionData((Auction) data);
        } else if (controller instanceof Them_san_pham_controller && data instanceof Auction) {
            ((Them_san_pham_controller) controller).setRelistData((Auction) data);
        }
    }

    private void displayScene(ActionEvent event, Parent newRoot, String title) {
        Stage stage = getStageFromEvent(event);

        if (stage.getScene() == null) {
            newRoot.setOpacity(0);
            stage.setScene(new Scene(newRoot));
            stage.setTitle(title);
            stage.show();
            stage.setMaximized(true);
            playFadeIn(newRoot);
        } else {
            Parent oldRoot = stage.getScene().getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(120), oldRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                newRoot.setOpacity(0);
                stage.getScene().setRoot(newRoot);
                stage.setTitle(title);
                // Giữ nguyên trạng thái cửa sổ (maximize/thường) không ép buộc
                playFadeIn(newRoot);
            });
            fadeOut.play();
        }
    }

    /** Fade in 160ms sau khi màn hình mới được gắn vào Scene */
    private void playFadeIn(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(160), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private Stage getStageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    @javafx.fxml.FXML
    public void handleQuayLaiTrangChu(ActionEvent event) { switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính"); }

    @javafx.fxml.FXML
    public void handleGoTothemsanpham(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }

    @javafx.fxml.FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) { switchScene(event, "San_pham_cua_toi.fxml", "Sản phẩm của tôi"); }

    @javafx.fxml.FXML
    public void handleGoToLichSu(ActionEvent event) { switchScene(event, "Phien_Da_Tham_Gia.fxml", "Lịch sử giao dịch"); }

    @javafx.fxml.FXML
    public void handleGoToDangNhap(ActionEvent event) { switchScene(event, "dang_nhap.fxml", "Đăng nhập"); }

    @javafx.fxml.FXML
    public void handleOpenNapTienPopup(ActionEvent event) {
        try {
            FXMLLoader loader = getFXMLLoader("Nap_Tien.fxml");
            if (loader == null) return;
            Parent root = loader.load();

            Nap_Tien_Controller controller = loader.getController();
            if (Team2_CS2_Auction.Session.Session.currentUser != null) {
                controller.setUserData(Team2_CS2_Auction.Session.Session.currentUser);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (event != null && event.getSource() instanceof Node) {
                popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            }
            popupStage.setTitle("Nạp Tiền");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            onNapTienSuccess();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Gọi sau khi popup nạp tiền đóng lại — các lớp con có thể override để reload data */
    protected void onNapTienSuccess() {
        updateBalanceDisplay();
    }
}