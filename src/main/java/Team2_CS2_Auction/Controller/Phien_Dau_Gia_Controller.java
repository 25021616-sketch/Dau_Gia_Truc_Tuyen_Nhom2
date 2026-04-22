package Team2_CS2_Auction.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class Phien_Dau_Gia_Controller {

    // --- Các thành phần UI khai báo trong FXML ---
    @FXML
    private Label currentBidLabel;     // Hiển thị giá hiện tại
    @FXML
    private Label targetPriceLabel;    // Hiển thị giá dự kiến (Giá hiện tại + n * bước giá)
    @FXML
    private Spinner<Integer> stepSpinner; // Ô chọn số bước nhảy n
    @FXML
    private ComboBox<String> bidStepCombo; // Ô chọn mức tăng (vd: $250)
    @FXML
    private TextField autoLimitField;  // Ô nhập giới hạn tự động
    @FXML
    private ImageView productImage;    // Ảnh sản phẩm

    // --- Biến lưu trữ dữ liệu ---
    private double currentPrice = 12000.0; // Giá gốc ban đầu (có thể lấy từ DB)

    /**
     * Hàm initialize() tự động chạy khi FXML được load
     */
    @FXML
    public void initialize() {
        // 1. Cấu hình Spinner cho số bước nhảy (n) từ 1 - 100
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        stepSpinner.setValueFactory(valueFactory);

        // 2. Thiết lập giá trị mặc định cho ComboBox nếu chưa chọn trong FXML
        if (bidStepCombo.getValue() == null) {
            bidStepCombo.setValue("$ 250");
        }

        // 3. Lắng nghe thay đổi trên Spinner (Khi nhấn mũi tên hoặc nhập số)
        stepSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateTargetPrice());

        // 4. Lắng nghe thay đổi trên ComboBox (Khi chọn mức tiền khác)
        bidStepCombo.valueProperty().addListener((obs, oldVal, newVal) -> calculateTargetPrice());

        // Tính toán hiển thị lần đầu
        updateUI();
    }

    /**
     * Hàm cập nhật UI giá hiện tại
     */
    private void updateUI() {
        currentBidLabel.setText(String.format("$ %,.0f", currentPrice));
        calculateTargetPrice();
    }

    /**
     * Logic tính toán: Giá dự kiến = Giá hiện tại + (n * mức tăng)
     */
    private void calculateTargetPrice() {
        try {
            int nSteps = stepSpinner.getValue();

            // Lấy giá trị từ ComboBox, loại bỏ ký tự $ và dấu phẩy
            String selectedValue = bidStepCombo.getValue().replaceAll("[^0-9]", "");
            double stepAmount = Double.parseDouble(selectedValue);

            double totalPotentialPrice = currentPrice + (nSteps * stepAmount);

            // Cập nhật nhãn hiển thị giá dự kiến
            targetPriceLabel.setText(String.format("$ %,.0f", totalPotentialPrice));
        } catch (Exception e) {
            System.err.println("Lỗi xử lý tính toán: " + e.getMessage());
        }
    }

    /**
     * Sự kiện khi nhấn nút "ĐẶT GIÁ THẦU NGAY"
     */
    @FXML
    private void handlePlaceBid() {
        String finalPrice = targetPriceLabel.getText();

        // Tạo Alert xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đấu thầu");
        alert.setHeaderText("Bạn có chắc chắn muốn đặt mức giá này?");
        alert.setContentText("Mức giá đặt: " + finalPrice);

        if (alert.showAndWait().get() == ButtonType.OK) {
            System.out.println("Đã gửi giá thầu " + finalPrice + " lên hệ thống.");
            // Ở đây bạn có thể cập nhật currentPrice = totalPotentialPrice và gọi updateUI()
        }
    }

    /**
     * Sự kiện khi nhấn nút "KÍCH HOẠT ĐẤU THẦU ỦY QUYỀN"
     */
    @FXML
    private void handleActivateAutoBid() {
        String limit = autoLimitField.getText();
        if (limit == null || limit.isEmpty()) {
            showError("Vui lòng nhập giới hạn giá tối đa cho Bot.");
            return;
        }
        System.out.println("Đã kích hoạt Bot tự động với giới hạn: " + limit);
    }

    /**
     * Sự kiện đóng cửa sổ
     */
    @FXML
    private void handleClose() {
        System.out.println("Đang đóng phiên đấu giá...");
        // Code để đóng Stage ở đây
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}