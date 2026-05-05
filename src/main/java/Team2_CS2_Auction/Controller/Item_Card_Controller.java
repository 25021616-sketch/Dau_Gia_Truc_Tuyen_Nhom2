package Team2_CS2_Auction.Controller;

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
import Team2_CS2_Auction.Model.item.Item;

import java.time.LocalDateTime;
import java.text.DecimalFormat;

public class Item_Card_Controller extends Base_Admin_Controller {

    @FXML private ImageView imgSanPham;
    @FXML private Label lblLoaiSP;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGian;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblBadgeTrangThai; // Badge xanh lá trong FXML mới
    @FXML private Button btnDatGia;

    private Timeline timeline;
    private Item item;

    public void setData(Item item) {
        this.item = item;

        // 1. Dữ liệu cơ bản
        lblTenSP.setText(item.getTenSanPham());
        lblLoaiSP.setText(item.getLoaiSanPham().toUpperCase());

        DecimalFormat df = new DecimalFormat("$#,###");
        lblGiaHienTai.setText(df.format(item.getGiaKhoiDiem()));

        // 2. Xử lý ảnh và bo góc ảnh
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                Image image = new Image(item.getImagePath(), true);
                imgSanPham.setImage(image);

                // Thủ thuật bo góc ảnh bằng Clip
                Rectangle clip = new Rectangle(imgSanPham.getFitWidth(), imgSanPham.getFitHeight());
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imgSanPham.setClip(clip);

            } catch (Exception e) {
                System.out.println("Lỗi load ảnh: " + e.getMessage());
            }
        }

        // 3. Reset hiệu ứng (phòng trường hợp card được tái sử dụng)
        imgSanPham.setEffect(null);

        startCountdown();
    }

    private void startCountdown() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = item.getThoiGianBatDau();
            LocalDateTime end = item.getThoiGianKetThuc();

            if (now.isBefore(start)) {
                // SẮP DIỄN RA
                updateUIState("SẮP DIỄN RA", "#F39C12", true);
                long diff = java.time.Duration.between(now, start).toSeconds();
                lblThoiGian.setText(formatDuration(diff));
                btnDatGia.setText("CHỜ ĐỢI");
                btnDatGia.setDisable(true);
                lblBadgeTrangThai.setVisible(false); // Ẩn badge vì chưa diễn ra

            } else if (now.isAfter(start) && now.isBefore(end)) {
                // ĐANG DIỄN RA
                updateUIState("ĐANG DIỄN RA", "#D32F2F", false);
                long diff = java.time.Duration.between(now, end).toSeconds();
                lblThoiGian.setText(formatDuration(diff));
                btnDatGia.setText("ĐẶT GIÁ");
                btnDatGia.setDisable(false);
                lblBadgeTrangThai.setVisible(true); // Hiện badge xanh

            } else {
                // ĐÃ KẾT THÚC
                lblThoiGian.setText("HẾT GIỜ");
                lblThoiGian.setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
                btnDatGia.setText("KẾT THÚC");
                btnDatGia.setDisable(true);
                lblBadgeTrangThai.setVisible(false);

                // Hiệu ứng trắng đen cho sản phẩm đã kết thúc
                ColorAdjust grayscale = new ColorAdjust();
                grayscale.setSaturation(-1.0);
                grayscale.setContrast(-0.2);
                imgSanPham.setEffect(grayscale);

                if (timeline != null) timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateUIState(String status, String color, boolean isPending) {
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        // Bạn có thể thêm logic đổi màu nút bấm ở đây nếu muốn
    }

    private String formatDuration(long secondsTotal) {
        long hours = secondsTotal / 3600;
        long minutes = (secondsTotal % 3600) / 60;
        long seconds = secondsTotal % 60;
        return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }

    // QUAN TRỌNG: Hàm này giúp tránh rò rỉ bộ nhớ
    public void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    @FXML
    private void handleDatGia(ActionEvent event) {
        // Chỉ cho phép bấm nếu phiên đấu giá đang diễn ra
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(item.getThoiGianBatDau()) && now.isBefore(item.getThoiGianKetThuc())) {

            // Khởi tạo lớp Base để dùng hàm chuyển cảnh (hoặc ép kiểu nếu cần)
            Base_Admin_Controller base = new Base_Admin_Controller() {};
            base.switchSceneWithData(event, "Phien_Dau_Gia.fxml", "Đấu giá: " + item.getTenSanPham(), this.item);

        } else {
            System.out.println("Phiên đấu giá chưa mở hoặc đã kết thúc!");
        }
    }
}