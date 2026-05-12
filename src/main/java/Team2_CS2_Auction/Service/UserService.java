package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.*;
import Team2_CS2_Auction.Repository.UserRepository;
import Team2_CS2_Auction.Session.Session;

public class UserService {
    private UserRepository userRepository = new UserRepository();

    public void handleRegisterLogic(String username, String phone, String password, String confirm) throws Exception {
        // Logic kiểm tra khớp mật khẩu
        if (!password.equals(confirm)) throw new Exception("Mật khẩu nhập lại không khớp.");

        if (userRepository.existsByPhone(phone)) throw new Exception("Số điện thoại đã tồn tại.");

        // Khởi tạo Member mới (Role tự động được set trong Constructor của Member)
        Member newMember = new Member(username, password);
        newMember.setPhone(phone);

        if (!userRepository.register(newMember)) {
            throw new Exception("Đăng ký thất bại (Tên đăng nhập có thể đã tồn tại).");
        }
    }

    public User handleLoginLogic(String username, String password, boolean isAdminLogin) throws Exception {
        User user = userRepository.login(username, password);

        if (user == null) throw new Exception("Sai tên đăng nhập hoặc mật khẩu.");

        // --- LOGIC MỚI: KIỂM TRA TRẠNG THÁI KHÓA ---
        // Kiểm tra nếu status của user là BANNED thì không cho vào
        if (user.getStatus() != null && "BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        }
        // ------------------------------------------

        // Kiểm tra quyền Admin dựa trên Role Enum từ Model
        if (isAdminLogin && user.getRole() != UserRole.ADMIN) {
            throw new Exception("Bạn không có quyền quản trị viên.");
        }

        // Ép kiểu và Inject Service cho Member
        if (user instanceof Member member) {
            member.setAuctionService(new AuctionServiceImpl());
            Session.currentUser = member;
        } else {
            Session.currentUser = user;
        }
        return user;
    }
}