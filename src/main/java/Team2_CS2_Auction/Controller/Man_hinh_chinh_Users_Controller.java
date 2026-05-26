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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    // ===== NOTIFICATION BELL =====
    @FXML private Button btnChuong;
    @FXML private Label lblBadgeChuong;
    @FXML private VBox notificationPopup;
    @FXML private VBox notificationList;
    @FXML private Label lblNoNotifications;

    private int unreadCount = 0;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    private NetworkListener networkListener;
    private final NetworkManager nm = NetworkManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDataFromServer();
        setupNetworkListener();
        updateBalanceDisplay();
    }

    private void setupNetworkListener() {
        // FIX BUG #1: Luôn xóa listener cũ trước khi tạo listener mới
        // Tránh tình trạng listener bị nhân bản mỗi lần onResume() được gọi
        if (networkListener != null) {
            nm.removeListener(networkListener);
            networkListener = null;
        }

        networkListener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                Platform.runLater(() -> {
                    switch (message.getAction()) {
                        case "NEW_BID":
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
                            break;

                        case "PRODUCT_UPDATED":
                            // Sản phẩm mới được duyệt hoặc bị từ chối -> Tải lại danh sách ngay
                            loadDataFromServer();
                            break;

                        case "PRODUCT_DELETED":
                            // Chủ sản phẩm đã xóa hoặc đăng lại phiên mà bạn đã đặt giá
                            try {
                                Gson gson = GsonUtil.getGson();
                                JsonObject payload = gson.fromJson(message.getPayload(), JsonObject.class);
                                String productName = payload.has("productName") ? payload.get("productName").getAsString() : "Sản phẩm";
                                String type = payload.has("type") ? payload.get("type").getAsString() : "DELETED";
                                String notifText = "RELISTED".equals(type)
                                        ? "🔄 Sản phẩm \u201c" + productName + "\u201d đã được đăng lại bởi chủ phiên. Phiên cũ không còn hiệu lực."
                                        : "🔴 Sản phẩm \u201c" + productName + "\u201d đã bị chủ phiên xóa. Phiên đấu giá đã kết thúc.";
                                addNotification(notifText);
                                loadDataFromServer(); // Cập nhật lại danh sách
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        case "BALANCE_UPDATED":
                            // Server báo số dư thay đổi (do đặt giá / auto-bid) -> Cập nhật ngay
                            try {
                                Gson gson = GsonUtil.getGson();
                                JsonObject payload = gson.fromJson(message.getPayload(), JsonObject.class);
                                double newBalance = payload.get("balance").getAsDouble();
                                // Cập nhật Session để các màn hình khác cũng nhất quán
                                if (Team2_CS2_Auction.Session.Session.currentUser != null) {
                                    Team2_CS2_Auction.Session.Session.currentUser.setBalance(newBalance);
                                }
                                updateBalanceDisplay(); // Cập nhật UI ngay lập tức
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        default:
                            break;
                    }
                });
            }

            @Override
            public void onConnectionError() {
            }
        };
        nm.addListener(networkListener);

    }

    @Override
    protected void cleanup() {
        // Xóa listener khi rời màn hình
        if (networkListener != null) {
            nm.removeListener(networkListener);
            networkListener = null;
        }
        // Dừng tất cả các đồng hồ đếm ngược của thẻ sản phẩm
        activeControllers.forEach(ctrl -> {
            if (ctrl != null) ctrl.stopTimeline();
        });
    }

    @Override
    protected void onResume() {
        // Gọi lại khi màn hình thức dậy từ Cache
        setupNetworkListener(); // Mở lại kết nối Socket (đã có guard xóa listener cũ bên trong)
        loadDataFromServer();   // Chạy ngầm fetch data mới và cập nhật UI mượt mà
        updateBalanceDisplay(); // Cập nhật lại số dư mới nhất
    }

    private void loadDataFromServer() {
        // Chạy ngầm (Background Thread) để không làm đơ nút bấm
        new Thread(() -> {
            try {
                List<Auction> list = auctionService.getActiveAuctions();

                // Tải trước FXML trong Background Thread để không làm đơ UI
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
                javafx.application.Platform.runLater(() -> System.err.println("Lỗi load dữ liệu: " + e.getMessage()));
            }
        }).start();
    }

    public void renderAuctionList(List<Auction> auctions, List<Parent> cards, List<Item_Card_Controller> controllers) {
        if (pnlItems == null) return;

        activeControllers.forEach(ctrl -> { if(ctrl != null) ctrl.stopTimeline(); });
        activeControllers.clear();
        pnlItems.getChildren().clear();

        if (auctions == null) return;

        for (int i = 0; i < auctions.size(); i++) {
            try {
                Parent card = cards.get(i);
                Item_Card_Controller cardController = controllers.get(i);

                // setData() tự động phát hiện ownership qua checkOwnership() bên trong,
                // KHÔNG cần gọi setOwnerView() từ ngoài nữa.
                cardController.setData(auctions.get(i));

                activeControllers.add(cardController);
                pnlItems.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // HÀM NẠP TIỀN THÀNH CÔNG
    // =========================================================
    @Override
    protected void onNapTienSuccess() {
        super.onNapTienSuccess();
        loadDataFromServer();
    }

    // ================== CÁC HÀM ĐIỀU HƯỚNG KHÁC ==================
    @FXML public void handleGoTothemsanpham(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }
    @FXML public void handleGoToSanPhamCuaToi(ActionEvent event) { switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi"); }
    @FXML public void handleGoToLichSu(ActionEvent event) { switchScene(event, "Phien_Da_Tham_Gia.fxml", "Lịch sử giao dịch"); }
    @FXML public void handleGoToDangNhap(ActionEvent event) { switchScene(event, "dang_nhap.fxml", "Đăng nhập"); }

    // ===== NOTIFICATION METHODS =====

    private void addNotification(String text) {
        unreadCount++;
        // Tạo một dòng thông báo
        Label notif = new Label(text);
        notif.setWrapText(true);
        notif.setMaxWidth(330);
        notif.setStyle("-fx-padding: 12 16; -fx-border-color: #F5F5F5; -fx-border-width: 0 0 1 0; -fx-font-size: 13px; -fx-text-fill: #212121;");
        if (notificationList != null) {
            notificationList.getChildren().add(0, notif); // Thả vào đầu list (mới nhất trên cùng)
        }
        if (lblNoNotifications != null) {
            lblNoNotifications.setVisible(false);
            lblNoNotifications.setManaged(false);
        }
        // Cập nhật badge
        if (lblBadgeChuong != null) {
            lblBadgeChuong.setText(String.valueOf(unreadCount));
            lblBadgeChuong.setVisible(true);
            lblBadgeChuong.setManaged(true);
        }
    }

    @FXML
    public void handleToggleNotifications(javafx.event.Event event) {
        if (notificationPopup == null) return;
        boolean isVisible = notificationPopup.isVisible();
        notificationPopup.setVisible(!isVisible);
        notificationPopup.setManaged(!isVisible);

        // Reset badge khi mở xem
        if (!isVisible) {
            unreadCount = 0;
            if (lblBadgeChuong != null) {
                lblBadgeChuong.setVisible(false);
                lblBadgeChuong.setManaged(false);
            }
        }
    }

    @FXML
    public void handleClearNotifications(ActionEvent event) {
        if (notificationList != null) notificationList.getChildren().clear();
        if (lblNoNotifications != null) {
            lblNoNotifications.setVisible(true);
            lblNoNotifications.setManaged(true);
        }
        unreadCount = 0;
        if (lblBadgeChuong != null) {
            lblBadgeChuong.setVisible(false);
            lblBadgeChuong.setManaged(false);
        }
    }

    @FXML
    public void handleCloseNotifications(ActionEvent event) {
        if (notificationPopup != null) {
            notificationPopup.setVisible(false);
            notificationPopup.setManaged(false);
        }
        unreadCount = 0;
        if (lblBadgeChuong != null) {
            lblBadgeChuong.setVisible(false);
            lblBadgeChuong.setManaged(false);
        }
    }
}