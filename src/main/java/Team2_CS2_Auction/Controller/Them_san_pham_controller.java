package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.item.*;
import Team2_CS2_Auction.Repository.AuctionData;
import Team2_CS2_Auction.Repository.ProductRepository;
import Team2_CS2_Auction.Session.Session;

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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Them_san_pham_controller extends Base_Admin_Controller implements Initializable {

    @FXML private TextField txtTenSanPham, txtGiaKhoiDiem, txtBuocGia;
    @FXML private TextField gioBatDau, phutBatDau, gioKetThuc, phutKetThuc;

    @FXML private ComboBox<String> loaiSanPhamCombo;
    @FXML private TextArea txtMoTa;

    @FXML private DatePicker ngayBatDauPicker, ngayKetThucPicker;

    @FXML private ImageView imgPreview;

    @FXML private VBox groupDoDienTu, groupDongHo, groupSach, groupBatDongSan;

    @FXML private TextField txtHangSX, txtCongSuat;
    @FXML private TextField txtThuongHieuDH, txtNamSXDH;
    @FXML private TextField txtTacGia, txtNamSangTac;
    @FXML private TextField txtDiaChiBDS, txtDienTichBDS;

    private String selectedImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồng hồ",
                "Sách",
                "Trang sức",
                "Xe hơi",
                "Tác phẩm nghệ thuật",
                "Bất động sản",
                "Đồ điện tử",
                "Khác"
        ));

        hideAllGroups();

        loaiSanPhamCombo.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {

                    hideAllGroups();

                    if (newVal == null) return;

                    switch (newVal) {
                        case "Đồ điện tử" -> showGroup(groupDoDienTu);
                        case "Đồng hồ", "Xe hơi", "Trang sức" -> showGroup(groupDongHo);
                        case "Sách", "Tác phẩm nghệ thuật" -> showGroup(groupSach);
                        case "Bất động sản" -> showGroup(groupBatDongSan);
                    }
                });
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {

        try {

            String loai = loaiSanPhamCombo.getValue();

            if (loai == null) {
                throw new Exception("Vui lòng chọn loại sản phẩm");
            }

            LocalDateTime start = ngayBatDauPicker.getValue().atTime(
                    Integer.parseInt(gioBatDau.getText()),
                    Integer.parseInt(phutBatDau.getText())
            );

            LocalDateTime end = ngayKetThucPicker.getValue().atTime(
                    Integer.parseInt(gioKetThuc.getText()),
                    Integer.parseInt(phutKetThuc.getText())
            );

            List<byte[]> images = new ArrayList<>();

            Item newItem;

            String id = "ID" + System.currentTimeMillis(); // tạo id tạm

            switch (loai) {

                case "Đồ điện tử" -> newItem = new Electronics(
                        id,
                        txtTenSanPham.getText(),
                        loai,
                        txtMoTa.getText(),
                        Double.parseDouble(txtGiaKhoiDiem.getText()),
                        Double.parseDouble(txtBuocGia.getText()),
                        start,
                        end,
                        selectedImagePath,
                        txtHangSX.getText(),
                        txtCongSuat.getText()
                );

                case "Sách", "Tác phẩm nghệ thuật" -> newItem = new Art(
                        id,
                        txtTenSanPham.getText(),
                        loai,
                        txtMoTa.getText(),
                        Double.parseDouble(txtGiaKhoiDiem.getText()),
                        Double.parseDouble(txtBuocGia.getText()),
                        start,
                        end,
                        selectedImagePath,
                        txtTacGia.getText(),
                        txtNamSangTac.getText()
                );

                case "Bất động sản" -> newItem = new RealEstate(
                        id,
                        txtTenSanPham.getText(),
                        loai,
                        txtMoTa.getText(),
                        Double.parseDouble(txtGiaKhoiDiem.getText()),
                        Double.parseDouble(txtBuocGia.getText()),
                        start,
                        end,
                        selectedImagePath,
                        txtDiaChiBDS.getText(),
                        Double.parseDouble(txtDienTichBDS.getText()),
                        "Nhà đất"
                );

                case "Xe hơi" -> newItem = new Vehicle(
                        id,
                        txtTenSanPham.getText(),
                        loai,
                        txtMoTa.getText(),
                        Double.parseDouble(txtGiaKhoiDiem.getText()),
                        Double.parseDouble(txtBuocGia.getText()),
                        start,
                        end,
                        selectedImagePath,
                        txtThuongHieuDH.getText(),
                        txtNamSXDH.getText()   // ✅ chỉ 2 field cuối
                );

                default -> newItem = new Art(
                        id,
                        txtTenSanPham.getText(),
                        loai,
                        txtMoTa.getText(),
                        Double.parseDouble(txtGiaKhoiDiem.getText()),
                        Double.parseDouble(txtBuocGia.getText()),
                        start,
                        end,
                        selectedImagePath,
                        txtTacGia.getText(),
                        txtNamSangTac.getText()   // ✅ FIX ở đây
                );
            }


            ProductRepository repo = new ProductRepository();

            boolean success = repo.insertProduct(

                    // =========================
                    // THÔNG TIN SẢN PHẨM
                    // =========================
                    txtTenSanPham.getText(),

                    txtMoTa.getText(),

                    loai,

                    // =========================
                    // GIÁ
                    // =========================
                    Double.parseDouble(txtGiaKhoiDiem.getText()),

                    // current_price ban đầu = giá khởi điểm
                    Double.parseDouble(txtGiaKhoiDiem.getText()),

                    Double.parseDouble(txtBuocGia.getText()),

                    // ==========================================
                    // SELLER ID
                    // ==========================================
                    // Lấy id của user đang đăng nhập
                    // thay vì hardcode = 1
                    // ==========================================
                    Session.currentUser.getId(),

                    // =========================
                    // THỜI GIAN
                    // =========================
                    start,

                    end,

                    // =========================
                    // TRẠNG THÁI
                    // =========================
                    "PENDING",

                    // =========================
                    // ẢNH
                    // =========================
                    selectedImagePath
            );

            if (success) {

                System.out.println("Lưu DB thành công");

                AuctionData.listSanPham.add(newItem);

                resetForm();

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Đăng sản phẩm thành công!"
                ).showAndWait();

                handleBackToHome(event);

            } else {
                throw new Exception("Không thể lưu dữ liệu");
            }

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Lỗi: " + e.getMessage()).show();
        }
    }

    private void resetForm() {

        txtTenSanPham.clear();
        txtMoTa.clear();
        txtGiaKhoiDiem.clear();
        txtBuocGia.clear();

        gioBatDau.clear();
        phutBatDau.clear();
        gioKetThuc.clear();
        phutKetThuc.clear();

        ngayBatDauPicker.setValue(null);
        ngayKetThucPicker.setValue(null);

        if (imgPreview != null) {
            imgPreview.setImage(null);
            imgPreview.setVisible(false);
        }

        selectedImagePath = "";

        hideAllGroups();
    }

    @FXML
    private void handleChooseImage() {

        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedImagePath = file.toURI().toString();
            if (imgPreview != null) {
                imgPreview.setImage(new Image(selectedImagePath));
                imgPreview.setVisible(true);
            }
        }
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {

        switchScene(
                event,
                "Man_hinh_chinh_Users.fxml",
                "Trang chủ"
        );
    }

    private void hideAllGroups() {

        VBox[] groups = {
                groupDoDienTu,
                groupDongHo,
                groupSach,
                groupBatDongSan
        };

        for (VBox g : groups) {
            if (g != null) {
                g.setVisible(false);
                g.setManaged(false);
            }
        }
    }

    private void showGroup(VBox g) {

        if (g != null) {
            g.setVisible(true);
            g.setManaged(true);
        }
    }
}