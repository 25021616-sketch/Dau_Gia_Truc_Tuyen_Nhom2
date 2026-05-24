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

    @FXML private TextField autoStepsCountField;
    @FXML private TextField autoLimitField;
    @FXML private Button btnActivateAutoBid;

    private XYChart.Series<String, Number> bidSeries;
    private Timeline timeline;

    private Auction currentAuction;
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private boolean isAutoBidActive = false;

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

        // Thiết lập Listener để nhận tin nhắn Broadcast từ Server
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
                            
                            // Chỉ cập nhật nếu tin nhắn thuộc về món hàng đang xem
                            if (currentAuction != null && currentAuction.getAuctionId().equals(rcvAuctionId)) {
                                double newPrice = payload.get("newPrice").getAsDouble();
                                
                                // Cập nhật UI
                                currentAuction.setCurrentPrice(newPrice);
                                currentPrice = newPrice;
                                currentBidLabel.setText(formatter.format(newPrice));
                                updateTargetPrice();

                                // Cập nhật đồ thị Realtime
                                if (bidSeries != null) {
                                    String timeNow = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
                                    timeNow = getUniqueTimeStr(timeNow);
                                    bidSeries.getData().add(new XYChart.Data<>(timeNow, newPrice));
                                    // Giới hạn hiển thị 20 điểm gần nhất cho đỡ rối
                                    if (bidSeries.getData().size() > 20) {
                                        bidSeries.getData().remove(0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("BID_FAILED".equals(message.getAction())) {
                        new Alert(Alert.AlertType.ERROR, message.getPayload()).show();
                    } else if ("AUTO_BID_STATUS_RESP".equals(message.getAction())) {
                        try {
                            String pl = message.getPayload();
                            if (pl != null && !pl.isEmpty()) {
                                Gson gson = GsonUtil.getGson();
                                JsonObject ab = gson.fromJson(pl, JsonObject.class);
                                
                                isAutoBidActive = true;
                                if (autoStepsCountField != null) {
                                    autoStepsCountField.setText(String.valueOf(ab.get("stepMultiplier").getAsInt()));
                                    autoStepsCountField.setDisable(true);
                                }
                                if (autoLimitField != null) {
                                    autoLimitField.setText(formatter.format(ab.get("maxLimit").getAsDouble()));
                                    autoLimitField.setDisable(true);
                                }
                                if (btnActivateAutoBid != null) {
                                    btnActivateAutoBid.setText("HỦY KÍCH HOẠT");
                                    btnActivateAutoBid.setStyle("-fx-border-color: #D32F2F; -fx-border-radius: 5; -fx-background-color: transparent; -fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-cursor: hand;");
                                }
                            } else {
                                isAutoBidActive = false;
                                if (autoStepsCountField != null) {
                                    autoStepsCountField.setText("1");
                                    autoStepsCountField.setDisable(false);
                                }
                                if (autoLimitField != null) {
                                    autoLimitField.setText("");
                                    autoLimitField.setDisable(false);
                                }
                                if (btnActivateAutoBid != null) {
                                    btnActivateAutoBid.setText("KÍCH HOẠT");
                                    btnActivateAutoBid.setStyle("-fx-border-color: #1A237E; -fx-border-radius: 5; -fx-background-color: transparent; -fx-text-fill: #1A237E; -fx-font-weight: bold; -fx-cursor: hand;");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("AUTO_BID_SUCCESS".equals(message.getAction())) {
                        isAutoBidActive = true;
                        if (autoStepsCountField != null) autoStepsCountField.setDisable(true);
                        if (autoLimitField != null) autoLimitField.setDisable(true);
                        if (btnActivateAutoBid != null) {
                            btnActivateAutoBid.setText("HỦY KÍCH HOẠT");
                            btnActivateAutoBid.setStyle("-fx-border-color: #D32F2F; -fx-border-radius: 5; -fx-background-color: transparent; -fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-cursor: hand;");
                        }
                        showStyledAlert("Thành công", "Đã kích hoạt ủy quyền đấu thầu tự động thành công!", Alert.AlertType.INFORMATION);
                    } else if ("AUTO_BID_CANCELLED".equals(message.getAction())) {
                        isAutoBidActive = false;
                        if (autoStepsCountField != null) {
                            autoStepsCountField.setDisable(false);
                        }
                        if (autoLimitField != null) {
                            autoLimitField.setDisable(false);
                            autoLimitField.setText("");
                        }
                        if (btnActivateAutoBid != null) {
                            btnActivateAutoBid.setText("KÍCH HOẠT");
                            btnActivateAutoBid.setStyle("-fx-border-color: #1A237E; -fx-border-radius: 5; -fx-background-color: transparent; -fx-text-fill: #1A237E; -fx-font-weight: bold; -fx-cursor: hand;");
                        }
                        String details = message.getPayload();
                        if (details != null && !details.isEmpty() && !details.contains("Đã hủy")) {
                            showStyledAlert("Thông báo", details, Alert.AlertType.WARNING);
                        } else {
                            showStyledAlert("Thông báo", "Đã tắt đấu giá tự động.", Alert.AlertType.INFORMATION);
                        }
                    } else if ("AUTO_BID_FAILED".equals(message.getAction())) {
                        showStyledAlert("Thất bại", message.getPayload(), Alert.AlertType.ERROR);
                    } else if ("AUTO_BID_PLACED".equals(message.getAction())) {
                        try {
                            double amount = Double.parseDouble(message.getPayload());
                            showStyledAlert("Đấu giá tự động", "Hệ thống đã tự động đặt thầu mới thành công ở mức $" + formatter.format(amount) + " thay cho bạn!", Alert.AlertType.INFORMATION);
                        } catch (Exception e) {
                            showStyledAlert("Đấu giá tự động", "Hệ thống đã tự động đặt thầu thành công thay cho bạn!", Alert.AlertType.INFORMATION);
                        }
                    }
                });
            }

            @Override
            public void onConnectionError() {
                // Ignore connection errors here, main listener handles it
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
                            // Nếu chưa có lịch sử, vẽ điểm xuất phát
                            String timeNow = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
                            bidSeries.getData().add(new XYChart.Data<>(timeNow, currentPrice));
                        } else {
                            // Chỉ vẽ tối đa 20 điểm cuối cùng để biểu đồ không bị nén quá chật
                            int startIdx = Math.max(0, history.size() - 20);
                            for (int i = startIdx; i < history.size(); i++) {
                                Bid b = history.get(i);
                                String timeStr = b.getTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM\nHH:mm:ss"));
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

        // Lấy cấu hình Đấu giá tự động của người dùng nếu có
        if (Session.currentUser != null) {
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", auction.getAuctionId());
            payload.addProperty("userId", Session.currentUser.getId());
            nm.send("GET_AUTO_BID_STATUS", payload);
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

    // Hàm phụ: Giúp tạo ra các chuỗi thời gian duy nhất (tránh lỗi Duplicate Category của JavaFX LineChart)
    private String getUniqueTimeStr(String timeStr) {
        if (bidSeries == null) return timeStr;
        String uniqueStr = timeStr;
        boolean exists;
        do {
            exists = false;
            for (XYChart.Data<String, Number> data : bidSeries.getData()) {
                if (data.getXValue().equals(uniqueStr)) {
                    exists = true;
                    uniqueStr += "\u200A"; // Thêm khoảng trắng tàng hình (Hair Space) để JavaFX hiểu là chuỗi mới
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
            Member currentUser = (Member) Team2_CS2_Auction.Session.Session.currentUser;
            double finalPrice = Double.parseDouble(targetPriceLabel.getText().replaceAll("[^\\d]", "")); // Lấy số sạch

            // GỬI LỆNH LÊN SERVER QUA SOCKET, KHÔNG CHẠY DATABASE Ở ĐÂY NỮA
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getAuctionId());
            payload.addProperty("bidAmount", finalPrice);
            payload.addProperty("userId", currentUser.getId());

            if (!nm.isConnected()) {
                new Alert(Alert.AlertType.ERROR, "Mất kết nối tới Server!").show();
                return;
            }
            
            nm.send("PLACE_BID", payload);
            // Không cập nhật UI ngay lập tức. Đợi Server Broadcast NEW_BID về thì UI mới cập nhật!
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        if (timeline != null) timeline.stop();
        // Gỡ bỏ Listener khi thoát khỏi trang này để tránh rác bộ nhớ
        if (networkListener != null) {
            nm.removeListener(networkListener);
        }
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    @FXML
    private void handleActivateAutoBid() {
        if (currentAuction == null) return;
        if (Session.currentUser == null) {
            showStyledAlert("Lỗi", "Bạn cần đăng nhập để sử dụng chức năng này!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra an toàn tránh lỗi NullPointerException nếu FXML chưa được đồng bộ/rebuild đúng
        if (autoStepsCountField == null || autoLimitField == null) {
            showStyledAlert("Lỗi đồng bộ", "Giao diện đấu giá tự động chưa được tải đúng. Vui lòng Clean và Rebuild lại dự án trong IntelliJ!", Alert.AlertType.ERROR);
            return;
        }

        if (isAutoBidActive) {
            // Hủy kích hoạt Auto Bid
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getAuctionId());
            payload.addProperty("userId", Session.currentUser.getId());
            nm.send("DEACTIVATE_AUTO_BID", payload);
        } else {
            // Đọc và kiểm tra Số lần bước giá
            String stepStr = autoStepsCountField.getText().trim();
            int stepMult = 1;
            if (!stepStr.isEmpty()) {
                try {
                    stepMult = Integer.parseInt(stepStr);
                    if (stepMult <= 0) {
                        showStyledAlert("Lỗi", "Số lần bước giá phải lớn hơn 0!", Alert.AlertType.ERROR);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showStyledAlert("Lỗi", "Số lần bước giá phải là số nguyên hợp lệ!", Alert.AlertType.ERROR);
                    return;
                }
            }

            // Đọc và kiểm tra Hạn mức tối đa
            String limitStr = autoLimitField.getText().trim().replaceAll("[^\\d\\.]", ""); // Lấy số
            if (limitStr.isEmpty()) {
                showStyledAlert("Lỗi", "Vui lòng nhập giới hạn tối đa ($)!", Alert.AlertType.ERROR);
                return;
            }

            double maxLimit;
            try {
                maxLimit = Double.parseDouble(limitStr);
            } catch (NumberFormatException e) {
                showStyledAlert("Lỗi", "Giới hạn tối đa phải là số hợp lệ!", Alert.AlertType.ERROR);
                return;
            }

            // Logic kiểm tra xem hạn mức có đủ cho mức nhảy đầu tiên không
            double nextBidPrice = currentPrice + (stepMult * currentAuction.getStepPrice());
            if (maxLimit < nextBidPrice) {
                showStyledAlert("Lỗi", "Giới hạn tối đa phải lớn hơn hoặc bằng giá thầu tiếp theo dự kiến ($" + formatter.format(nextBidPrice) + ")!", Alert.AlertType.ERROR);
                return;
            }

            // Gửi yêu cầu kích hoạt lên Server
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getAuctionId());
            payload.addProperty("userId", Session.currentUser.getId());
            payload.addProperty("stepMultiplier", stepMult);
            payload.addProperty("maxLimit", maxLimit);
            
            nm.send("ACTIVATE_AUTO_BID", payload);
        }
    }

    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            DialogPane pane = alert.getDialogPane();
            pane.setStyle("-fx-background-color: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
            
            Button okButton = (Button) pane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: #1A237E; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            alert.show();
        });
    }
}