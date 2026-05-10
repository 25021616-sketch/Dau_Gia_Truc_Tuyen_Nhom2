package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import Team2_CS2_Auction.Model.item.Item;

public class Admin_quan_li_dau_gia_Controller extends Base_Admin_Controller implements Initializable {


    // --- ĐIỀU HƯỚNG ---
    @FXML public void handleGoToDashboard(ActionEvent event) { switchScene(event, "Trang_chu_Admin.fxml", "Bảng điều khiển"); }
    @FXML public void handleGoToUsers(ActionEvent event) { switchScene(event, "Admin_quan_li_User.fxml", "Quản lý người dùng"); }
    @FXML public void handleGoToHistory(ActionEvent event) { switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá"); }
    @FXML public void handleAddNewListing(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Niêm yết mới"); }
}