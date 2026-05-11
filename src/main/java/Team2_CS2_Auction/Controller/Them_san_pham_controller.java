package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Session.Session;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
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

    private String selectedImagePath = "";
    private File selectedFile = null;

    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồ điện tử", "Tác phẩm nghệ thuật", "Bất động sản", "Xe hơi", "Khác"
        ));
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {
        try {
            Member seller = getCurrentUser();

            validateInputFields();

            String ten = txtTenSanPham.getText().trim();
            String loai = loaiSanPhamCombo.getValue();
            String moTa = txtMoTa.getText().trim();
            String giaKhoi = txtGiaKhoiDiem.getText().trim();
            String buocGia = txtBuocGia.getText().trim();

            LocalDateTime start = parseDateTime(ngayBatDauPicker, gioBatDau, phutBatDau);
            LocalDateTime end = parseDateTime(ngayKetThucPicker, gioKetThuc, phutKetThuc);

            String finalImagePath = uploadSelectedImage();

            auctionService.createAuction(seller, ten, loai, moTa, finalImagePath, giaKhoi, buocGia, start, end);

            showSuccessAlert("Đăng sản phẩm thành công!");
            handleBackToHome(event);

        } catch (NumberFormatException e) {
            showErrorAlert("Giờ/Phút hoặc Giá phải là chữ số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert(e.getMessage());
        }
    }

    private Member getCurrentUser() throws Exception {
        if (Session.currentUser == null) {
            throw new Exception("Vui lòng đăng nhập lại!");
        }
        return (Member) Session.currentUser;
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

    private LocalDateTime parseDateTime(DatePicker datePicker, TextField hourField, TextField minuteField) {
        int hour = Integer.parseInt(hourField.getText().trim());
        int minute = Integer.parseInt(minuteField.getText().trim());
        return datePicker.getValue().atTime(hour, minute);
    }

    private String uploadSelectedImage() throws Exception {
        if (selectedFile == null) {
            return "";
        }

        System.out.println("Đang upload ảnh lên mạng...");
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
        if (file != null) {
            selectedFile = file;
            selectedImagePath = file.toURI().toString();
            imgPreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}