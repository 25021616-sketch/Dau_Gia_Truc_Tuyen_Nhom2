package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import Team2_CS2_Auction.Networking.NetworkManager;
import Team2_CS2_Auction.Networking.NetworkMessage;
import Team2_CS2_Auction.Networking.NetworkListener;
import Team2_CS2_Auction.Networking.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Man_hinh_chinh_Users_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlItems;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();
    
    private NetworkListener networkListener;
    private final NetworkManager nm = NetworkManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDataFromServer();
        setupNetworkListener();
    }

    private void setupNetworkListener() {
        networkListener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                Platform.runLater(() -> {
                    if ("NEW_BID".equals(message.getAction())) {
                        try {
                            Gson gson = GsonUtil.getGson();
                            JsonObject payload = gson.fromJson(message.getPayload(), JsonObject.class);
                            String rcvAuctionId = payload.get("auctionId").getAsString();
                            double newPrice = payload.get("newPrice").getAsDouble();
                            
                            for (Item_Card_Controller ctrl : activeControllers) {
                                if (ctrl != null && ctrl.getAuction() != null && ctrl.getAuction().getAuctionId().equals(rcvAuctionId)) {
                                    ctrl.updatePrice(newPrice);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onConnectionError() {
            }
        };
        nm.addListener(networkListener);
    }

    private void cleanup() {
        if (networkListener != null) {
            nm.removeListener(networkListener);
        }
    }

    private void loadDataFromServer() {
        // Chạy ngầm (Background Thread) để không làm đơ nút bấm
        new Thread(() -> {
            try {
                List<Auction> list = auctionService.getActiveAuctions();
                // Cập nhật UI phải được đẩy về JavaFX Thread
                javafx.application.Platform.runLater(() -> renderAuctionList(list, false));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> System.err.println("Lỗi load dữ liệu: " + e.getMessage()));
            }
        }).start();
    }

    public void renderAuctionList(List<Auction> auctions, boolean isOwnerView) {
        if (pnlItems == null) return;

        activeControllers.forEach(ctrl -> { if(ctrl != null) ctrl.stopTimeline(); });
        activeControllers.clear();
        pnlItems.getChildren().clear();

        for (Auction auction : auctions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
                Parent card = loader.load();
                Item_Card_Controller cardController = loader.getController();
                cardController.setData(auction);
                cardController.setOwnerView(isOwnerView);
                activeControllers.add(cardController);
                pnlItems.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // HÀM MỞ POPUP NẠP TIỀN (CHỈNH SỬA TẠI ĐÂY)
    // =========================================================
    @FXML
    public void handleOpenNapTienPopup(ActionEvent event) {
        System.out.println("===> DEBUG: Đang mở file Nap_Tien.fxml...");

        try {
            // 1. Sửa đường dẫn đúng tên file của bạn là Nap_Tien.fxml
            URL fxmlLocation = getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Nap_Tien.fxml");

            if (fxmlLocation == null) {
                System.err.println("===> LỖI: Không tìm thấy file Nap_Tien.fxml. Kiểm tra lại thư mục resources!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // 2. LẤY CONTROLLER VÀ TRUYỀN USER (Để hết lỗi Null User)
            Nap_Tien_Controller controller = loader.getController();

            // Lấy user từ Session mà bạn đã lưu lúc đăng nhập
            if (Team2_CS2_Auction.Session.Session.currentUser != null) {
                controller.setUserData(Team2_CS2_Auction.Session.Session.currentUser);
                System.out.println("===> DEBUG: Đã truyền User: " + Team2_CS2_Auction.Session.Session.currentUser.getUsername());
            } else {
                System.err.println("===> LỖI: Chưa có User trong Session!");
            }

            // 3. Hiển thị Popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setTitle("Nạp Tiền");

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait();

            // 4. Cập nhật lại giao diện màn hình chính sau khi đóng popup
            loadDataFromServer();

        } catch (Exception e) {
            System.err.println("===> LỖI KHI MỞ POPUP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================== CÁC HÀM ĐIỀU HƯỚNG KHÁC ==================
    @FXML public void handleGoTothemsanpham(ActionEvent event) { cleanup(); switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }
    @FXML public void handleGoToSanPhamCuaToi(ActionEvent event) { cleanup(); switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi"); }
    @FXML public void handleGoToLichSu(ActionEvent event) { cleanup(); switchScene(event, "Phien_Da_Tham_Gia.fxml", "Lịch sử giao dịch"); }
    @FXML public void handleGoToDangNhap(ActionEvent event) { cleanup(); switchScene(event, "dang_nhap.fxml", "Đăng nhập"); }
}