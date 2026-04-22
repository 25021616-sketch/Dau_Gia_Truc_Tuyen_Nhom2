package Team2_CS2_Auction.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class Them_san_pham_controller extends Base_Admin_Controller implements Initializable {

    @FXML
    private Button Quay_lai_trang_chủ;
    @FXML
    private Button Đăng_Phien_Đấu_Giá;


    // Các trường thông tin cơ bản
    @FXML private TextField txtTenSanPham;
    @FXML private ComboBox<String> loaiSanPhamCombo;
    @FXML private TextArea txtMoTa;
    @FXML private TextField txtGiaKhoiDiem;
    @FXML private TextField txtBuocGia;

    // Cụm Thời gian Bắt đầu
    @FXML private DatePicker ngayBatDauPicker;
    @FXML private TextField gioBatDau;
    @FXML private TextField phutBatDau;

    // Cụm Thời gian Kết thúc
    @FXML private DatePicker ngayKetThucPicker;
    @FXML private TextField gioKetThuc;
    @FXML private TextField phutKetThuc;

    // Các nhóm trường thông tin thêm (Ẩn/Hiện)
    @FXML private VBox groupDongHo;
    @FXML private VBox groupSach;
    @FXML private VBox groupBatDongSan;
    @FXML private VBox groupDoDienTu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Đổ dữ liệu vào ComboBox
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồng hồ", "Sách", "Trang sức", "Xe hơi",
                "Tác phẩm nghệ thuật", "Bất động sản", "Đồ điện tử", "Khác"
        ));

        // 2. Thiết lập trạng thái ẩn ban đầu cho các group phụ
        hideAllGroups();

        // 3. Lắng nghe sự kiện chọn loại sản phẩm để hiện group tương ứng
        loaiSanPhamCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateUI(newVal);
        });

        // 4. Ràng buộc chỉ cho phép nhập số vào các ô Giờ/Phút
        setupNumberValidation(gioBatDau, 23);
        setupNumberValidation(phutBatDau, 59);
        setupNumberValidation(gioKetThuc, 23);
        setupNumberValidation(phutKetThuc, 59);

        // Ràng buộc nhập số cho Giá tiền
        txtGiaKhoiDiem.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) txtGiaKhoiDiem.setText(val.replaceAll("[^\\d]", ""));
        });
    }

    private void updateUI(String selected) {
        hideAllGroups();
        if (selected == null) return;

        switch (selected) {
            case "Đồng hồ":
            case "Trang sức":
            case "Xe hơi":
                showGroup(groupDongHo);
                break;
            case "Sách":
            case "Tác phẩm nghệ thuật":
                showGroup(groupSach);
                break;
            case "Bất động sản":
                showGroup(groupBatDongSan);
                break;
            case "Đồ điện tử":
                showGroup(groupDoDienTu);
                break;
        }
    }

    private void hideAllGroups() {
        VBox[] groups = {groupDongHo, groupSach, groupBatDongSan, groupDoDienTu};
        for (VBox g : groups) {
            if (g != null) {
                g.setVisible(false);
                g.setManaged(false);
            }
        }
    }

    private void showGroup(VBox group) {
        if (group != null) {
            group.setVisible(true);
            group.setManaged(true);
        }
    }

    // Hàm bổ trợ: Chỉ cho nhập số và giới hạn giá trị tối đa (ví dụ 23h, 59p)
    private void setupNumberValidation(TextField tf, int maxVal) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tf.setText(newVal.replaceAll("[^\\d]", ""));
            } else if (!newVal.isEmpty()) {
                int val = Integer.parseInt(newVal);
                if (val > maxVal) tf.setText(oldVal);
            }
            if (tf.getText().length() > 2) tf.setText(oldVal);
        });
    }

    @FXML
    private void handleDangSanPham() {
        try {
            // 1. Kiểm tra các trường cơ bản
            if (txtTenSanPham.getText().isEmpty() || loaiSanPhamCombo.getValue() == null) {
                showAlert("Lỗi", "Vui lòng nhập tên và chọn loại sản phẩm!");
                return;
            }

            // 2. Xử lý Thời gian Bắt đầu
            if (ngayBatDauPicker.getValue() == null || gioBatDau.getText().isEmpty() || phutBatDau.getText().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thời gian bắt đầu!");
                return;
            }
            LocalDateTime startDateTime = ngayBatDauPicker.getValue().atTime(
                    Integer.parseInt(gioBatDau.getText()),
                    Integer.parseInt(phutBatDau.getText())
            );

            // 3. Xử lý Thời gian Kết thúc
            if (ngayKetThucPicker.getValue() == null || gioKetThuc.getText().isEmpty() || phutKetThuc.getText().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thời gian kết thúc!");
                return;
            }
            LocalDateTime endDateTime = ngayKetThucPicker.getValue().atTime(
                    Integer.parseInt(gioKetThuc.getText()),
                    Integer.parseInt(phutKetThuc.getText())
            );

            // 4. Kiểm tra logic: Kết thúc phải sau Bắt đầu
            if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
                showAlert("Lỗi thời gian", "Thời gian kết thúc phải sau thời gian bắt đầu!");
                return;
            }

            // 5. Thành công - Thu thập dữ liệu để lưu vào Database (Giai đoạn tiếp theo)
            System.out.println("Đăng sản phẩm: " + txtTenSanPham.getText());
            System.out.println("Loại: " + loaiSanPhamCombo.getValue());
            System.out.println("Bắt đầu: " + startDateTime);
            System.out.println("Kết thúc: " + endDateTime);

            showAlert("Thành công", "Sản phẩm của bạn đã sẵn sàng để lên sàn!");

        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // Phương thức chuyển giao diện
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Quản lý đấu giá");
    }
}