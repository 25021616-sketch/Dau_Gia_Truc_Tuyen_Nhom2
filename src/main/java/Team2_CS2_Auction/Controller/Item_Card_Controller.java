package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction; // ✅ Import Auction
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
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.util.List;

public class Item_Card_Controller extends Base_Admin_Controller {

    @FXML private ImageView imgSanPham;
    @FXML private Label lblLoaiSP;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGian;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblBadgeTrangThai;
    @FXML private Button btnDatGia;

    private Timeline timeline;
    private Auction auction; // ✅ Đổi từ Item sang Auction

    public void setData(Auction auction) {
        this.auction = auction;
        Item item = auction.getItem(); // ✅ Lấy Item từ Auction để lấy thông tin mô tả

        // 1. Dữ liệu cơ bản
        lblTenSP.setText(item.getTenSanPham());
        lblLoaiSP.setText(item.getLoaiSanPham().toUpperCase());

        // ✅ Lấy giá hiện tại từ Auction (không phải giá khởi điểm của Item)
        DecimalFormat df = new DecimalFormat("$#,###");
        lblGiaHienTai.setText(df.format(auction.getCurrentPrice()));

        // 2. Xử lý ảnh (Lấy ảnh đầu tiên trong danh sách imagePaths)
        List<String> images = item.getImagePaths();
        if (images != null && !images.isEmpty()) {
            try {
                String path = images.get(0);
                Image image = new Image(path, true);
                imgSanPham.setImage(image);

                // Bo góc ảnh
                Rectangle clip = new Rectangle(imgSanPham.getFitWidth(), imgSanPham.getFitHeight());
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imgSanPham.setClip(clip);

            } catch (Exception e) {
                System.out.println("Lỗi load ảnh: " + e.getMessage());
            }
        }

        // 3. Reset hiệu ứng
        imgSanPham.setEffect(null);

        startCountdown();
    }

    private void startCountdown() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            // ✅ Lấy thời gian từ đối tượng Auction
            LocalDateTime start = auction.getStartTime();
            LocalDateTime end = auction.getEndTime();

            if (now.isBefore(start)) {
                // SẮP DIỄN RA
                updateUIState("SẮP DIỄN RA", "#F39C12");
                long diff = java.time.Duration.between(now, start).toSeconds();
                lblThoiGian.setText(formatDuration(diff));
                btnDatGia.setText("CHỜ ĐỢI");
                btnDatGia.setDisable(true);
                lblBadgeTrangThai.setVisible(false);

            } else if (now.isAfter(start) && now.isBefore(end)) {
                // ĐANG DIỄN RA
                updateUIState("ĐANG DIỄN RA", "#D32F2F");
                long diff = java.time.Duration.between(now, end).toSeconds();
                lblThoiGian.setText(formatDuration(diff));
                btnDatGia.setText("ĐẶT GIÁ");
                btnDatGia.setDisable(false);
                lblBadgeTrangThai.setVisible(true);

            } else {
                // ĐÃ KẾT THÚC
                lblThoiGian.setText("HẾT GIỜ");
                lblThoiGian.setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
                btnDatGia.setText("KẾT THÚC");
                btnDatGia.setDisable(true);
                lblBadgeTrangThai.setVisible(false);

                // Hiệu ứng trắng đen cho ảnh
                ColorAdjust grayscale = new ColorAdjust();
                grayscale.setSaturation(-1.0);
                imgSanPham.setEffect(grayscale);

                if (timeline != null) timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateUIState(String status, String color) {
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private String formatDuration(long secondsTotal) {
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
        // ✅ Kiểm tra thời gian từ Auction
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartTime()) && now.isBefore(auction.getEndTime())) {

            // Chuyển sang màn hình chi tiết đấu giá, truyền đối tượng auction đi
            switchSceneWithData(event, "Phien_Dau_Gia.fxml",
                    "Đấu giá: " + auction.getItem().getTenSanPham(), this.auction);

        } else {
            System.out.println("Phiên đấu giá chưa mở hoặc đã kết thúc!");
        }
    }
}