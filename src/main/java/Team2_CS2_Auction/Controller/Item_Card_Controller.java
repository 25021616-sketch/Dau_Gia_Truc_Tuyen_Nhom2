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
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.Duration;


public class Item_Card_Controller extends Base_Admin_Controller {

    @FXML private ImageView imgSanPham;
    @FXML private Label lblLoaiSP;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGian; // Sử dụng lblThoiGian thay vì lblTime
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblBadgeTrangThai;
    @FXML private Button btnDatGia;

    private Timeline timeline;
    private Auction auction;
    private boolean isOwnerView = false;

    public void setOwnerView(boolean isOwner) {
        this.isOwnerView = isOwner;
        if (isOwnerView) {
            btnDatGia.setVisible(false);
            btnDatGia.setManaged(false);
            lblBadgeTrangThai.setText("SẢN PHẨM CỦA TÔI");
            lblBadgeTrangThai.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        }
    }

    public void setData(Auction auction) {
        this.auction = auction;
        Item item = auction.getItem();

        // 1. Dữ liệu văn bản
        lblTenSP.setText(item.getTenSanPham());
        lblLoaiSP.setText(item.getLoaiSanPham().toUpperCase());

        DecimalFormat df = new DecimalFormat("$#,###");
        lblGiaHienTai.setText(df.format(auction.getCurrentPrice()));

        // 2. Xử lý ảnh
        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                imgSanPham.setImage(new Image(path, true));
                Rectangle clip = new Rectangle(imgSanPham.getFitWidth(), imgSanPham.getFitHeight());
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imgSanPham.setClip(clip);
            } catch (Exception e) {
                System.out.println("Lỗi nạp ảnh: " + e.getMessage());
            }
        }

        imgSanPham.setEffect(null);
        startCountdown(); // Chỉ cần gọi 1 hàm này để xử lý thời gian
    }

    private void startCountdown() {
        if (timeline != null) timeline.stop();

        // Sử dụng javafx.util.Duration cho KeyFrame
        timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = auction.getStartTime();
            LocalDateTime end = auction.getEndTime();

            if (now.isBefore(start)) {
                // TRƯỚC KHI ĐẤU GIÁ
                updateUIState("SẮP DIỄN RA", "#F39C12");
                long sec = java.time.Duration.between(now, start).toSeconds();
                lblThoiGian.setText(formatDuration(sec));

                if (!isOwnerView) {
                    btnDatGia.setText("CHỜ ĐỢI");
                    btnDatGia.setDisable(true);
                }
            } else if (now.isBefore(end)) {
                // TRONG KHI ĐẤU GIÁ
                updateUIState("ĐANG DIỄN RA", "#D32F2F");
                long sec = java.time.Duration.between(now, end).toSeconds();
                lblThoiGian.setText(formatDuration(sec));

                if (!isOwnerView) {
                    btnDatGia.setText("ĐẶT GIÁ");
                    btnDatGia.setDisable(false);
                }
                lblBadgeTrangThai.setVisible(true);
            } else {
                // KẾT THÚC
                lblThoiGian.setText("HẾT GIỜ");
                lblThoiGian.setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;");

                if (!isOwnerView) {
                    btnDatGia.setText("KẾT THÚC");
                    btnDatGia.setDisable(true);
                }

                ColorAdjust grayscale = new ColorAdjust();
                grayscale.setSaturation(-1.0);
                imgSanPham.setEffect(grayscale);
                timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateUIState(String status, String color) {
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private String formatDuration(long secondsTotal) {
        if (secondsTotal < 0) return "00 : 00 : 00";
        long hours = secondsTotal / 3600;
        long minutes = (secondsTotal % 3600) / 60;
        long seconds = secondsTotal % 60;
        return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }

    public void stopTimeline() {
        if (timeline != null) timeline.stop();
    }

    @FXML
    private void handleDatGia(ActionEvent event) {
        if (isOwnerView) return;

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartTime()) && now.isBefore(auction.getEndTime())) {
            switchSceneWithData(event, "Phien_Dau_Gia.fxml",
                    "Đấu giá: " + auction.getItem().getTenSanPham(), this.auction);
        }
    }
}