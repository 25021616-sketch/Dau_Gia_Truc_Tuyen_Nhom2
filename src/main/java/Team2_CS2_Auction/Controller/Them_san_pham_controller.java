package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Repository.AuctionData;
import Team2_CS2_Auction.Model.item.*;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class Them_san_pham_controller extends Base_Admin_Controller implements Initializable {

    @FXML private TextField txtTenSanPham, txtGiaKhoiDiem, txtBuocGia, gioBatDau, phutBatDau, gioKetThuc, phutKetThuc;
    @FXML private ComboBox<String> loaiSanPhamCombo;
    @FXML private TextArea txtMoTa;
    @FXML private DatePicker ngayBatDauPicker, ngayKetThucPicker;
    @FXML private ImageView imgPreview;
    @FXML private VBox groupDoDienTu, groupDongHo, groupSach, groupBatDongSan;
    @FXML private TextField txtHangSX, txtCongSuat, txtThuongHieuDH, txtNamSXDH, txtTacGia, txtNamSangTac, txtDiaChiBDS, txtDienTichBDS;

    private String selectedImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồng hồ", "Sách", "Trang sức", "Xe hơi",
                "Tác phẩm nghệ thuật", "Bất động sản", "Đồ điện tử", "Khác"
        ));

        hideAllGroups();

        // Lắng nghe thay đổi ComboBox để hiện form tương ứng
        loaiSanPhamCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            hideAllGroups();
            if (newVal == null) return;
            switch (newVal) {
                case "Đồ điện tử": showGroup(groupDoDienTu); break;
                case "Đồng hồ", "Xe hơi", "Trang sức": showGroup(groupDongHo); break;
                case "Sách", "Tác phẩm nghệ thuật": showGroup(groupSach); break;
                case "Bất động sản": showGroup(groupBatDongSan); break;
            }
        });
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {
        try {
            LocalDateTime start = ngayBatDauPicker.getValue().atTime(Integer.parseInt(gioBatDau.getText()), Integer.parseInt(phutBatDau.getText()));
            LocalDateTime end = ngayKetThucPicker.getValue().atTime(Integer.parseInt(gioKetThuc.getText()), Integer.parseInt(phutKetThuc.getText()));

            String customID = "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String loai = loaiSanPhamCombo.getValue();
            Item newItem;

            // Logic tạo Object theo lớp con (giữ nguyên chức năng cũ)
            switch (loai) {
                case "Đồ điện tử": newItem = new Electronics(customID, txtTenSanPham.getText(), loai, txtMoTa.getText(), Double.parseDouble(txtGiaKhoiDiem.getText()), Double.parseDouble(txtBuocGia.getText()), start, end, txtHangSX.getText(), txtCongSuat.getText()); break;
                case "Bất động sản": newItem = new RealEstate(customID, txtTenSanPham.getText(), loai, txtMoTa.getText(), Double.parseDouble(txtGiaKhoiDiem.getText()), Double.parseDouble(txtBuocGia.getText()), start, end, txtDiaChiBDS.getText(), Double.parseDouble(txtDienTichBDS.getText()), "Sổ đỏ"); break;
                case "Sách", "Tác phẩm nghệ thuật": newItem = new Art(customID, txtTenSanPham.getText(), loai, txtMoTa.getText(), Double.parseDouble(txtGiaKhoiDiem.getText()), Double.parseDouble(txtBuocGia.getText()), start, end, txtTacGia.getText(), txtNamSangTac.getText()); break;
                default: newItem = new Art(customID, txtTenSanPham.getText(), loai, txtMoTa.getText(), Double.parseDouble(txtGiaKhoiDiem.getText()), Double.parseDouble(txtBuocGia.getText()), start, end, "N/A", "N/A");
            }

            newItem.setImagePath(selectedImagePath);
            AuctionData.listSanPham.add(newItem);

            resetForm(); // Làm sạch form sau khi đăng
            new Alert(Alert.AlertType.INFORMATION, "Sản phẩm đã được đăng thành công!").showAndWait();
            handleBackToHome(event);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
    }

    private void resetForm() {
        txtTenSanPham.clear();
        txtMoTa.clear();
        txtGiaKhoiDiem.clear();
        txtBuocGia.clear();
        imgPreview.setImage(null);
        selectedImagePath = "";
        hideAllGroups();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImagePath = file.toURI().toString();
            imgPreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    private void hideAllGroups() {
        VBox[] groups = {groupDoDienTu, groupDongHo, groupSach, groupBatDongSan};
        for (VBox g : groups) if (g != null) { g.setVisible(false); g.setManaged(false); }
    }

    private void showGroup(VBox g) {
        if (g != null) { g.setVisible(true); g.setManaged(true); }
    }
}