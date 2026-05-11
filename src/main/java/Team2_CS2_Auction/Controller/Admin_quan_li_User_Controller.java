package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.AdminService;
import Team2_CS2_Auction.Service.AdminServiceImpl;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML private TableColumn<Member, String> colNgayDangKy;

    private AdminService adminService = new AdminServiceImpl(new AuctionRepositoryImpl());
    private ObservableList<Member> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        // SỬA TẠI ĐÂY: Sử dụng Lambda thay vì PropertyValueFactory để chắc chắn lấy được dữ liệu từ lớp cha User

        // 1. Kết nối cột ID
        colID.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));

        // 2. Kết nối cột Tên người dùng
        colTen.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        // 3. Định dạng ngày đăng ký (Giữ nguyên vì phần này của bạn đã chạy tốt)
        colNgayDangKy.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("N/A");
        });

        // 4. Tạo số thứ tự (STT) tự động
        colSTT.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(userTable.getItems().indexOf(column.getValue()) + 1));

        userTable.setItems(masterData);
    }

    private void loadData() {
        try {
            masterData.clear();
            List<Member> members = adminService.getMemberList();
            if (members != null && !members.isEmpty()) {
                masterData.addAll(members);
                System.out.println("✅ Đã load " + members.size() + " thành viên vào TableView.");
            } else {
                System.out.println("⚠️ Không có thành viên nào để hiển thị.");
            }
            userTable.refresh();
        } catch (Exception e) {
            System.err.println("❌ Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBanUser(ActionEvent event) {
        Member selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                adminService.banMember(selected.getId());
                showAlert("Thành công", "Đã khóa tài khoản: " + selected.getUsername(), Alert.AlertType.INFORMATION);
                loadData();
            } catch (Exception e) {
                showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một người dùng từ bảng!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleUnbanUser(ActionEvent event) {
        Member selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                adminService.unbanMember(selected.getId());
                showAlert("Thành công", "Đã mở khóa tài khoản: " + selected.getUsername(), Alert.AlertType.INFORMATION);
                loadData();
            } catch (Exception e) {
                showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một người dùng!", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML public void handleGoToInventory(ActionEvent event) { switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá"); }
    @FXML public void handleGoToHistory(ActionEvent event) { switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá"); }
    @FXML public void handleGoToDashboard(ActionEvent event) { switchScene(event, "Trang_chu_Admin.fxml", "Trang chủ Admin"); }
}