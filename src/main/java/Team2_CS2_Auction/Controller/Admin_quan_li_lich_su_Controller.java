package Team2_CS2_Auction.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class Admin_quan_li_lich_su_Controller extends Base_Admin_Controller {
    @FXML
    private Button Dashboard;

    @FXML
    private Button history;

    @FXML
    private Button inventory;

    @FXML
    private Button users;
    // Phần chuyển giao diện
    @FXML
    public void handleGoToUsers(ActionEvent event) {
        switchScene(event, "Admin_quan_ly_User.fxml", "Quản lý người dùng");
    }
    @FXML
    public void handleGoToInventory(ActionEvent event) {
        switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá");
    }
    @FXML
    public void handleGoToDashboard(ActionEvent event) {
        switchScene(event, "Trang_chu_Admin.fxml", "Trang chủ Admin");
    }
}
