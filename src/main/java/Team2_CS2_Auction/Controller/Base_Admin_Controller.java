package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public abstract class Base_Admin_Controller {

    private static final String BASE_PATH = "/Team2_CS2_Auction/example/myauctionapp/";

    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        navigate(event, fxmlFileName, title, null);
    }

    public void switchSceneWithData(ActionEvent event, String fxmlFileName, String title, Object data) {
        navigate(event, fxmlFileName, title, data);
    }

    private void navigate(ActionEvent event, String fxmlFileName, String title, Object data) {
        try {
            FXMLLoader loader = getFXMLLoader(fxmlFileName);
            if (loader == null) return;

            Parent root = loader.load();
            passDataToController(loader.getController(), data);
            displayScene(event, root, title);

        } catch (IOException e) {
            System.err.println("Lỗi trong quá trình nạp giao diện (" + fxmlFileName + "): " + e.getMessage());
            e.printStackTrace();
        }
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
    protected void onNapTienSuccess() {}
}