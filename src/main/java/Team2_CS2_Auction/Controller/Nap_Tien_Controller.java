package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Service.UserService;
import Team2_CS2_Auction.Repository.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Nap_Tien_Controller {
    @FXML private TextField txtAmount;
    @FXML private Label lblMessage;
    @FXML private Label lblCurrentBalance; // Ánh xạ từ FXML

    private final UserService userService = new UserService();
    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    public void setUserData(User user) {
        this.currentUser = user;
        refreshBalance(); // Hiện số dư ngay khi vừa mở popup
    }

    // Hàm lấy số dư mới nhất từ Database
    private void refreshBalance() {
        if (currentUser != null) {
            double balance = userRepository.getBalance(currentUser.getId());
            lblCurrentBalance.setText(String.format("%.2f $", balance));
        }
    }

    @FXML
    private void handleConfirmDeposit(ActionEvent event) {
        try {
            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) {
                lblMessage.setText("Vui lòng nhập số tiền!");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            boolean success = userService.handleDeposit(currentUser.getId(), amount);

            if (success) {
                lblMessage.setStyle("-fx-text-fill: #2E7D32;"); // Màu xanh lá
                lblMessage.setText("✅ Nạp tiền thành công!");
                txtAmount.clear();
                refreshBalance(); // Cập nhật lại số tiền hiển thị để user xem
            }

        } catch (NumberFormatException e) {
            lblMessage.setStyle("-fx-text-fill: #d32f2f;");
            lblMessage.setText("Số tiền phải là con số!");
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: #d32f2f;");
            lblMessage.setText(e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}