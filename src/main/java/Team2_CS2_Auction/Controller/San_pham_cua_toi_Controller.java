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
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class San_pham_cua_toi_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlMyItems; // Phải khớp với fx:id trong file san_pham_cua_toi.fxml

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMyProducts();
    }

    /**
     * Lấy danh sách sản phẩm do chính người dùng hiện tại đăng
     */
    private void loadMyProducts() {
        try {
            // 1. Lấy thông tin người dùng hiện tại từ Session
            Member currentUser = (Member) Session.currentUser;

            if (currentUser != null) {
                // 2. Gọi Service để lấy danh sách sản phẩm của riêng User này
                // Hàm này lấy dữ liệu từ Repository (đã bao gồm giá hiện tại mới nhất)
                List<Auction> myAuctions = auctionService.getAuctionsBySeller(currentUser.getId());

                // 3. Hiển thị lên giao diện
                hienThiDanhSach(myAuctions);
            } else {
                System.err.println("Chưa đăng nhập, không thể tải sản phẩm cá nhân!");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi load sản phẩm cá nhân: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Render các card sản phẩm và ẨN nút đặt giá
     */

    private void hienThiDanhSach(List<Auction> auctions) {
        if (pnlMyItems == null) return;

        // Dọn dẹp card cũ
        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }
        activeControllers.clear();
        pnlMyItems.getChildren().clear();

        // Nạp card mới
        for (Auction auction : auctions) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml")
                );

                Parent card = loader.load();
                Item_Card_Controller cardController = loader.getController();

                // Đổ dữ liệu sản phẩm (Giá sẽ được cập nhật mới nhất từ Database)
                cardController.setData(auction);

                // QUAN TRỌNG: Kích hoạt chế độ "Chủ sở hữu" để ẩn nút Đặt giá
                cardController.setOwnerView(true);

                activeControllers.add(cardController);
                pnlMyItems.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Lỗi nạp card: " + e.getMessage());
            }
        }
    }

    // ================== NAVIGATION (Điều hướng) ==================

    @FXML
    public void handleQuayLaiTrangChu(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ Đấu giá");
    }

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm mới");
    }

    @FXML
    public void chuyenSangdanhsachtheodoi(ActionEvent event) {
        switchScene(event, "danh_sach_theo_doi_San_Pham.fxml", "Danh sách theo dõi");
    }

    @FXML
    public void handleGoToLichSu(ActionEvent event) {
        switchScene(event, "Lich_su_giao_dich.fxml", "Lịch sử giao dịch");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMyProducts();
    }
}