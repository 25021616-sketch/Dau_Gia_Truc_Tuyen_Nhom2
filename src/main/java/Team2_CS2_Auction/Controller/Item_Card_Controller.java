package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
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

    // Nhãn mới bạn vừa thêm vào FXML
    @FXML private Label lblThongBaoVịThe;

    private Timeline timeline;
    private Auction auction;
    private boolean isOwnerView = false;

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("$#,###");

    private static final String COLOR_WAITING = "#F39C12";
    private static final String COLOR_ONGOING = "#27ae60";
    private static final String COLOR_ENDED = "#7F8C8D";

    public void setData(Auction auction) {
        if (auction == null) return;

        this.auction = auction;
        this.isOwnerView = false;

        // Gán dữ liệu cơ bản
        lblTenSP.setText(auction.getItem().getTenSanPham());
        lblLoaiSP.setText(auction.getItem().getLoaiSanPham().toUpperCase());
        lblGiaHienTai.setText(PRICE_FORMAT.format(auction.getCurrentPrice()));

        populateImageData(auction.getItem());

        // Tự kiểm tra quyền sở hữu
        checkOwnerShip();

        // Khởi động đồng hồ
        startCountdown();
    }

    /**
     * HÀM 1: Fix lỗi đỏ "Cannot resolve method setOwnerView"
     */
    public void setOwnerView(boolean isOwnerView) {
        this.isOwnerView = isOwnerView;
        updatePositionLabel();
    }

    /**
     * HÀM 2: Fix lỗi đỏ "Cannot resolve method stopTimeline"
     */
    public void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void checkOwnerShip() {
        if (Session.currentUser != null && auction != null && auction.getSeller() != null) {
            if (Session.currentUser.getId() == auction.getSeller().getId()) {
                this.isOwnerView = true;
            }
        }
        updatePositionLabel();
    }

    private void updatePositionLabel() {
        if (Session.currentUser == null || auction == null || lblThongBaoVịThe == null) return;

        int myId = Session.currentUser.getId();
        int sellerId = auction.getSeller().getId();
        int leaderId = auction.getCurrentBidderId();

        if (myId == sellerId) {
            lblThongBaoVịThe.setText("SẢN PHẨM CỦA TÔI");
            lblThongBaoVịThe.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            lblThongBaoVịThe.setVisible(true);
            btnDatGia.setText("QUẢN LÝ");
        } else if (leaderId > 0 && myId == leaderId) {
            lblThongBaoVịThe.setText("BẠN ĐANG DẪN ĐẦU");
            lblThongBaoVịThe.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 5;");
            lblThongBaoVịThe.setVisible(true);
            btnDatGia.setText("GIỮ GIÁ");
        } else {
            lblThongBaoVịThe.setVisible(false);
            btnDatGia.setText("XEM CHI TIẾT");
        }
    }

    private void startCountdown() {
        stopTimeline();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateTimeLogic();
            updatePositionLabel();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimeLogic() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end = auction.getEndTime();

        if (now.isBefore(start)) {
            lblBadgeTrangThai.setText("SẮP DIỄN RA");
            lblBadgeTrangThai.setStyle("-fx-background-color: " + COLOR_WAITING + "; -fx-text-fill: white; -fx-background-radius: 5;");
            updateTimeDisplay(now, start, COLOR_WAITING);
            btnDatGia.setDisable(true);
        } else if (now.isBefore(end)) {
            lblBadgeTrangThai.setText("ĐANG DIỄN RA");
            lblBadgeTrangThai.setStyle("-fx-background-color: " + COLOR_ONGOING + "; -fx-text-fill: white; -fx-background-radius: 5;");
            updateTimeDisplay(now, end, COLOR_ONGOING);
            btnDatGia.setDisable(false);
        } else {
            handleEndedState();
        }
    }

    private void updateTimeDisplay(LocalDateTime now, LocalDateTime target, String color) {
        long sec = java.time.Duration.between(now, target).toSeconds();
        if (sec < 0) sec = 0;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        lblThoiGian.setText(String.format("%02d : %02d : %02d", h, m, s));
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void handleEndedState() {
        lblThoiGian.setText("HẾT GIỜ");
        lblThoiGian.setStyle("-fx-text-fill: " + COLOR_ENDED + "; -fx-font-weight: bold;");
        lblBadgeTrangThai.setText("KẾT THÚC");
        lblBadgeTrangThai.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-background-radius: 5;");
        btnDatGia.setDisable(true);
        btnDatGia.setText("ĐÃ ĐÓNG");

        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        imgSanPham.setEffect(grayscale);
        stopTimeline();
    }

    private void populateImageData(Item item) {
        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                imgSanPham.setImage(new Image(path, true));
                Rectangle clip = new Rectangle(220, 150);
                clip.setArcWidth(30); clip.setArcHeight(30);
                imgSanPham.setClip(clip);
            } catch (Exception e) {}
        }
    }

    @FXML
    private void handleDatGia(ActionEvent event) {
        if (isOwnerView) {
            System.out.println("Mở quản lý cho SP: " + auction.getId());
        } else {
            switchSceneWithData(event, "Phien_Dau_Gia.fxml", "Đấu giá", this.auction);
        }
    }

    public Auction getAuction() {
        return this.auction;
    }

    public void updatePrice(double newPrice) {
        if (this.auction != null) {
            this.auction.setCurrentPrice(newPrice);
            lblGiaHienTai.setText(PRICE_FORMAT.format(newPrice));
            // updatePositionLabel(); // Nếu cần kiểm tra lại người dẫn đầu thì gọi ở đây, 
            // nhưng hiện tại server chỉ trả về newPrice.
        }
    }
}