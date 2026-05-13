package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Session.Session;
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

    private Auction currentAuction;
    private double currentPrice;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // THỐNG NHẤT DÙNG AUCTION SERVICE
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTargetPrice());
    }

    public void setAuctionData(Auction auction) {
        if (auction == null) return;

        this.currentAuction = auction;
        Item item = auction.getItem();
        this.currentPrice = auction.getCurrentPrice();

        if (lblTenSanPham != null) lblTenSanPham.setText(item.getTenSanPham());
        if (lblMoTa != null) lblMoTa.setText(item.getMoTa());
        currentBidLabel.setText(formatter.format(currentPrice));

        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                productImage.setImage(new Image(path, true));
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh sản phẩm: " + e.getMessage());
            }
        }

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
            double d = currentAuction.getStepPrice();
            double targetPrice = currentPrice + (n * d);
            targetPriceLabel.setText(formatter.format(targetPrice));
        } catch (Exception e) {
            targetPriceLabel.setText(formatter.format(currentPrice));
        }
    }

    @FXML
    private void handlePlaceBid() {
        try {
            Member currentUser = (Member) Team2_CS2_Auction.Session.Session.currentUser;
            double finalPrice = Double.parseDouble(targetPriceLabel.getText().replaceAll("[^\\d]", "")); // Lấy số sạch

            auctionService.placeBid(currentUser, currentAuction.getAuctionId(), finalPrice);

            // ĐỒNG BỘ LẠI DỮ LIỆU ĐỂ ĐẶT TIẾP LẦN SAU
            this.currentAuction.setCurrentPrice(finalPrice);
            this.currentPrice = finalPrice;
            currentBidLabel.setText(formatter.format(finalPrice));
            updateTargetPrice();

            new Alert(Alert.AlertType.INFORMATION, "✅ Đặt giá thành công!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }

    @FXML
    private void handleActivateAutoBid() {
        new Alert(Alert.AlertType.INFORMATION, "Tính năng đấu giá tự động sẽ sớm ra mắt!").show();
    }
}