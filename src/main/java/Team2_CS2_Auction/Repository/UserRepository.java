package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.user.*;
import Team2_CS2_Auction.util.DBConnection;
import Team2_CS2_Auction.util.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User login(String username, String password) {
        String sql = "SELECT * FROM user WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password").trim();
                String hashedInput = PasswordUtils.hashSha256(password);

                if (!storedPassword.equals(password) && !storedPassword.equals(hashedInput)) {
                    return null;
                }

                int id = rs.getInt("id");
                String phone = rs.getString("phone");
                String roleRaw = rs.getString("role");

                if ("ADMIN".equalsIgnoreCase(roleRaw)) {
                    return new Admin(id, username, storedPassword, phone);
                } else {
                    return new Member(id, username, storedPassword, phone);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean register(User user) {
        String sql = "INSERT INTO user(username, password, phone, role) VALUES(?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtils.hashSha256(user.getPassword()));
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole().name());

            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean existsByPhone(String phone) {
        String sql = "SELECT 1 FROM user WHERE phone = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            return ps.executeQuery().next();
        } catch (Exception e) { return false; }
    }

    /**
     * Lấy toàn bộ danh sách Member
     */
    public List<Member> findAllMembers() {
        List<Member> list = new ArrayList<>();

        // 1. Lấy dữ liệu từ Database
        // Lưu ý: Vẫn giữ 'balance' trong query để Object Member đủ dữ liệu, dù không hiện lên bảng
        String sql = "SELECT id, username, password, phone, role, created_at, balance FROM auction_db.user WHERE role = 'MEMBER'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Member m = new Member(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone")
                );

                // QUAN TRỌNG: Dùng setBalance thay vì addBalance để không bị lỗi nạp tiền <= 0
                m.setBalance(rs.getDouble("balance"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    m.setCreatedAt(ts.toLocalDateTime());
                }

                list.add(m);
            }
            System.out.println("✅ [DEBUG] Đã load thành công: " + list.size() + " thành viên.");

        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Cập nhật trạng thái Ban/Unban
     */
    public boolean updateStatus(int userId, String newStatus) {
        // Đảm bảo bảng user tồn tại trong auction_db
        String sql = "UPDATE auction_db.user SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}