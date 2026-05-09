package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction; // CẦN IMPORT CÁI NÀY
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
    @FXML private Label lblThoiGian;
    @FXML private Spinner<Integer> stepSpinner;
    @FXML private ComboBox<String> bidStepCombo;
    @FXML private TextField autoLimitField;

    private Auction currentAuction; // Đổi từ Item sang Auction
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTargetPrice());
    }

    /**
     * HÀM NHẬN DỮ LIỆU TỪ TRANG CHỦ (Đã sửa)
     */
    public void setAuctionData(Auction auction) {
        this.currentAuction = auction;
        Item item = auction.getItem(); // Lấy Item từ trong Auction ra

        // Giá hiện tại bây giờ lấy từ Auction
        this.currentPrice = auction.getCurrentPrice();


        // --- ĐỔ DỮ LIỆU Tên/Mô tả từ Item ---
        if (lblTenSanPham != null) lblTenSanPham.setText(item.getTenSanPham());
        if (lblMoTa != null) lblMoTa.setText(item.getMoTa());

        // Hiển thị giá hiện tại
        currentBidLabel.setText(formatter.format(currentPrice));

        // Hiển thị ảnh (Sửa lại hàm lấy ảnh của Item)
        if (item.getImagePaths() != null && !item.getImagePaths().isEmpty()) {
            productImage.setImage(new Image(item.getImagePaths().get(0)));
        }

        // Nạp bước giá (Lấy từ Auction)
        double buocGiaTuAuction = auction.getStepPrice();
        bidStepCombo.getItems().clear();
        bidStepCombo.getItems().add(formatter.format(buocGiaTuAuction));
        bidStepCombo.getSelectionModel().selectFirst();
        bidStepCombo.setDisable(true);

        updateTargetPrice();
    }

    private void updateTargetPrice() {
        if (currentAuction == null) return;
        try {
            int n = stepSpinner.getValue();
            double d = currentAuction.getStepPrice(); // Lấy từ Auction
            double targetPrice = currentPrice + (n * d);
            targetPriceLabel.setText(formatter.format(targetPrice));
        } catch (Exception e) {
            targetPriceLabel.setText(formatter.format(currentPrice));
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (currentAuction == null) return;
        try {
            double finalPrice = Double.parseDouble(targetPriceLabel.getText().replace(",", ""));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đặt giá");
            alert.setHeaderText("Bạn muốn đặt thầu mức giá: $" + targetPriceLabel.getText());

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    this.currentPrice = finalPrice;
                    // Cập nhật giá vào Auction (Không phải Item)
                    this.currentAuction.setCurrentPrice(finalPrice);

                    currentBidLabel.setText(formatter.format(currentPrice));
                    updateTargetPrice();
                    new Alert(Alert.AlertType.INFORMATION, "Đặt giá thành công!").show();
                }
            });
        } catch (Exception e) {
            System.err.println("Lỗi khi đặt giá: " + e.getMessage());
        }
    }

    // Các hàm khác giữ nguyên...
    @FXML private void handleClose(javafx.event.ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}