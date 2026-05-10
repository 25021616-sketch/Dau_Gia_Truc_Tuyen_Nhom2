package Team2_CS2_Auction.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class Admin_quan_li_User_Controller extends Base_Admin_Controller {
    // Đảm bảo class này đã có: extends Admin_Base_Controller
    @FXML
    private Button Dashboard;

    @FXML
    private Button history;

    @FXML
    private Button inventory;

    @FXML
    private Button users;


    @FXML
    public void handleGoToInventory(ActionEvent event) {switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá");}
    @FXML
    public void handleGoToHistory(ActionEvent event) {switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá");}
    @FXML
    public void handleGoToDashboard(ActionEvent event) {switchScene(event, "Trang_chu_Admin.fxml", "Trang chủ Admin");}
}