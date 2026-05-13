package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class San_pham_cua_toi_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlMyItems;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMyProducts();
    }

    private void loadMyProducts() {
        try {
            Member currentUser = (Member) Session.currentUser;
            if (currentUser != null) {
                List<Auction> myAuctions = auctionService.getAuctionsBySeller(currentUser.getId());
                hienThiDanhSach(myAuctions);
            } else {
                System.err.println("Chưa đăng nhập!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hienThiDanhSach(List<Auction> auctions) {
        if (pnlMyItems == null) return;

        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }
        activeControllers.clear();
        pnlMyItems.getChildren().clear();

        for (Auction auction : auctions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
                Parent card = loader.load();
                Item_Card_Controller cardController = loader.getController();

                cardController.setData(auction);
                cardController.setOwnerView(true);

                activeControllers.add(cardController);
                pnlMyItems.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // HÀM MỞ POPUP NẠP TIỀN (MỚI THÊM)
    // =========================================================
    @FXML
    public void handleOpenNapTienPopup(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Nap_Tien.fxml");
            if (fxmlLocation == null) {
                System.err.println("Không tìm thấy file Nap_Tien.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Truyền dữ liệu User sang màn hình nạp tiền
            Nap_Tien_Controller controller = loader.getController();
            if (Session.currentUser != null) {
                controller.setUserData(Session.currentUser);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setTitle("Nạp Tiền");

            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            // Sau khi nạp xong, có thể load lại danh sách nếu cần cập nhật hiển thị liên quan đến ví
            loadMyProducts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== NAVIGATION (Điều hướng) ==================

    @FXML
    public void handleQuayLaiTrangChu(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm");
    }

    @FXML
    public void chuyenSangdanhsachtheodoi(ActionEvent event) {
        // Bạn có thể dùng hàm popup ở trên hoặc chuyển cảnh tùy ý
        handleOpenNapTienPopup(event);
    }

    @FXML
    public void handleGoToLichSu(ActionEvent event) {
        switchScene(event, "Phien_Da_Tham_Gia.fxml", "Lịch sử");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMyProducts();
    }
}