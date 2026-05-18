package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.user.*;
import Team2_CS2_Auction.util.DBConnection;
import Team2_CS2_Auction.util.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User login(String username, String password) {
        // 1. SỬA QUERY: Thêm cột 'status' vào câu lệnh SELECT
        String sql = "SELECT id, username, password, phone, role, status FROM user WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password").trim();
                String hashedInput = PasswordUtils.hashSha256(password);

                if (storedPassword.equals(password) || storedPassword.equals(hashedInput)) {
                    int id = rs.getInt("id");
                    String phone = rs.getString("phone");
                    String roleRaw = rs.getString("role");
                    String status = rs.getString("status"); // Lấy status từ DB

                    User user;
                    if ("ADMIN".equalsIgnoreCase(roleRaw)) {
                        user = new Admin(id, username, storedPassword, phone);
                    } else {
                        user = new Member(id, username, storedPassword, phone);
                    }

                    // 2. QUAN TRỌNG: Gán status vào Object User
                    user.setStatus(status != null ? status : "ACTIVE");

                    return user;
                } else {
                    System.out.println("❌ Sai mật khẩu!");
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(User user) {
        // Mặc định đăng ký mới là ACTIVE
        String sql = "INSERT INTO user(username, password, phone, role, status) VALUES(?,?,?,?,'ACTIVE')";
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
     * Lấy toàn bộ danh sách Member (Sửa để lấy thêm status hiển thị cho Admin)
     */
    public List<Member> findAllMembers() {
        List<Member> list = new ArrayList<>();
        // SỬA QUERY: Thêm status
        String sql = "SELECT id, username, password, phone, role, created_at, balance, status FROM auction_db.user WHERE role = 'MEMBER'";

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

                m.setBalance(rs.getDouble("balance"));
                // Gán status để Admin biết ai đang bị khóa
                m.setStatus(rs.getString("status"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    m.setCreatedAt(ts.toLocalDateTime());
                }

                list.add(m);
            }
            System.out.println("✅ [DEBUG] Đã load thành công: " + list.size() + " thành viên.");

        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Lỗi SQL: " + e.getMessage());
        }

        return list;
    }

    /**
     * Cập nhật trạng thái Ban/Unban
     */
    public boolean updateStatus(int userId, String newStatus) {
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
    public boolean depositMoney(int userId, double amount) {
        // Truy vấn cộng dồn tiền vào số dư (balance) hiện tại
        String sql = "UPDATE user SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi SQL tại UserRepository.depositMoney: " + e.getMessage());
            return false;
        }
    }
    public double getBalance(int userId) {
        String sql = "SELECT balance FROM user WHERE id = ?";
        try (Connection conn = Team2_CS2_Auction.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public boolean updateBalance(int userId, double newBalance) {
        String sql = "UPDATE user SET balance = ? WHERE id = ?";
        try (Connection conn = Team2_CS2_Auction.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    // =========================
    // KIỂM TRA USERNAME TỒN TẠI
    // =========================
    public boolean isUsernameExists(String username) {

        String sql =
                "SELECT 1 FROM user " +
                        "WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) " +
                        "LIMIT 1";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================
// ALIAS KIỂM TRA PHONE
// =========================
    public boolean isPhoneExists(String phone) {

        return existsByPhone(phone);
    }

    // =========================
// ALIAS ĐĂNG KÝ USER
// =========================
    public boolean registerUser(User user) {

        String sql =
                "INSERT INTO user(username,password,phone,role,status) " +
                        "VALUES(?,?,?,?,?)";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, user.getUsername());

            ps.setString(
                    2,
                    PasswordUtils.hashSha256(user.getPassword())
            );

            ps.setString(3, user.getPhone());

            ps.setString(4, user.getRole().name());

            ps.setString(5, "ACTIVE");

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}