package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Model.user.Admin;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Model.user.UserRole;

public class UserDTO {
    public int id;
    public String username;
    public String phone;
    public String role;
    public double balance;
    public String status;

    // Chuyển từ User gốc sang DTO để gửi đi an toàn
    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.phone = user.getPhone();
        dto.role = user.getRole().name();
        dto.balance = user.getBalance();
        dto.status = user.getStatus();
        return dto;
    }

    // Chuyển từ DTO nhận được qua mạng trở lại thành Model gốc
    public User toUser() {
        User user;
        if ("ADMIN".equals(role)) {
            user = new Admin(id, username, "HIDDEN", phone);
        } else {
            user = new Member(id, username, "HIDDEN", phone);
        }
        user.setBalance(balance);
        user.setStatus(status);
        // Ở máy Client (JavaFX), mật khẩu thường không cần thiết nữa nên ta có thể để trống
        return user;
    }
}
