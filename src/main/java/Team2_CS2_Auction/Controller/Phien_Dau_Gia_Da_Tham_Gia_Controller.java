package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
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

public class Phien_Dau_Gia_Da_Tham_Gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlJoinedItems;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG CTRL: Initialize màn hình...");
        loadDataPhienDaThamGia();
    }

    private void loadDataPhienDaThamGia() {
        try {
            if (Session.currentUser != null) {
                int currentUserId = Session.currentUser.getId();
                System.out.println("DEBUG CTRL: Bắt đầu tải dữ liệu cho User ID: " + currentUserId);

                List<Auction> list = auctionService.getAuctionsByBidder(currentUserId);

                System.out.println("DEBUG CTRL: Service trả về " + (list != null ? list.size() : "NULL") + " phiên.");
                renderAuctionList(list);
            } else {
                System.err.println("DEBUG CTRL: Session.currentUser đang bị NULL! Bạn cần đăng nhập trước.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG CTRL: Lỗi loadData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAuctionList(List<Auction> auctions) {
        if (pnlJoinedItems == null) {
            System.err.println("DEBUG UI: pnlJoinedItems bị NULL! Kiểm tra fx:id trong FXML.");
            return;
        }

        // Dọn dẹp card cũ
        activeControllers.forEach(ctrl -> { if(ctrl != null) ctrl.stopTimeline(); });
        activeControllers.clear();
        pnlJoinedItems.getChildren().clear();

        if (auctions == null || auctions.isEmpty()) {
            System.out.println("DEBUG UI: Danh sách rỗng, không có card nào để vẽ.");
            return;
        }

        for (Auction auction : auctions) {
            try {
                System.out.println("DEBUG UI: Đang tạo Card cho Auction ID: " + auction.getAuctionId());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
                Parent card = loader.load();

                Item_Card_Controller cardController = loader.getController();
                if (cardController != null) {
                    cardController.setData(auction);
                    activeControllers.add(cardController);
                    pnlJoinedItems.getChildren().add(card);
                    System.out.println("DEBUG UI: Đã thêm Card thành công.");
                }
            } catch (Exception e) {
                System.err.println("DEBUG UI: Lỗi load Card FXML: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleQuayLaiTrangChu(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính");
    }

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm");
    }

    @FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) {
        switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi");
    }

    @FXML
    public void handleOpenNapTienPopup(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Nap_Tien.fxml");
            if (fxmlLocation == null) return;

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

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

            loadDataPhienDaThamGia();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}