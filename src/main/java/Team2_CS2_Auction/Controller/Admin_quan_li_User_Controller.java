package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AdminService;
import Team2_CS2_Auction.Service.AdminServiceImpl;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Admin_quan_li_User_Controller extends Base_Admin_Controller {

    @FXML private TableView<Member> userTable;
    @FXML private TableColumn<Member, Number> colSTT;
    @FXML private TableColumn<Member, Integer> colID;
    @FXML private TableColumn<Member, String> colTen;
    @FXML private TableColumn<Member, Double> colTaiSan; // Cột mới
    @FXML private TableColumn<Member, String> colNgayDangKy;
    @FXML private TextField txtSearch;

    private AdminService adminService = new AdminServiceImpl(new AuctionRepositoryImpl());
    private ObservableList<Member> masterData = FXCollections.observableArrayList();
    private FilteredList<Member> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        // 1. Số thứ tự (STT)
        colSTT.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(userTable.getItems().indexOf(column.getValue()) + 1));

        // 2. ID Người dùng
        colID.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));

        // 3. Tên người dùng
        colTen.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        // 4. TÀI SẢN (SỐ DƯ) - Đã format hiển thị tiền tệ
        colTaiSan.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getBalance()));

        colTaiSan.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (empty || balance == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", balance));
                    setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;"); // Chữ xanh đậm cho tài sản
                }
            }
        });

        // 5. Ngày đăng ký
        colNgayDangKy.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("N/A");
        });

        filteredData = new FilteredList<>(masterData, p -> true);

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(member -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (String.valueOf(member.getId()).toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (member.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });
        }

        SortedList<Member> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void loadData() {
        try {
            masterData.clear();
            List<Member> members = adminService.getMemberList();
            if (members != null && !members.isEmpty()) {
                masterData.addAll(members);
                System.out.println("✅ Đã load " + members.size() + " thành viên.");
            }
            userTable.refresh();
        } catch (Exception e) {
            System.err.println("❌ Lỗi load dữ liệu: " + e.getMessage());
        }
    }

    @FXML
    public void handleBanUser(ActionEvent event) {
        Member selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                adminService.banMember(selected.getId());
                new Alert(Alert.AlertType.INFORMATION, "Đã khóa tài khoản: " + selected.getUsername()).show();
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    @FXML
    public void handleUnbanUser(ActionEvent event) {
        Member selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                adminService.unbanMember(selected.getId());
                new Alert(Alert.AlertType.INFORMATION, "Đã mở khóa tài khoản: " + selected.getUsername()).show();
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Quay lại trang đăng nhập (Tên file: dang_nhap.fxml)
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
    }

    // Các hàm chuyển Scene
    @FXML public void handleGoToInventory(ActionEvent event) { switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá"); }
    @FXML public void handleGoToHistory(ActionEvent event) { switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá"); }
    @FXML public void handleGoToDashboard(ActionEvent event) { switchScene(event, "Trang_chu_Admin.fxml", "Trang chủ Admin"); }
}