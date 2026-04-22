package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Admin_quan_li_dau_gia_Controller extends Base_Admin_Controller {

    // Các fx:id khớp hoàn toàn với FXML
    @FXML
    private Button Dashboard;

    @FXML
    private Button users;

    @FXML
    private Button inventory;

    @FXML
    private Button history;

    @FXML
    public void handleGoToDashboard(ActionEvent event) {
        switchScene(event, "Trang_chu_Admin.fxml", "Bảng điều khiển");
    }

    @FXML
    public void handleGoToUsers(ActionEvent event) {
        switchScene(event, "Admin_quan_ly_User.fxml", "Quản lý người dùng");
    }

    @FXML
    public void handleGoToHistory(ActionEvent event) {
        switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá");
    }

    // Nút inventory (Kho hàng) thường không cần handle nếu đang ở chính trang đó,
    // nhưng bạn có thể thêm handleGoToInventory nếu cần dùng cho các trang khác.
}