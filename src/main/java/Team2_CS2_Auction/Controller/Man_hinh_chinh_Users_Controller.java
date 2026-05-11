package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
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

public class Man_hinh_chinh_Users_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlItems;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDataFromServer();
    }

    /**
     * Lấy dữ liệu từ Service và hiển thị
     */
    private void loadDataFromServer() {
        try {
            // Lấy toàn bộ sản phẩm đang đấu giá
            List<Auction> list = auctionService.getActiveAuctions();

            // Render lên giao diện (false vì đây là trang chủ, cần hiện nút Đặt giá)
            renderAuctionList(list, false);

        } catch (Exception e) {
            System.err.println("Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * LOGIC DÙNG CHUNG: Nạp card vào FlowPane
     * @param auctions: Danh sách sản phẩm
     * @param isOwnerView: true nếu là trang "Của tôi" (ẩn nút đặt giá)
     */
    public void renderAuctionList(List<Auction> auctions, boolean isOwnerView) {
        if (pnlItems == null) return;

        // 1. Dọn dẹp card cũ & dừng timeline để tránh tốn RAM
        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }
        activeControllers.clear();
        pnlItems.getChildren().clear();

        // 2. Duyệt danh sách để nạp Card
        for (Auction auction : auctions) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml")
                );

                Parent card = loader.load();
                Item_Card_Controller cardController = loader.getController();

                // Đổ dữ liệu vào card
                cardController.setData(auction);

                // GỌI HÀM NÀY: Để ẩn nút đặt giá nếu là chủ sở hữu
                cardController.setOwnerView(isOwnerView);

                activeControllers.add(cardController);
                pnlItems.getChildren().add(card);

            } catch (Exception e) {
                String name = (auction.getItem() != null) ? auction.getItem().getTenSanPham() : "Unknown";
                System.err.println("Lỗi nạp card [" + name + "]: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilterAll(ActionEvent event) {
        loadDataFromServer();
    }

    // ================== ĐIỀU HƯỚNG MÀN HÌNH ==================

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm");
    }

    @FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) {
        switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi");
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
    public void handleGoToDangNhap(ActionEvent event) {
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
    }
}