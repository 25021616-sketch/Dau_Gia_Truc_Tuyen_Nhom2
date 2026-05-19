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
        Member currentUser = (Member) Session.currentUser;
        if (currentUser == null) {
            System.err.println("Chưa đăng nhập!");
            return;
        }

        // Chạy ngầm (Background Thread) để không làm đơ nút bấm
        new Thread(() -> {
            try {
                List<Auction> myAuctions = auctionService.getAuctionsBySeller(currentUser.getId());
                
                // Tải trước FXML trong Background Thread để không làm đơ UI
                List<Parent> cards = new ArrayList<>();
                List<Item_Card_Controller> controllers = new ArrayList<>();
                
                for (Auction auction : myAuctions) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
                    Parent card = loader.load(); // Load FXML tốn thời gian nên để ở thread ngoài
                    Item_Card_Controller cardController = loader.getController();
                    
                    cards.add(card);
                    controllers.add(cardController);
                }

                // Cập nhật UI và gán dữ liệu phải được đẩy về JavaFX Thread
                javafx.application.Platform.runLater(() -> hienThiDanhSach(myAuctions, cards, controllers));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void hienThiDanhSach(List<Auction> auctions, List<Parent> cards, List<Item_Card_Controller> controllers) {
        if (pnlMyItems == null) return;

        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }
        activeControllers.clear();
        pnlMyItems.getChildren().clear();

        for (int i = 0; i < auctions.size(); i++) {
            try {
                Parent card = cards.get(i);
                Item_Card_Controller cardController = controllers.get(i);

                cardController.setData(auctions.get(i));
                cardController.setOwnerView(true);

                activeControllers.add(cardController);
                pnlMyItems.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // HÀM MỞ POPUP NẠP TIỀN (SỬ DỤNG HÀM CHUẨN TỪ BASE)
    // =========================================================
    @Override
    protected void onNapTienSuccess() {
        loadMyProducts();
    }

    // =========================================================
    // DỌN DẸP TÀI NGUYÊN KHI ĐỔI MÀN HÌNH (Fix Memory/CPU Leak)
    // =========================================================
    @Override
    protected void cleanup() {
        activeControllers.forEach(ctrl -> {
            if (ctrl != null) ctrl.stopTimeline();
        });
        // Không clear activeControllers ở đây để tránh lỗi đồng bộ khi tải lại/quay lại cache
    }

    @Override
    protected void onResume() {
        loadMyProducts(); // Cập nhật danh sách sản phẩm của tôi khi quay lại từ Cache
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