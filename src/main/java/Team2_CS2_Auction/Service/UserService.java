package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Repository.UserRepository;

public class UserService {

    private UserRepository userRepository = new UserRepository();

    public User login(String username, String password) {

        if(username == null || password == null){
            return null;
        }

        if(username.isEmpty() || password.isEmpty()){
            return null;
        }

        return userRepository.login(username, password);
    }

    public boolean existsByPhone(String phone){
        return userRepository.existsByPhone(phone);
    }

    public boolean register(User user) {

        // 1. Kiểm tra tên đăng nhập trống
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }

        // 2. Kiểm tra độ dài tên đăng nhập
        if (user.getUsername().length() < 4) {
            return false;
        }

        // 3. Kiểm tra mật khẩu trống
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return false;
        }

        // 4. Kiểm tra mật khẩu tối thiểu 6 ký tự
        if (user.getPassword().length() < 6) {
            return false;
        }

        // 5. Kiểm tra số điện thoại trống
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return false;
        }

        // 6. Kiểm tra định dạng số điện thoại Việt Nam (10 số, bắt đầu bằng 0)
        if (!user.getPhone().matches("0\\d{9}")) {
            return false;
        }

        // 7. Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.existsByPhone(user.getPhone())) {
            return false;
        }

        // 8. Nếu hợp lệ thì lưu vào database
        return userRepository.register(user);
    }
}