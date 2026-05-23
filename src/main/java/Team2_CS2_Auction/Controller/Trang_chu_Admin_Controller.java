package Team2_CS2_Auction.Controller;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class Trang_chu_Admin_Controller
        extends Base_Admin_Controller
        implements Initializable {
    @FXML
    private Button Dashboard;

    @FXML
    private Button history;

    @FXML
    private Button inventory;

    @FXML
    private Button users;

    // Nút mới thêm vào
    @FXML
    private Button btnLogout;

    @FXML
    private Label lblRevenue;

    @FXML
    private Label lblTotalSessions;

    private final AuctionRepositoryImpl repo =
            new AuctionRepositoryImpl();

    @FXML
    public void handleGoToUser(ActionEvent event) {
        // Gõ đúng tên file trong ảnh: Admin_quan_li_User.fxml
        switchScene(event, "Admin_quan_li_User.fxml", "Quản lý người dùng");
    }

    @FXML
    public void handleGoToInventory(ActionEvent event) {
        // Gõ đúng tên file trong ảnh: Admin_quan_li_dau_gia.fxml
        switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá");
    }

    @FXML
    public void handleGoToHistory(ActionEvent event) {
        // Mở trang Lịch sử đấu giá
        switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Quay lại trang đăng nhập (Tên file: dang_nhap.fxml)
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loadDashboardData();
    }

    private void loadDashboardData() {

        try {

            double revenue = repo.getTotalRevenue();

            int totalSessions =
                    repo.getTotalSessionsOrganized();

            lblRevenue.setText(
                    "$" + String.format("%,.0f", revenue)
            );

            lblTotalSessions.setText(
                    String.format("%,d", totalSessions)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}