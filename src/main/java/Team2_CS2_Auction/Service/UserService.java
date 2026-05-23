package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.*;
import Team2_CS2_Auction.Repository.UserRepository;
import Team2_CS2_Auction.Session.Session;

public class UserService {
    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Logic Đăng ký tài khoản
     */
    public void handleRegisterLogic(
            String username,
            String phone,
            String password,
            String confirmPassword
    ) throws Exception {

        // ===== USERNAME =====
        if (username.isEmpty()) {

            throw new Exception(
                    "Vui lòng nhập tên đăng nhập."
            );
        }

        if (username.length() < 4) {

            throw new Exception(
                    "Tên đăng nhập phải từ 4 ký tự trở lên."
            );
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {

            throw new Exception(
                    "Tên đăng nhập không được chứa ký tự đặc biệt."
            );
        }

        // ===== PHONE =====
        if (phone.isEmpty()) {

            throw new Exception(
                    "Vui lòng nhập số điện thoại."
            );
        }

        if (!phone.matches("^0\\d{9}$")) {

            throw new Exception(
                    "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0."
            );
        }

        if (userRepository.isPhoneExists(phone)) {

            throw new Exception(
                    "Số điện thoại đã được sử dụng."
            );
        }

        // ===== PASSWORD =====
        if (password.isEmpty()) {

            throw new Exception(
                    "Vui lòng nhập mật khẩu."
            );
        }

        if (password.length() < 6) {

            throw new Exception(
                    "Mật khẩu phải có ít nhất 6 ký tự."
            );
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {

            throw new Exception(
                    "Mật khẩu phải chứa cả chữ và số."
            );
        }

        // ===== CONFIRM PASSWORD =====
        if (!password.equals(confirmPassword)) {

            throw new Exception(
                    "Mật khẩu nhập lại không khớp."
            );
        }

        // ===== USERNAME EXISTS =====
        if (userRepository.isUsernameExists(username)) {

            throw new Exception(
                    "Tên đăng nhập đã tồn tại."
            );
        }

        // ===== CREATE ACCOUNT =====
        Member user = new Member(username, password);

        user.setPhone(phone);

        boolean success = userRepository.registerUser(user);

        if (success) {

            System.out.println("Đăng ký thành công");

        } else {

            throw new Exception(
                    "Đăng ký thất bại. Có thể tài khoản đã tồn tại."
            );
        }
    }

    /**
     * Logic Đăng nhập - Đã bao gồm lưu vào Session
     */
    public User handleLoginLogic(String username, String password, boolean isAdminLogin) throws Exception {
        User user = userRepository.login(username, password);

        if (user == null) {
            throw new Exception("Sai tên đăng nhập hoặc mật khẩu.");
        }

        // Kiểm tra trạng thái khóa
        if (user.getStatus() != null && "BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        }

        // Kiểm tra quyền Admin
        if (isAdminLogin && user.getRole() != UserRole.ADMIN) {
            throw new Exception("Bạn không có quyền quản trị viên.");
        }
        
        if (!isAdminLogin && user.getRole() == UserRole.ADMIN) {
            throw new Exception("Quản trị viên không thể đăng nhập vào giao diện người dùng.");
        }

        // Khởi tạo Session
        Session.currentUser = user;

        // Nếu là Member, đảm bảo khởi tạo đúng AuctionService
        if (user instanceof Member member) {
            member.setAuctionService(new AuctionServiceImpl());
        }

        return user;
    }

    /**
     * Logic Nạp tiền - Đã đồng bộ hóa Session
     * Quan trọng: Giữ lại ở đây vì nạp tiền thuộc về quản lý User/Ví tiền
     */
    public boolean handleDeposit(int userId, double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Số tiền nạp phải lớn hơn 0!");
        }

        // 1. Cập nhật tiền vào Database
        boolean isSuccess = userRepository.depositMoney(userId, amount);

        if (isSuccess) {
            // 2. CẬP NHẬT LẠI VÍ TIỀN TRONG SESSION
            // Đồng bộ ngay lập tức để khi User quay lại màn hình đấu giá, số dư đã mới nhất
            if (Session.currentUser instanceof Member member && member.getId() == userId) {
                double latestBalance = userRepository.getBalance(userId);
                member.setBalance(latestBalance);
                System.out.println("===> DEBUG: Nạp thành công. Số dư mới trong Session: " + member.getBalance());
            }
            return true;
        } else {
            throw new Exception("Lỗi hệ thống: Không thể cập nhật tiền vào cơ sở dữ liệu.");
        }
    }

    /**
     * Lưu ý: Hàm handlePlaceBid đã được chuyển sang AuctionService.
     * Không nên để ở đây để tránh xung đột logic.
     */
}