package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Repository.UserRepository;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public abstract class Base_Admin_Controller {

    private static final String BASE_PATH = "/Team2_CS2_Auction/example/myauctionapp/";

    @FXML protected Label lblUsername;
    @FXML protected Label lblBalance;

    private final UserRepository userRepository = new UserRepository();

    public void updateBalanceDisplay() {
        if (Team2_CS2_Auction.Session.Session.currentUser != null) {
            String username = Team2_CS2_Auction.Session.Session.currentUser.getUsername();
            // Lấy số dư thực tế mới nhất từ CSDL
            double balance = userRepository.getBalance(Team2_CS2_Auction.Session.Session.currentUser.getId());
            
            Platform.runLater(() -> {
                if (lblUsername != null) {
                    lblUsername.setText(username);
                }
                if (lblBalance != null) {
                    lblBalance.setText(String.format("%.2f $", balance));
                }
            });
        }
    }

    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        navigate(event, fxmlFileName, title, null);
    }

    public void switchSceneWithData(ActionEvent event, String fxmlFileName, String title, Object data) {
        navigate(event, fxmlFileName, title, data);
    }

    // Bộ nhớ đệm lưu trữ các giao diện đã tải (Scene Caching)
    private static final java.util.Map<String, Parent> sceneCache = new java.util.HashMap<>();
    private static final java.util.Map<String, Object> controllerCache = new java.util.HashMap<>();

    // Lưu lại controller hiện tại đang hiển thị để gọi hook
    private static Object currentController = null;

    public static Object getCurrentController() {
        return currentController;
    }

    private void navigate(ActionEvent event, String fxmlFileName, String title, Object data) {
        try {
            // 1. Gọi hook dọn dẹp (tạm dừng timer/socket) trước khi rời màn hình cũ
            if (currentController instanceof Base_Admin_Controller) {
                ((Base_Admin_Controller) currentController).cleanup();
            }

            Parent root = sceneCache.get(fxmlFileName);
            Object controller = controllerCache.get(fxmlFileName);

            // 2. Nếu chưa có trong Cache thì tiến hành nạp từ file FXML
            if (root == null) {
                FXMLLoader loader = getFXMLLoader(fxmlFileName);
                if (loader == null) return;
                root = loader.load();
                controller = loader.getController();

                sceneCache.put(fxmlFileName, root);
                controllerCache.put(fxmlFileName, controller);
            }

            // Cập nhật controller hiện tại
            currentController = controller;

            // 3. Chuyển dữ liệu và hiển thị màn hình (Lấy từ Cache nên tức thì 0 giây)
            passDataToController(controller, data);
            displayScene(event, root, title);

            // 4. Gọi hook thức dậy (Resume) để fetch data ngầm sau khi màn hình đã hiện
            if (controller instanceof Base_Admin_Controller) {
                Base_Admin_Controller baseCtrl = (Base_Admin_Controller) controller;
                baseCtrl.updateBalanceDisplay();
                baseCtrl.onResume();
            }

        } catch (java.io.IOException e) {
            System.err.println("Lỗi trong quá trình nạp giao diện (" + fxmlFileName + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hook gọi ngay trước khi rời màn hình (Tạm dừng tài nguyên)
     */
    protected void cleanup() {
    }

    /**
     * Hook gọi ngay sau khi màn hình được hiển thị lại từ Cache (Cập nhật dữ liệu ngầm)
     */
    protected void onResume() {
    }

    private FXMLLoader getFXMLLoader(String fxmlFileName) {
        URL fxmlLocation = getClass().getResource(BASE_PATH + fxmlFileName);
        if (fxmlLocation == null) {
            System.err.println("Không tìm thấy file FXML tại đường dẫn: " + BASE_PATH + fxmlFileName);
            return null;
        }
        return new FXMLLoader(fxmlLocation);
    }

    private void passDataToController(Object controller, Object data) {
        if (data == null || controller == null) return;

        if (controller instanceof Phien_Dau_Gia_Controller && data instanceof Auction) {
            Phien_Dau_Gia_Controller targetController = (Phien_Dau_Gia_Controller) controller;
            targetController.setAuctionData((Auction) data);
        }
    }

    private void displayScene(ActionEvent event, Parent root, String title) {
        Stage stage = getStageFromEvent(event);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle(title);
        stage.setMaximized(true);
        stage.show();
    }

    private Stage getStageFromEvent(ActionEvent event) {
        Node sourceNode = (Node) event.getSource();
        return (Stage) sourceNode.getScene().getWindow();
    }

    // ================== CÁC HÀM XỬ LÝ THANH ĐIỀU HƯỚNG (SIDEBAR & NAVBAR) CHUNG ==================
    @javafx.fxml.FXML
    public void handleQuayLaiTrangChu(ActionEvent event) { switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính"); }

    @javafx.fxml.FXML
    public void handleGoTothemsanpham(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }

    @javafx.fxml.FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) { switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi"); }

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

            // Hàm load lại dữ liệu nếu có, các lớp con có thể override
            onNapTienSuccess();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm ảo để các lớp con override nếu muốn load lại data sau khi nạp tiền
    protected void onNapTienSuccess() {
        updateBalanceDisplay();
    }
}