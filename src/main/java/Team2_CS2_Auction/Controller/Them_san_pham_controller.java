package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Session.Session;
import Team2_CS2_Auction.Networking.NetworkManager;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class Them_san_pham_controller extends Base_Admin_Controller implements Initializable {

    @FXML private TextField txtTenSanPham, txtGiaKhoiDiem, txtBuocGia;
    @FXML private TextField gioBatDau, phutBatDau, gioKetThuc, phutKetThuc;
    @FXML private ComboBox<String> loaiSanPhamCombo;
    @FXML private TextArea txtMoTa;
    @FXML private DatePicker ngayBatDauPicker, ngayKetThucPicker;
    @FXML private ImageView imgPreview;
    @FXML private VBox vboxPlaceholder;
    @FXML private Button btnDeleteImage;

    private String selectedImagePath = "";
    private File selectedFile = null;
    private String oldAuctionIdForRelist = null;
    private boolean isRelist = false;

    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồ điện tử", "Tác phẩm nghệ thuật", "Bất động sản", "Xe hơi", "Khác"
        ));
        if (btnDeleteImage != null) btnDeleteImage.setVisible(false);
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {
        try {
            User seller = getCurrentUser();

            validateInputFields();

            String ten = txtTenSanPham.getText().trim();
            String loai = loaiSanPhamCombo.getValue();
            String moTa = txtMoTa.getText().trim();
            String giaKhoi = txtGiaKhoiDiem.getText().trim();
            String buocGia = txtBuocGia.getText().trim();

            LocalDateTime start = parseDateTime(ngayBatDauPicker, gioBatDau, phutBatDau);
            LocalDateTime end = parseDateTime(ngayKetThucPicker, gioKetThuc, phutKetThuc);

            if (start.isBefore(LocalDateTime.now())) {
                throw new Exception("Thời gian bắt đầu phải lớn hơn thời gian hiện tại!");
            }

            if (end.isBefore(start)) {
                throw new Exception("Thời gian kết thúc phải sau thời gian bắt đầu!");
            }

            String finalImagePath = uploadSelectedImage();

            auctionService.createAuction(seller, ten, loai, moTa, finalImagePath, giaKhoi, buocGia, start, end);

            showSuccessAlert(oldAuctionIdForRelist != null ? "Đăng lại sản phẩm thành công!" : "Đăng sản phẩm thành công!");

            if (oldAuctionIdForRelist != null) {
                // Thông báo cho các user đang xem phiên cũ biết phiên đó đã được tái đăng
                JsonObject payload = new JsonObject();
                payload.addProperty("auctionId", oldAuctionIdForRelist);
                payload.addProperty("sellerId", seller.getId());
                payload.addProperty("productName", ten);
                payload.addProperty("relist", true);
                NetworkManager.getInstance().send("CANCEL_AUCTION", payload);
                oldAuctionIdForRelist = null;
            } else {
                NetworkManager.getInstance().send("PRODUCT_UPDATED", "");
            }

            clearAllFields();
            handleBackToHome(event);

        } catch (NumberFormatException e) {
            showErrorAlert("Giờ/Phút hoặc Giá phải là chữ số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert(e.getMessage());
        }
    }

    /**
     * Xóa sạch toàn bộ dữ liệu trên form và khôi phục trạng thái ban đầu.
     */
    private void clearAllFields() {
        txtTenSanPham.clear();
        txtGiaKhoiDiem.clear();
        txtBuocGia.clear();
        txtMoTa.clear();

        gioBatDau.clear();
        phutBatDau.clear();
        gioKetThuc.clear();
        phutKetThuc.clear();

        loaiSanPhamCombo.setValue(null);
        ngayBatDauPicker.setValue(null);
        ngayKetThucPicker.setValue(null);

        selectedFile = null;
        selectedImagePath = "";
        imgPreview.setImage(null);
        imgPreview.setVisible(false);
        vboxPlaceholder.setVisible(true);
        btnDeleteImage.setVisible(false);
        txtTenSanPham.setDisable(false);
        loaiSanPhamCombo.setDisable(false);
        oldAuctionIdForRelist = null;
    }

    @Override
    protected void onResume() {
        // Nếu không phải luồng đăng lại, xóa form để tránh dữ liệu cũ bị tồn dư khi cache
        if (!isRelist) {
            clearAllFields();
        }
        isRelist = false;
    }

    public void setRelistData(Team2_CS2_Auction.Model.auction.Auction oldAuction) {
        clearAllFields();
        this.oldAuctionIdForRelist = oldAuction.getAuctionId();
        // Đánh dấu đây là luồng đăng lại để onResume không xóa dữ liệu vừa điền
        this.isRelist = true;

        txtTenSanPham.setText(oldAuction.getItem().getTenSanPham());
        txtTenSanPham.setDisable(true);

        loaiSanPhamCombo.setValue(oldAuction.getItem().getLoaiSanPham());
        loaiSanPhamCombo.setDisable(true);

        txtGiaKhoiDiem.setText(String.valueOf((long) oldAuction.getCurrentPrice()));
        txtBuocGia.setText(String.valueOf((long) oldAuction.getStepPrice()));
        txtMoTa.setText(oldAuction.getItem().getMoTa());

        String oldImg = oldAuction.getItem().getImagePath();
        if (oldImg != null && !oldImg.isEmpty()) {
            selectedImagePath = oldImg;
            try {
                imgPreview.setImage(new Image(oldImg, true));
                imgPreview.setVisible(true);
                vboxPlaceholder.setVisible(false);
                btnDeleteImage.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private User getCurrentUser() throws Exception {
        if (Session.currentUser == null) {
            throw new Exception("Vui lòng đăng nhập lại!");
        }
        return Session.currentUser;
    }

    private void validateInputFields() throws Exception {
        if (txtTenSanPham.getText().trim().isEmpty() ||
                txtGiaKhoiDiem.getText().trim().isEmpty() ||
                txtBuocGia.getText().trim().isEmpty() ||
                loaiSanPhamCombo.getValue() == null) {
            throw new Exception("Vui lòng điền đầy đủ các thông tin cơ bản!");
        }
        if (ngayBatDauPicker.getValue() == null || ngayKetThucPicker.getValue() == null) {
            throw new Exception("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!");
        }
    }

    private LocalDateTime parseDateTime(DatePicker datePicker, TextField hourField, TextField minuteField) throws Exception {
        int hour = Integer.parseInt(hourField.getText().trim());
        int minute = Integer.parseInt(minuteField.getText().trim());

        if (hour < 0 || hour > 23) {
            throw new Exception("Giờ phải từ 0 -> 23");
        }
        if (minute < 0 || minute > 59) {
            throw new Exception("Phút phải từ 0 -> 59");
        }
        return datePicker.getValue().atTime(hour, minute);
    }

    private String uploadSelectedImage() throws Exception {
        if (selectedFile == null) {
            return selectedImagePath; // Trả về URL ảnh cũ nếu đang đăng lại mà không đổi ảnh
        }
        String uploadedUrl = Team2_CS2_Auction.util.ImgBBUploader.uploadImage(selectedFile);
        if (uploadedUrl == null) {
            throw new Exception("Lỗi tải ảnh lên server ImgBB! Vui lòng thử lại.");
        }
        return uploadedUrl;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        new Alert(Alert.AlertType.ERROR, "Lỗi: " + message).show();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return;
        }

        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null || !mimeType.startsWith("image")) {
                showErrorAlert("File không phải ảnh hợp lệ!");
                return;
            }

            long fileSizeMB = file.length() / (1024 * 1024);
            if (fileSizeMB > 5) {
                showErrorAlert("Ảnh vượt quá 5MB!");
                return;
            }

            selectedFile = file;
            selectedImagePath = file.toURI().toString();
            imgPreview.setImage(new Image(selectedImagePath));
            imgPreview.setVisible(true);
            vboxPlaceholder.setVisible(false);
            btnDeleteImage.setVisible(true);

        } catch (Exception e) {
            showErrorAlert("Không thể đọc file ảnh!");
        }
    }

    @FXML
    private void handleDeleteImage(ActionEvent event) {
        event.consume();
        selectedFile = null;
        selectedImagePath = "";
        imgPreview.setImage(null);
        imgPreview.setVisible(false);
        vboxPlaceholder.setVisible(true);
        btnDeleteImage.setVisible(false);
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}