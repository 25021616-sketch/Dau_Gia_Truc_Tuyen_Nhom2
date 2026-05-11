package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
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

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;

public class Item_Card_Controller extends Base_Admin_Controller {

    @FXML
    private ImageView imgSanPham;
    @FXML
    private Label lblLoaiSP;
    @FXML
    private Label lblTenSP;
    @FXML
    private Label lblThoiGian;
    @FXML
    private Label lblGiaHienTai;
    @FXML
    private Label lblBadgeTrangThai;
    @FXML
    private Button btnDatGia;

    private Timeline timeline;
    private Auction auction;
    private boolean isOwnerView = false;

    // Các hằng số (Constants) để dễ quản lý style
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("$#,###");
    private static final String COLOR_UPCOMING = "#F39C12";
    private static final String COLOR_ONGOING = "#D32F2F";
    private static final String STYLE_ENDED = "-fx-text-fill: #7F8C8D; -fx-font-weight: bold;";
    private static final String STYLE_OWNER_BADGE = "-fx-background-color: #27ae60; -fx-text-fill: white;";

    public void setOwnerView(boolean isOwner) {
        this.isOwnerView = isOwner;
        if (this.isOwnerView) {
            btnDatGia.setVisible(false); // Ẩn nút đặt giá
            btnDatGia.setManaged(false); // Ẩn nút đặt giá
            lblBadgeTrangThai.setText("SẢN PHẨM CỦA TÔI");
            lblBadgeTrangThai.setStyle(STYLE_OWNER_BADGE);
        }
    }

    public void setData(Auction auction) {
        this.auction = auction;
        Item item = auction.getItem();

        populateTextData(item);
        populateImageData(item);

        imgSanPham.setEffect(null);
        startCountdown();
    }

    private void populateTextData(Item item) {
        lblTenSP.setText(item.getTenSanPham());
        lblLoaiSP.setText(item.getLoaiSanPham().toUpperCase());
        lblGiaHienTai.setText(PRICE_FORMAT.format(auction.getCurrentPrice()));
    }

    private void populateImageData(Item item) {
        String path = item.getImagePath();
        if (path == null || path.isEmpty()) return;

        try {
            imgSanPham.setImage(new Image(path, true));
            Rectangle clip = new Rectangle(imgSanPham.getFitWidth(), imgSanPham.getFitHeight());
            clip.setArcWidth(30);
            clip.setArcHeight(30);
            imgSanPham.setClip(clip);
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh: " + e.getMessage());
        }
    }

    private void startCountdown() {
        stopTimeline(); // Tái sử dụng method stopTimeline

        timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> handleTimelineTick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void handleTimelineTick() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end = auction.getEndTime();

        if (now.isBefore(start)) {
            handleUpcomingState(now, start);
        } else if (now.isBefore(end)) {
            handleOngoingState(now, end);
        } else {
            handleEndedState();
        }
    }

    private void handleUpcomingState(LocalDateTime now, LocalDateTime start) {
        updateUIState("SẮP DIỄN RA", COLOR_UPCOMING);
        long sec = Duration.between(now, start).toSeconds();
        lblThoiGian.setText(formatDuration(sec));

        if (!isOwnerView) {
            updateButtonState("CHỜ ĐỢI", true);
        }
    }

    private void handleOngoingState(LocalDateTime now, LocalDateTime end) {
        updateUIState("ĐANG DIỄN RA", COLOR_ONGOING);
        long sec = Duration.between(now, end).toSeconds();
        lblThoiGian.setText(formatDuration(sec));
        lblBadgeTrangThai.setVisible(true);

        if (!isOwnerView) {
            updateButtonState("ĐẶT GIÁ", false);
        }
    }

    private void handleEndedState() {
        lblThoiGian.setText("HẾT GIỜ");
        lblThoiGian.setStyle(STYLE_ENDED);

        if (!isOwnerView) {
            updateButtonState("KẾT THÚC", true);
        }

        applyGrayscaleEffect();
        stopTimeline();
    }

    private void updateButtonState(String text, boolean disable) {
        btnDatGia.setText(text);
        btnDatGia.setDisable(disable);
    }

    private void applyGrayscaleEffect() {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        imgSanPham.setEffect(grayscale);
    }

    private void updateUIState(String status, String color) {
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private String formatDuration(long secondsTotal) {
        if (secondsTotal < 0)
            return "00 : 00 : 00";
        long hours = secondsTotal / 3600;
        long minutes = (secondsTotal % 3600) / 60;
        long seconds = secondsTotal % 60;
        return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }

    public void stopTimeline() {
        if (timeline != null)
            timeline.stop();
    }

    @FXML
    private void handleDatGia(ActionEvent event) {
        if (isOwnerView)
            return;

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartTime()) && now.isBefore(auction.getEndTime())) {
            switchSceneWithData(event, "Phien_Dau_Gia.fxml",
                    "Đấu giá: " + auction.getItem().getTenSanPham(), this.auction);
        }
    }
}