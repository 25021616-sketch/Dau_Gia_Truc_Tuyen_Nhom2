package Team2_CS2_Auction.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import Team2_CS2_Auction.Model.item.Item;

public class Phien_Dau_Gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private ImageView productImage;
    @FXML private Label currentBidLabel;
    @FXML private Label targetPriceLabel; // Nhãn màu xanh (Giá dự kiến)
    @FXML private Label lblTenSanPham;    // THÊM MỚI
    @FXML private Label lblMoTa;          // THÊM MỚI
    @FXML private Label lblThoiGian;      // Đồng bộ với FXML
    @FXML private Spinner<Integer> stepSpinner;
    @FXML private ComboBox<String> bidStepCombo;
    @FXML private TextField autoLimitField;

    private Item currentItem;
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình Spinner (n) từ 1 đến 100
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        // Lắng nghe Spinner. Mỗi khi bấm tăng/giảm n, nhãn màu xanh tự tính lại ngay
        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTargetPrice());
    }

    /**
     * HÀM NHẬN DỮ LIỆU TỪ TRANG CHỦ
     * Đổ tất cả thông tin từ Item sang giao diện chi tiết
     */
    public void setItemData(Item item) {
        this.currentItem = item;
        this.currentPrice = item.getGiaKhoiDiem();

        // --- ĐỔ DỮ LIỆU TÊN VÀ MÔ TẢ ---
        if (lblTenSanPham != null) lblTenSanPham.setText(item.getTenSanPham());
        if (lblMoTa != null) lblMoTa.setText(item.getMoTa());

        // Hiển thị giá hiện tại
        currentBidLabel.setText(formatter.format(currentPrice));

        // Hiển thị ảnh
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            productImage.setImage(new Image(item.getImagePath()));
        }

        // Nạp bước giá do người bán quy ước vào ComboBox
        double buocGiaTuModel = item.getBuocGia();
        bidStepCombo.getItems().clear();
        bidStepCombo.getItems().add(formatter.format(buocGiaTuModel));
        bidStepCombo.getSelectionModel().selectFirst();
        bidStepCombo.setDisable(true); // Khóa lại theo quy ước người bán

        updateTargetPrice();
    }

    private void updateTargetPrice() {
        if (currentItem == null) return;
        try {
            int n = stepSpinner.getValue();
            double d = currentItem.getBuocGia();
            double targetPrice = currentPrice + (n * d);
            targetPriceLabel.setText(formatter.format(targetPrice));
        } catch (Exception e) {
            targetPriceLabel.setText(formatter.format(currentPrice));
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (currentItem == null) return;
        try {
            double finalPrice = Double.parseDouble(targetPriceLabel.getText().replace(",", ""));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đặt giá");
            alert.setHeaderText("Bạn muốn đặt thầu mức giá: $" + targetPriceLabel.getText());
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    this.currentPrice = finalPrice;
                    this.currentItem.setGiaKhoiDiem(finalPrice);
                    currentBidLabel.setText(formatter.format(currentPrice));
                    updateTargetPrice();
                    new Alert(Alert.AlertType.INFORMATION, "Đặt giá thành công!").show();
                }
            });
        } catch (Exception e) {
            System.err.println("Lỗi khi đặt giá: " + e.getMessage());
        }
    }

    @FXML
    private void handleActivateAutoBid() {
        String limit = autoLimitField.getText();
        if (limit.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập giới hạn tối đa!").show();
            return;
        }
        System.out.println("Kích hoạt đấu giá tự động đến mức: " + limit);
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}