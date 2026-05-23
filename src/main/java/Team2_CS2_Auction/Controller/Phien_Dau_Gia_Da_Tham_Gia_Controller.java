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
        if (Session.currentUser == null) {
            System.err.println("DEBUG CTRL: Session.currentUser bị NULL!");
            return;
        }
        
        int currentUserId = Session.currentUser.getId();
        System.out.println("DEBUG CTRL: Đang tải phiên cho User ID: " + currentUserId);

        // Chạy ngầm (Background Thread) để không làm đơ nút bấm
        new Thread(() -> {
            try {
                List<Auction> list = auctionService.getAuctionsByBidder(currentUserId);
                System.out.println("DEBUG CTRL: Service trả về " + (list != null ? list.size() : "NULL") + " phiên.");
                
                List<Parent> cards = new ArrayList<>();
                List<Item_Card_Controller> controllers = new ArrayList<>();
                
                if (list != null) {
                    for (Auction auction : list) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
                        Parent card = loader.load();
                        Item_Card_Controller cardController = loader.getController();
                        
                        cards.add(card);
                        controllers.add(cardController);
                    }
                }
                
                // Cập nhật UI phải được đẩy về JavaFX Thread
                javafx.application.Platform.runLater(() -> renderAuctionList(list, cards, controllers));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderAuctionList(List<Auction> auctions, List<Parent> cards, List<Item_Card_Controller> controllers) {
        if (pnlJoinedItems == null) return;

        // Dọn dẹp card cũ để giải phóng bộ nhớ
        activeControllers.forEach(ctrl -> { if(ctrl != null) ctrl.stopTimeline(); });
        activeControllers.clear();
        pnlJoinedItems.getChildren().clear();

        if (auctions == null || auctions.isEmpty()) {
            System.out.println("DEBUG UI: Không có dữ liệu để hiển thị.");
            return;
        }

        for (int i = 0; i < auctions.size(); i++) {
            try {
                Parent card = cards.get(i);
                Item_Card_Controller cardController = controllers.get(i);
                
                if (cardController != null) {
                    cardController.setData(auctions.get(i));
                    activeControllers.add(cardController);
                    pnlJoinedItems.getChildren().add(card);
                }
            } catch (Exception e) {
                System.err.println("DEBUG UI: Lỗi nạp Card: " + e.getMessage());
            }
        }
    }

    // Dọn dẹp tài nguyên khi đổi màn hình (chặn CPU/Memory leak)
    @Override
    protected void cleanup() {
        activeControllers.forEach(ctrl -> {
            if (ctrl != null) ctrl.stopTimeline();
        });
        // Không clear activeControllers ở đây để tránh lỗi đồng bộ khi tải lại/quay lại cache
    }

    @Override
    protected void onResume() {
        loadDataPhienDaThamGia(); // Cập nhật danh sách phiên đấu giá đã tham gia khi quay lại từ Cache
    }

    @FXML public void handleQuayLaiTrangChu(ActionEvent event) { switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính"); }
    @FXML public void handleGoTothemsanpham(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }
    @FXML public void handleGoToSanPhamCuaToi(ActionEvent event) { switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi"); }

    @Override
    protected void onNapTienSuccess() {
        super.onNapTienSuccess();
        loadDataPhienDaThamGia();
    }
}