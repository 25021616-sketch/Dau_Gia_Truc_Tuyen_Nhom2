package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Networking.NetworkManager;
import Team2_CS2_Auction.Networking.NetworkMessage;
import Team2_CS2_Auction.Networking.NetworkListener;
import Team2_CS2_Auction.Networking.GsonUtil;
import Team2_CS2_Auction.Session.Session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Phien_Dau_Gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private ImageView productImage;
    @FXML private Label currentBidLabel;
    @FXML private Label targetPriceLabel;
    @FXML private Label lblTenSanPham;
    @FXML private Label lblMoTa;
    @FXML private Label lblThoiGian;
    @FXML private Spinner<Integer> stepSpinner;
    @FXML private TextField bidStepField;
    @FXML private LineChart<String, Number> bidHistoryChart;

    private XYChart.Series<String, Number> bidSeries;
    private Timeline timeline;
    private ScheduledExecutorService pollScheduler;

    private Auction currentAuction;
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private NetworkListener networkListener;
    private final NetworkManager nm = NetworkManager.getInstance();
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTargetPrice());

        // Khởi tạo đồ thị
        bidSeries = new XYChart.Series<>();
        bidSeries.setName("Giá ($)");
        if (bidHistoryChart != null) {
            bidHistoryChart.getData().add(bidSeries);
        }

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

                            if (currentAuction != null && currentAuction.getAuctionId().equals(rcvAuctionId)) {
                                double newPrice = payload.get("newPrice").getAsDouble();

                                currentAuction.setCurrentPrice(newPrice);
                                currentPrice = newPrice;
                                currentBidLabel.setText(formatter.format(newPrice));
                                updateTargetPrice();

                                // Cập nhật đồ thị Realtime
                                if (bidSeries != null) {
                                    String timeNow = java.time.LocalDateTime.now()
                                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
                                    timeNow = getUniqueTimeStr(timeNow);
                                    bidSeries.getData().add(new XYChart.Data<>(timeNow, newPrice));

                                    if (bidSeries.getData().size() > 10) {
                                        bidSeries.getData().remove(0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("BID_FAILED".equals(message.getAction())) {
                        new Alert(Alert.AlertType.ERROR, message.getPayload()).show();
                    }
                });
            }

            @Override
            public void onConnectionError() {
            }
        };
        nm.addListener(networkListener);
    }

    public void setAuctionData(Auction auction) {
        if (auction == null) return;

        this.currentAuction = auction;
        Item item = auction.getItem();
        this.currentPrice = auction.getCurrentPrice();

        if (lblTenSanPham != null) lblTenSanPham.setText(item.getTenSanPham());
        if (lblMoTa != null) lblMoTa.setText(item.getMoTa());
        currentBidLabel.setText(formatter.format(currentPrice));

        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                productImage.setImage(new Image(path, true));
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh sản phẩm: " + e.getMessage());
            }
        }

        double buocGiaTuAuction = auction.getStepPrice();
        if (bidStepField != null) {
            bidStepField.setText(formatter.format(buocGiaTuAuction));
            bidStepField.setDisable(true);
        }

        // Chạy luồng ngầm để load lịch sử đấu giá từ Database
        new Thread(() -> {
            try {
                List<Bid> history = auctionService.getBidHistory(auction.getAuctionId());
                Platform.runLater(() -> {
                    if (bidSeries != null) {
                        bidSeries.getData().clear();

                        if (history.isEmpty()) {
                            String timeNow = java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
                            bidSeries.getData().add(new XYChart.Data<>(timeNow, currentPrice));
                        } else {
                            int startIdx = Math.max(0, history.size() - 10);
                            for (int i = startIdx; i < history.size(); i++) {
                                Bid b = history.get(i);
                                String timeStr = b.getTime()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
                                timeStr = getUniqueTimeStr(timeStr);
                                bidSeries.getData().add(new XYChart.Data<>(timeStr, b.getAmount()));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        updateTargetPrice();
        startCountdown();
        updateBidButtonStateForSeller();
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải seller không.
     * Nếu là seller: disable nút "Đặt giá" và show cảnh báo
     */
    private void updateBidButtonStateForSeller() {
        if (currentAuction == null || Session.currentUser == null) return;

        int currentUserId = Session.currentUser.getId();
        int sellerId = (currentAuction.getSeller() != null) ? currentAuction.getSeller().getId() : -1;

        if (currentUserId == sellerId) {
            System.out.println("✓ Đây là sản phẩm của bạn — disable nút đặt giá");

            if (lblMoTa != null) {
                String originalMoTa = currentAuction.getItem().getMoTa();
                lblMoTa.setText("[⚠️ BẠN LÀ CHỦ SỞ HỮU - KHÔNG ĐẶT GIÁ]\n\n" + originalMoTa);
                lblMoTa.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }

    private void startCountdown() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeLogic()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateTimeLogic();
    }

    private void updateTimeLogic() {
        if (currentAuction == null) return;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = currentAuction.getStartTime();
        LocalDateTime end = currentAuction.getEndTime();

        if (now.isBefore(start)) {
            updateTimeDisplay(now, start, "#F39C12", "SẮP BẮT ĐẦU: ");
        } else if (now.isBefore(end)) {
            updateTimeDisplay(now, end, "#27ae60", "CÒN LẠI: ");
        } else {
            lblThoiGian.setText("ĐÃ KẾT THÚC");
            lblThoiGian.setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold; -fx-font-size: 18px;");
            if (timeline != null) timeline.stop();
        }
    }

    private void updateTimeDisplay(LocalDateTime now, LocalDateTime target, String color, String prefix) {
        long sec = java.time.Duration.between(now, target).toSeconds();
        if (sec < 0) sec = 0;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        lblThoiGian.setText(String.format("%s%02d : %02d : %02d", prefix, h, m, s));
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");
    }

    private String getUniqueTimeStr(String timeStr) {
        if (bidSeries == null) return timeStr;
        String uniqueStr = timeStr;
        boolean exists;
        do {
            exists = false;
            for (XYChart.Data<String, Number> data : bidSeries.getData()) {
                if (data.getXValue().equals(uniqueStr)) {
                    exists = true;
                    uniqueStr += "\u200A";
                    break;
                }
            }
        } while (exists);
        return uniqueStr;
    }

    private void updateTargetPrice() {
        if (currentAuction == null) return;
        try {
            int n = stepSpinner.getValue();
            double d = currentAuction.getStepPrice();
            double targetPrice = currentPrice + (n * d);
            targetPriceLabel.setText(formatter.format(targetPrice));
        } catch (Exception e) {
            targetPriceLabel.setText(formatter.format(currentPrice));
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (currentAuction == null) return;
        try {
            if (!(Session.currentUser instanceof Member)) {
                throw new Exception("Chỉ Member mới có thể đặt giá!");
            }

            Member currentUser = (Member) Session.currentUser;

            if (currentAuction.getSeller() != null &&
                    currentUser.getId() == currentAuction.getSeller().getId()) {
                new Alert(Alert.AlertType.WARNING,
                        "Bạn không thể đặt giá cho sản phẩm của chính mình!").show();
                return;
            }

            double finalPrice = Double.parseDouble(targetPriceLabel.getText().replaceAll("[^\\d]", ""));

            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getAuctionId());
            payload.addProperty("bidAmount", finalPrice);
            payload.addProperty("userId", currentUser.getId());

            if (!nm.isConnected()) {
                new Alert(Alert.AlertType.ERROR, "Mất kết nối tới Server!").show();
                return;
            }

            nm.send("PLACE_BID", payload);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        if (timeline != null) timeline.stop();
        if (pollScheduler != null) pollScheduler.shutdownNow();
        if (networkListener != null) {
            nm.removeListener(networkListener);
        }
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    @FXML
    private void handleActivateAutoBid() {
        new Alert(Alert.AlertType.INFORMATION, "Tính năng đấu giá tự động sẽ sớm ra mắt!").show();
    }
}