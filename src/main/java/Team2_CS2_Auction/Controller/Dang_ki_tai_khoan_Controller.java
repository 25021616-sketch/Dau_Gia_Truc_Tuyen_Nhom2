package Team2_CS2_Auction.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Dang_ki_tai_khoan_Controller extends Base_Admin_Controller {
    @FXML
    private Button Dang_ky;
    @FXML
    private Hyperlink Dang_nhap_ngay;
    @FXML
    private PasswordField Dat_mat_khau;
    @FXML
    private PasswordField Nhap_lai_mat_khau;
    @FXML
    private TextField Sdt_dang_ki;
    @FXML
    private TextField Ten_dang_ki;
    @FXML
    public void handleSwitchToLogin(ActionEvent event) {
        switchScene(event, "dang_nhap.fxml", "Trang Đăng nhập");
    }
}

