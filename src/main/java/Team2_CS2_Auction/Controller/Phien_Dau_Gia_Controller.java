package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class Phien_Dau_Gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private ImageView productImage;
    @FXML private Label currentBidLabel;
    @FXML private Label targetPriceLabel;
    @FXML private Label lblTenSanPham;
    @FXML private Label lblMoTa;
    @FXML private Label lblThoiGian; // Có thể dùng để hiển thị countdown nếu cần
    @FXML private Spinner<Integer> stepSpinner;
    @FXML private ComboBox<String> bidStepCombo;
    @FXML private TextField autoLimitField;

    private Auction currentAuction;
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình Spinner: tối thiểu 1 bước, tối đa 100 bước, mặc định 1
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        // Lắng nghe thay đổi của Spinner để cập nhật giá dự kiến (Target Price)
        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTargetPrice());
    }

    /**
     * HÀM NHẬN DỮ LIỆU TỪ MÀN HÌNH CHÍNH (Đã đồng bộ Item 1 ảnh)
     */
    public void setAuctionData(Auction auction) {
        if (auction == null) return;

        this.currentAuction = auction;
        Item item = auction.getItem();

        // 1. Lấy giá hiện tại từ Auction
        this.currentPrice = auction.getCurrentPrice();

        // 2. Đổ dữ liệu văn bản từ Item (Sử dụng getTenSanPham)
        if (lblTenSanPham != null) lblTenSanPham.setText(item.getTenSanPham());
        if (lblMoTa != null) lblMoTa.setText(item.getMoTa());

        // 3. Hiển thị giá hiện tại
        currentBidLabel.setText(formatter.format(currentPrice));

        // 4. Xử lý ảnh (SỬA LỖI: Sử dụng getImagePath() thay vì List)
        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                productImage.setImage(new Image(path, true));
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh sản phẩm: " + e.getMessage());
            }
        }

        // 5. Nạp bước giá cố định từ Auction
        double buocGiaTuAuction = auction.getStepPrice();
        bidStepCombo.getItems().clear();
        bidStepCombo.getItems().add(formatter.format(buocGiaTuAuction));
        bidStepCombo.getSelectionModel().selectFirst();
        bidStepCombo.setDisable(true); // Khóa combo để người dùng chỉ dùng Spinner nhân lên

        updateTargetPrice();
    }

    /**
     * Cập nhật mức giá mà người dùng muốn đặt (Giá hiện tại + N * Bước giá)
     */
    private void updateTargetPrice() {
        if (currentAuction == null) return;
        try {
            int n = stepSpinner.getValue();
            double d = currentAuction.getStepPrice();
            double targetPrice = currentPrice + (n * d);
            targetPriceLabel.setText(formatter.format(targetPrice));
        } catch (Exception e) {
            targetPriceLabel.setText(formatter.format(currentPrice));
        }
    }

    /**
     * Xử lý khi nhấn nút Đặt giá
     */
    @FXML
    private void handlePlaceBid() {
        if (currentAuction == null) return;
        try {
            // Loại bỏ dấu phẩy trong chuỗi định dạng để parse sang số
            String priceText = targetPriceLabel.getText().replace(",", "").replace(".", "");
            double finalPrice = Double.parseDouble(priceText);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đặt giá");
            alert.setHeaderText("Bạn muốn đặt thầu mức giá: $" + targetPriceLabel.getText());
            alert.setContentText("Hành động này không thể hoàn tác.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Cập nhật giá mới vào logic local
                    this.currentPrice = finalPrice;
                    this.currentAuction.setCurrentPrice(finalPrice);

                    // Cập nhật UI
                    currentBidLabel.setText(formatter.format(currentPrice));
                    updateTargetPrice();

                    new Alert(Alert.AlertType.INFORMATION, "Đặt giá thành công!").show();
                }
            });
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi định dạng giá thầu!").show();
        }
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        // Quay về màn hình chính, nhớ stop bất kỳ luồng dữ liệu nào nếu có
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    @FXML
    private void handleActivateAutoBid(javafx.event.ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Tính năng ủy quyền đấu giá đang được phát triển!").show();
    }
}