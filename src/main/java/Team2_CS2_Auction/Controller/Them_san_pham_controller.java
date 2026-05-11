package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl; // Nhớ import cái này
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

    // === BƯỚC QUAN TRỌNG: KHỞI TẠO SERVICE ===
    // Khai báo đối tượng cụ thể để gọi được các hàm không phải static
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
            // Kiểm tra session người dùng
            if (Session.currentUser == null) {
                throw new Exception("Vui lòng đăng nhập lại!");
            }
            Member seller = (Member) Session.currentUser;

            // THU THẬP DỮ LIỆU THÔ TỪ UI
            String ten = txtTenSanPham.getText();
            String loai = loaiSanPhamCombo.getValue();
            String moTa = txtMoTa.getText();
            String giaKhoi = txtGiaKhoiDiem.getText();
            String buocGia = txtBuocGia.getText();

            // Kiểm tra input cơ bản (tránh lỗi NullPointerException)
            if (ngayBatDauPicker.getValue() == null || ngayKetThucPicker.getValue() == null) {
                throw new Exception("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!");
            }

            // Lấy ngày tháng năm và ghép giờ phút
            LocalDateTime start = ngayBatDauPicker.getValue().atTime(
                    Integer.parseInt(gioBatDau.getText().trim()),
                    Integer.parseInt(phutBatDau.getText().trim()));

            LocalDateTime end = ngayKetThucPicker.getValue().atTime(
                    Integer.parseInt(gioKetThuc.getText().trim()),
                    Integer.parseInt(phutKetThuc.getText().trim()));

            // GỌI SERVICE: Bây giờ auctionService (chữ a thường) đã tồn tại nên sẽ hết lỗi
            auctionService.createAuction(seller, ten, loai, moTa, selectedImagePath, giaKhoi, buocGia, start, end);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đăng sản phẩm thành công!");
            successAlert.setHeaderText(null);
            successAlert.showAndWait();

            handleBackToHome(event);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Giờ/Phút hoặc Giá phải là số hợp lệ!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
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
            selectedImagePath = file.toURI().toString();
            imgPreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        // Giả sử switchScene nằm trong Base_Admin_Controller
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}