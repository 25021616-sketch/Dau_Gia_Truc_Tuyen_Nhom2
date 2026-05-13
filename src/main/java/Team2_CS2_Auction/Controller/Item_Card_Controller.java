package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Session.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

public class Item_Card_Controller extends Base_Admin_Controller {

    @FXML private ImageView imgSanPham;
    @FXML private Label lblLoaiSP;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGian;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblBadgeTrangThai;
    @FXML private Button btnDatGia;

    private Timeline timeline;
    private Auction auction;
    private boolean isOwnerView = false;

    // Cấu hình màu sắc và định dạng
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("$#,###");
    private static final String COLOR_WAITING = "#F39C12"; // Cam
    private static final String COLOR_ONGOING = "#27ae60"; // Xanh lá
    private static final String COLOR_ENDED = "#7F8C8D";   // Xám
    private static final String STYLE_OWNER_BADGE = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;";

    public void setData(Auction auction) {
        if (auction == null) return;
        this.auction = auction;
        Item item = auction.getItem();

        // 1. Đổ dữ liệu cơ bản
        lblTenSP.setText(item.getTenSanPham());
        lblLoaiSP.setText(item.getLoaiSanPham().toUpperCase());
        lblGiaHienTai.setText(PRICE_FORMAT.format(auction.getCurrentPrice()));

        populateImageData(item);

        // 2. Kiểm tra quyền sở hữu
        checkOwnerShip();

        // 3. Khởi động đồng hồ đếm ngược
        startCountdown();
    }

    private void checkOwnerShip() {
        if (Session.currentUser instanceof Member currentUser) {
            if (auction.getSeller() != null && auction.getSeller().getId() == currentUser.getId()) {
                this.isOwnerView = true;
                lblBadgeTrangThai.setText("SẢN PHẨM CỦA TÔI");
                lblBadgeTrangThai.setStyle(STYLE_OWNER_BADGE);

                btnDatGia.setText("QUẢN LÝ SẢN PHẨM");
                btnDatGia.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 0 0 15 15;");
            }
        }
    }

    private void populateImageData(Item item) {
        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                imgSanPham.setImage(new Image(path, true));
                Rectangle clip = new Rectangle(240, 150);
                clip.setArcWidth(30); clip.setArcHeight(30);
                imgSanPham.setClip(clip);
            } catch (Exception e) {
                System.err.println("Lỗi nạp ảnh: " + e.getMessage());
            }
        }
    }

    private void startCountdown() {
        stopTimeline();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeLogic()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimeLogic() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end = auction.getEndTime();

        if (now.isBefore(start)) {
            // Trạng thái: Chờ đợi (Sắp diễn ra)
            updateUIStatus("SẮP DIỄN RA", COLOR_WAITING, now, start);
            btnDatGia.setDisable(true);
            btnDatGia.setText("CHƯA BẮT ĐẦU");
        } else if (now.isBefore(end)) {
            // Trạng thái: Đang diễn ra
            updateUIStatus("ĐANG DIỄN RA", COLOR_ONGOING, now, end);
            if (!isOwnerView) {
                btnDatGia.setDisable(false);
                btnDatGia.setText("XEM CHI TIẾT");
            }
        } else {
            // Trạng thái: Kết thúc
            handleEndedState();
        }
    }

    private void updateUIStatus(String statusText, String colorCode, LocalDateTime now, LocalDateTime target) {
        // Cập nhật Badge (Nếu là chủ sở hữu thì giữ nguyên nhãn "SẢN PHẨM CỦA TÔI")
        if (!isOwnerView) {
            lblBadgeTrangThai.setText(statusText);
            lblBadgeTrangThai.setStyle("-fx-background-color: " + colorCode + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
        }

        // Cập nhật thời gian và màu chữ thời gian
        long sec = java.time.Duration.between(now, target).toSeconds();
        lblThoiGian.setText(formatDuration(sec));
        lblThoiGian.setStyle("-fx-text-fill: " + colorCode + "; -fx-font-weight: bold;");
    }

    private String formatDuration(long secondsTotal) {
        if (secondsTotal <= 0) return "00 : 00 : 00";
        long h = secondsTotal / 3600;
        long m = (secondsTotal % 3600) / 60;
        long s = secondsTotal % 60;
        return String.format("%02d : %02d : %02d", h, m, s);
    }

    private void handleEndedState() {
        lblThoiGian.setText("HẾT GIỜ");
        lblThoiGian.setStyle("-fx-text-fill: " + COLOR_ENDED + "; -fx-font-weight: bold;");

        if (!isOwnerView) {
            lblBadgeTrangThai.setText("KẾT THÚC");
            lblBadgeTrangThai.setStyle("-fx-background-color: " + COLOR_ENDED + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
            btnDatGia.setText("ĐÃ ĐÓNG");
            btnDatGia.setDisable(true);
        }

        applyGrayscaleEffect();
        stopTimeline();
    }

    private void applyGrayscaleEffect() {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        imgSanPham.setEffect(grayscale);
    }

    public void stopTimeline() {
        if (timeline != null) timeline.stop();
    }

    @FXML
    private void handleDatGia(ActionEvent event) {
        if (isOwnerView) {
            System.out.println("DEBUG: Mở màn hình quản lý sản phẩm");
            return;
        }
        // Chỉ chuyển cảnh nếu phiên đang diễn ra
        if (LocalDateTime.now().isAfter(auction.getStartTime()) && LocalDateTime.now().isBefore(auction.getEndTime())) {
            switchSceneWithData(event, "Phien_Dau_Gia.fxml", "Đấu giá", this.auction);
        }
    }

    public void setOwnerView(boolean isOwnerView) {
        this.isOwnerView = isOwnerView;
    }
}