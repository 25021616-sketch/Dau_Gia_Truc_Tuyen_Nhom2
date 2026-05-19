package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.BidHistory;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class Admin_quan_li_lich_su_Controller
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

    @FXML
    private javafx.scene.control.Label lblTotalRevenue;

    @FXML
    private javafx.scene.control.Label lblTotalSessions;

    @FXML
    private TableView<BidHistory> historyTable;

    @FXML
    private TableColumn<BidHistory, Integer> colSTT;

    @FXML
    private TableColumn<BidHistory, String> colID;

    @FXML
    private TableColumn<BidHistory, String> colTenSanPham;

    @FXML
    private TableColumn<BidHistory, String> colNguoiThang;

    @FXML
    private TableColumn<BidHistory, Double> colGiaChot;

    @FXML
    private TableColumn<BidHistory, String> colThoiDiem;

    private final AuctionRepositoryImpl repo =
            new AuctionRepositoryImpl();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colSTT.setCellValueFactory(
                new PropertyValueFactory<>("stt"));

        colID.setCellValueFactory(
                new PropertyValueFactory<>("auctionId"));

        colTenSanPham.setCellValueFactory(
                new PropertyValueFactory<>("productName"));

        colNguoiThang.setCellValueFactory(
                new PropertyValueFactory<>("bidderName"));

        colGiaChot.setCellValueFactory(
                new PropertyValueFactory<>("bidAmount"));

        colThoiDiem.setCellValueFactory(
                new PropertyValueFactory<>("bidTime"));

        loadHistory();
    }

    private void loadHistory() {

        try {

            ObservableList<BidHistory> list =
                    FXCollections.observableArrayList(
                            repo.getBidHistory()
                    );
            System.out.println("SIZE = " + list.size());
            historyTable.setItems(list);

            double totalRevenue = 0;
            for (BidHistory b : list) {
                totalRevenue += b.getBidAmount();
            }
            int totalSessions = list.size();

            if (lblTotalSessions != null) {
                lblTotalSessions.setText(String.format("%,d", totalSessions));
            }
            if (lblTotalRevenue != null) {
                lblTotalRevenue.setText("$" + String.format("%,.0f", totalRevenue));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CHUYỂN GIAO DIỆN
    // =========================

    @FXML
    public void handleGoToUsers(ActionEvent event) {
        switchScene(event,
                "Admin_quan_li_User.fxml",
                "Quản lý người dùng");
    }

    @FXML
    public void handleGoToInventory(ActionEvent event) {
        switchScene(event,
                "Admin_quan_li_dau_gia.fxml",
                "Quản lý đấu giá");
    }

    @FXML
    public void handleGoToDashboard(ActionEvent event) {
        switchScene(event,
                "Trang_chu_Admin.fxml",
                "Trang chủ Admin");
    }
}