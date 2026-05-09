package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.user.Admin;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.util.DBConnection;
import Team2_CS2_Auction.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRepository {

    public User login(String username, String password) {
        String sql = "SELECT * FROM user WHERE LOWER(username)=LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username != null ? username.trim() : null);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword != null) {
                    storedPassword = storedPassword.trim();
                }

                String inputPassword = password != null ? password.trim() : "";
                String hashedInput = PasswordUtils.hashSha256(inputPassword);

                boolean isPasswordMatch = storedPassword != null
                        && (storedPassword.equals(inputPassword) || storedPassword.equals(hashedInput));

                if (!isPasswordMatch) {
                    return null;
                }

                int id = rs.getInt("id");
                String usernameInDb = rs.getString("username");
                String phone = rs.getString("phone");
                String roleRaw = rs.getString("role");

                User user;
                if (roleRaw != null && roleRaw.equalsIgnoreCase("ADMIN")) {
                    user = new Admin(id, usernameInDb, password, phone);
                } else {
                    user = new Member(id, usernameInDb, password, phone);
                }

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean register(User user) {
        String sql = "INSERT INTO user(username,password,phone,role) VALUES(?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getPhone());
            ps.setString(4, "MEMBER");

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean existsByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}