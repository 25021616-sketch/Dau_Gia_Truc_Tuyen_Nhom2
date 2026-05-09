package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.item.*;
import Team2_CS2_Auction.Repository.ProductRepository;
import Team2_CS2_Auction.Session.Session;
import Team2_CS2_Auction.Model.user.Member;

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

    private String selectedImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Danh mục rút gọn
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồ điện tử", "Nghệ thuật", "Bất động sản", "Xe hơi", "Khác"
        ));
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {
        try {
            // 1. Kiểm tra đầu vào
            String loai = loaiSanPhamCombo.getValue();
            if (loai == null) throw new Exception("Vui lòng chọn loại sản phẩm");
            if (Session.currentUser == null) throw new Exception("Vui lòng đăng nhập lại");

            // 2. Xử lý thời gian
            LocalDateTime start = ngayBatDauPicker.getValue().atTime(
                    Integer.parseInt(gioBatDau.getText()), Integer.parseInt(phutBatDau.getText()));
            LocalDateTime end = ngayKetThucPicker.getValue().atTime(
                    Integer.parseInt(gioKetThuc.getText()), Integer.parseInt(phutKetThuc.getText()));

            // 3. Chuẩn bị thông tin giá và người bán
            double startPrice = Double.parseDouble(txtGiaKhoiDiem.getText());
            double stepPrice = Double.parseDouble(txtBuocGia.getText());
            Member seller = (Member) Session.currentUser;

            // 4. LƯU THẲNG VÀO DATABASE QUA REPOSITORY
            ProductRepository repo = new ProductRepository();

            // Hàm này sẽ thực thi lệnh INSERT vào bảng products
            boolean success = repo.insertProduct(
                    txtTenSanPham.getText(),
                    txtMoTa.getText(),
                    loai,
                    startPrice,
                    startPrice, // current_price ban đầu = giá khởi điểm
                    stepPrice,
                    seller.getId(), // Lấy ID kiểu int của User
                    start,
                    end,
                    "OPENING", // Trạng thái mặc định khi mới đăng
                    selectedImagePath
            );

            if (success) {
                // ✅ ĐÃ BỎ AuctionData.listSanPham.add(...)
                // Không cần lưu vào RAM nữa vì DB đã giữ rồi.

                resetForm();
                new Alert(Alert.AlertType.INFORMATION, "Đăng sản phẩm thành công!").showAndWait();

                // Quay về trang chủ. Controller trang chủ sẽ load lại list từ DB.
                handleBackToHome(event);
            } else {
                new Alert(Alert.AlertType.ERROR, "Lỗi kết nối cơ sở dữ liệu!").show();
            }

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đúng định dạng số cho giá và thời gian!").show();
        } catch (NullPointerException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đầy đủ các trường ngày tháng!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
    }

    private void resetForm() {
        txtTenSanPham.clear();
        txtMoTa.clear();
        txtGiaKhoiDiem.clear();
        txtBuocGia.clear();
        gioBatDau.clear(); phutBatDau.clear();
        gioKetThuc.clear(); phutKetThuc.clear();
        ngayBatDauPicker.setValue(null);
        ngayKetThucPicker.setValue(null);
        selectedImagePath = "";
        if (imgPreview != null) imgPreview.setImage(null);
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Chuyển đổi file path sang định dạng URL để ImageView hiển thị được
            selectedImagePath = file.toURI().toString();
            imgPreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}