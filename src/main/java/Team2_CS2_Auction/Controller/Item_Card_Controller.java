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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Item_Card_Controller extends Base_Admin_Controller {

    // ==================== FXML BINDINGS ====================
    @FXML private ImageView imgSanPham;
    @FXML private Label lblLoaiSP;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGian;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblBadgeTrangThai;  // GÓC TRÁI: CHỜ ĐỢI / ĐANG DIỄN RA / KẾT THÚC
    @FXML private Label lblThongBaoVịThe;   // GÓC PHẢI: SẢN PHẨM CỦA TÔI / DẪN ĐẦU / ĐÃ THẮNG
    @FXML private Button btnDatGia;         // NÚT: ĐẶT GIÁ hoặc QUẢN LÝ

    // ==================== STATE ====================
    private Timeline timeline;
    private Auction auction;
    private boolean isOwnerView = false;

    // ==================== CONSTANTS ====================
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,###");
    private static final String COLOR_WAITING = "#F39C12"; // Cam - chờ đợi
    private static final String COLOR_ONGOING = "#27ae60"; // Xanh lá - đang diễn ra
    private static final String COLOR_ENDED   = "#7F8C8D"; // Xám - kết thúc

    /** Cache ảnh theo URL/path — tải 1 lần, dùng mãi, không bao giờ chớp */
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    // ==================== LIFECYCLE ====================

    @FXML
    public void initialize() {
        // Đặt clip 1 lần duy nhất — không cần tạo lại mỗi khi load ảnh
        Rectangle clip = new Rectangle(220, 150);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imgSanPham.setClip(clip);
    }

    // ==================== ENTRY POINT ====================

    /**
     * Nạp dữ liệu phiên đấu giá vào thẻ. Gọi từ màn hình chính và màn hình đã tham gia.
     */
    public void setData(Auction auction) {
        if (auction == null) return;

        this.auction = auction;
        this.isOwnerView = false;

        // Gán thông tin cơ bản
        lblTenSP.setText(auction.getItem().getTenSanPham());
        lblLoaiSP.setText(auction.getItem().getLoaiSanPham().toUpperCase());
        lblGiaHienTai.setText("$" + PRICE_FORMAT.format(auction.getCurrentPrice()));

        populateImageData(auction.getItem());

        // Kiểm tra người dùng có phải chủ sở hữu không
        checkOwnership();

        // Bắt đầu đồng hồ đếm ngược (cũng cập nhật badge trái)
        startCountdown();
    }

    /** Đặt chế độ owner thủ công nếu cần (gọi từ bên ngoài) */
    public void setOwnerView(boolean isOwnerView) {
        this.isOwnerView = isOwnerView;
        refreshRightBadge();
        refreshButton();
    }

    public void stopTimeline() {
        if (timeline != null) timeline.stop();
    }

    // ==================== OWNERSHIP CHECK ====================

    private void checkOwnership() {
        if (Session.currentUser != null && auction.getSeller() != null) {
            isOwnerView = (Session.currentUser.getId() == auction.getSeller().getId());
        }
        refreshRightBadge();
        refreshButton();
    }

    // ==================== BADGE PHẢI (góc trên bên phải) ====================
    // Hiển thị: "SẢN PHẨM CỦA TÔI" | "BẠN ĐANG DẪN ĐẦU" | "BẠN ĐÃ THẮNG 🏆" | (ẩn)

    private void refreshRightBadge() {
        if (auction == null || lblThongBaoVịThe == null) return;

        int myId     = (Session.currentUser != null) ? Session.currentUser.getId() : -1;
        int sellerId = auction.getSeller().getId();
        int leaderId = auction.getCurrentBidderId();
        boolean ended = LocalDateTime.now().isAfter(auction.getEndTime());

        if (myId == sellerId) {
            // --- Chủ sở hữu ---
            showRightBadge("SẢN PHẨM CỦA TÔI",
                "#27ae60", "white");

        } else if (leaderId > 0 && myId == leaderId) {
            // --- Đang dẫn đầu hoặc đã thắng ---
            if (ended) {
                showRightBadge("BẠN ĐÃ THẮNG 🏆", "#e67e22", "white");
            } else {
                showRightBadge("BẠN ĐANG DẪN ĐẦU", "#f1c40f", "#2c3e50");
            }

        } else {
            // --- Không liên quan ---
            lblThongBaoVịThe.setVisible(false);
        }
    }

    private void showRightBadge(String text, String bgColor, String textColor) {
        lblThongBaoVịThe.setText(text);
        lblThongBaoVịThe.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 4 8;"
        );
        lblThongBaoVịThe.setVisible(true);
    }

    // ==================== NÚT BẤM ====================
    // Chỉ 2 nhãn: "ĐẶT GIÁ" (người mua đang mở) hoặc "QUẢN LÝ" (chủ sở hữu)

    private void refreshButton() {
        if (btnDatGia == null || auction == null) return;

        if (isOwnerView) {
            btnDatGia.setText("QUẢN LÝ");
            btnDatGia.setDisable(false);
            applyButtonStyle("#1a237e");
        } else {
            LocalDateTime now = LocalDateTime.now();
            boolean notStarted = now.isBefore(auction.getStartTime());
            boolean ended      = now.isAfter(auction.getEndTime());

            btnDatGia.setText("ĐẶT GIÁ");
            // Chỉ enable khi phiên đang diễn ra
            btnDatGia.setDisable(notStarted || ended);
            applyButtonStyle(notStarted || ended ? "#9E9E9E" : "#1a237e");
        }
    }

    private void applyButtonStyle(String bgColor) {
        btnDatGia.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 0 0 15 15;" +
            "-fx-cursor: hand;"
        );
    }

    // ==================== BADGE TRÁI + ĐẾM NGƯỢC ====================
    // isOwnerView=false: CHỜ ĐỢI | ĐANG DIỄN RA | KẾT THÚC  (màn hình chính / đã tham gia)
    // isOwnerView=true : CHỜ DUYỆT | ĐÃ DUYỆT | KHÔNG ĐƯỢC DUYỆT  (phiên của tôi)

    private void startCountdown() {
        stopTimeline();
        updateTimeLogic(); // Cập nhật ngay lần đầu không chờ 1 giây
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            updateTimeLogic();
            refreshRightBadge();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimeLogic() {
        if (auction == null) return;

        // ---- CHẾ ĐỘ PHIÊN CỦA TÔI: badge = trạng thái duyệt của Admin ----
        if (isOwnerView) {
            updateOwnerLeftBadge();
            // Vẫn hiện đồng hồ đếm ngược bình thường để chủ biết còn bao lâu
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(auction.getStartTime())) {
                updateCountdownDisplay(now, auction.getStartTime(), COLOR_WAITING);
            } else if (now.isBefore(auction.getEndTime())) {
                updateCountdownDisplay(now, auction.getEndTime(), COLOR_ONGOING);
            } else {
                lblThoiGian.setText("HẾT GIỜ");
                lblThoiGian.setStyle("-fx-text-fill: " + COLOR_ENDED + "; -fx-font-weight: bold;");
                stopTimeline();
            }
            return;
        }

        // ---- CHẾ ĐỘ THƯỜNG: badge = trạng thái thời gian ----
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end   = auction.getEndTime();

        if (now.isBefore(start)) {
            // === CHỜ ĐỢI ===
            setLeftBadge("CHỜ ĐỢI", COLOR_WAITING);
            updateCountdownDisplay(now, start, COLOR_WAITING);
            btnDatGia.setDisable(true);
            applyButtonStyle("#9E9E9E");

        } else if (now.isBefore(end)) {
            // === ĐANG DIỄN RA ===
            setLeftBadge("ĐANG DIỄN RA", COLOR_ONGOING);
            updateCountdownDisplay(now, end, COLOR_ONGOING);
            btnDatGia.setDisable(false);
            applyButtonStyle("#1a237e");

        } else {
            // === KẾT THÚC ===
            handleEndedState();
        }
    }

    /**
     * Cập nhật badge trái theo trạng thái duyệt của Admin — dùng cho màn hình "Phiên của tôi".
     */
    private void updateOwnerLeftBadge() {
        if (auction == null) return;
        switch (auction.getStatus()) {
            case PENDING:
                setLeftBadge("CHỜ DUYỆT", "#F39C12");      // 🟠 Cam
                break;
            case APPROVED:
            case OPEN:
            case CLOSED:
                setLeftBadge("ĐÃ DUYỆT", "#27ae60");       // 🟢 Xanh lá
                break;
            case REJECTED:
            case CANCELLED:
                setLeftBadge("KHÔNG ĐƯỢC DUYỆT", "#e74c3c"); // 🔴 Đỏ
                break;
            default:
                setLeftBadge("KHÔNG RÕ", "#95a5a6");
        }
    }

    private void setLeftBadge(String text, String hexColor) {
        if (lblBadgeTrangThai == null) return;
        lblBadgeTrangThai.setText(text);
        lblBadgeTrangThai.setVisible(true);
        lblBadgeTrangThai.setStyle(
            "-fx-background-color: " + hexColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 4 8;"
        );
    }

    private void updateCountdownDisplay(LocalDateTime now, LocalDateTime target, String color) {
        long totalSec = java.time.Duration.between(now, target).toSeconds();
        if (totalSec < 0) totalSec = 0;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        lblThoiGian.setText(String.format("%02d:%02d:%02d", h, m, s));
        lblThoiGian.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void handleEndedState() {
        setLeftBadge("KẾT THÚC", COLOR_ENDED);
        lblThoiGian.setText("HẾT GIỜ");
        lblThoiGian.setStyle("-fx-text-fill: " + COLOR_ENDED + "; -fx-font-weight: bold;");

        // Người mua không thể đặt giá nữa khi kết thúc
        if (!isOwnerView) {
            btnDatGia.setDisable(true);
            applyButtonStyle("#9E9E9E");
        }

        // Xám hóa ảnh khi kết thúc
        if (imgSanPham != null) {
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1.0);
            imgSanPham.setEffect(grayscale);
        }

        stopTimeline();
    }

    // ==================== ẢNH SẢN PHẨM ====================

    private void populateImageData(Item item) {
        imgSanPham.setEffect(null); // xóa hiệu ứng xám nếu có từ trước

        if (item == null) {
            imgSanPham.setImage(null);
            return;
        }
        String path = item.getImagePath();
        if (path == null || path.isEmpty()) {
            imgSanPham.setImage(null);
            return;
        }

        // Kiểm tra cache trước — nếu đã tải rồi thì hiện ngay, không chớp
        Image cached = IMAGE_CACHE.get(path);
        if (cached != null && !cached.isError()) {
            imgSanPham.setImage(cached);
            return;
        }

        // Chưa có trong cache — xoá ảnh cũ và tải mới
        imgSanPham.setImage(null);
        try {
            Image img = new Image(path, true); // Tải nền

            img.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                    IMAGE_CACHE.put(path, img);   // Lưu vào cache
                    javafx.application.Platform.runLater(() -> imgSanPham.setImage(img));
                }
            });

            // Trường hợp ảnh có sẵn ngay (ví dụ: từ JVM cache bên trong)
            if (img.getProgress() >= 1.0 && !img.isError()) {
                IMAGE_CACHE.put(path, img);
                imgSanPham.setImage(img);
            }
        } catch (Exception e) {
            imgSanPham.setImage(null);
        }
    }

    // ==================== XỬ LÝ CLICK NÚT ====================


    @FXML
    private void handleDatGia(ActionEvent event) {
        // Cho phép seller xem phiên, nhưng UI sẽ disable nút "Đặt giá" khi mở
        switchSceneWithData(event, "Phien_Dau_Gia.fxml", "Đấu giá", this.auction);
    }

    // ==================== PUBLIC UTILS ====================

    public Auction getAuction() {
        return this.auction;
    }

    public void updatePrice(double newPrice) {
        if (this.auction != null) {
            this.auction.setCurrentPrice(newPrice);
            lblGiaHienTai.setText("$" + PRICE_FORMAT.format(newPrice));
        }
    }
}