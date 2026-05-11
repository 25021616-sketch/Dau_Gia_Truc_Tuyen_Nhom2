package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.user.UserRole;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Service.UserService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Session.Session;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class Dang_nhap_Controller extends Base_Admin_Controller {

    private static final String FXML_REGISTER = "dang_ky_tai_khoan.fxml";
    private static final String FXML_USER_HOME = "Man_hinh_chinh_Users.fxml";
    private static final String FXML_ADMIN_HOME = "Trang_chu_Admin.fxml";

    @FXML private CheckBox Dang_nhap_Admin;
    @FXML private PasswordField Mat_khau;
    @FXML private TextField Ten_dang_nhap;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        // 1. LẤY DỮ LIỆU TỪ FORM
        String username = Ten_dang_nhap.getText().trim();
        String password = Mat_khau.getText();

        boolean isAdminLogin = Dang_nhap_Admin != null && Dang_nhap_Admin.isSelected();

        // 2. KIỂM TRA RỖNG
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", Alert.AlertType.WARNING);
            return;
        }

        // 3. GỌI SERVICE KIỂM TRA ĐĂNG NHẬP
        User user = userService.login(username, password);

        // 4. KIỂM TRA KẾT QUẢ ĐĂNG NHẬP
        if (user == null) {
            showAlert("Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu.", Alert.AlertType.ERROR);
            return;
        }

        // =====================================================================
        // 5. XỬ LÝ SESSION & INJECT SERVICE (PHẦN QUAN TRỌNG NHẤT)
        // =====================================================================
        if (user.getRole() == UserRole.MEMBER) {
            // Ép kiểu sang Member để sử dụng các tính năng riêng biệt
            Member member = (Member) user;

            // Kích hoạt bộ máy xử lý đấu giá cho Member này
            member.setAuctionService(new AuctionServiceImpl());

            // Lưu vào Session để dùng chung toàn hệ thống
            Session.currentUser = member;
        } else {
            // Nếu là Admin thì lưu trực tiếp
            Session.currentUser = user;
        }

        System.out.println("DEBUG: Đã đăng nhập với tư cách: " + Session.currentUser.getUsername());

        // =====================================================================
        // 6. ĐIỀU HƯỚNG MÀN HÌNH (ADMIN vs USER)
        // =====================================================================
        if (isAdminLogin) {
            if (user.getRole() == UserRole.ADMIN) {
                showAlert("Thành công", "Chào mừng Admin quay trở lại.", Alert.AlertType.INFORMATION);
                navigateTo(event, FXML_ADMIN_HOME, "Trang Quản Trị");
            } else {
                showAlert("Từ chối truy cập", "Tài khoản này không có quyền quản trị viên.", Alert.AlertType.WARNING);
            }
        } else {
            // Đăng nhập User thường
            showAlert("Thành công", "Đăng nhập thành công.", Alert.AlertType.INFORMATION);
            navigateTo(event, FXML_USER_HOME, "Trang Chủ");
        }
    }

    @FXML
    public void handleSwitchToRegister(ActionEvent event) {
        navigateTo(event, FXML_REGISTER, "Đăng ký tài khoản");
    }


    // --- CÁC HÀM HỖ TRỢ (HELPERS) ---

    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            switchScene(event, fxmlFile, title);
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Không thể chuyển trang: " + fxmlFile, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}