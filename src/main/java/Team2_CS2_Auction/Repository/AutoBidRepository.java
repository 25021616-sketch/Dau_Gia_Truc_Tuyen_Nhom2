package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.AutoBid;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AutoBidRepository {

    public AutoBid getByUserAndProduct(int userId, int productId) {
        String sql = "SELECT * FROM auto_bids WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new AutoBid(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getInt("step_multiplier"),
                        rs.getDouble("max_limit"),
                        rs.getBoolean("is_active"),
                        0.0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveOrUpdate(AutoBid ab) {
        String sql = "INSERT INTO auto_bids (user_id, product_id, step_multiplier, max_limit, is_active) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "step_multiplier = VALUES(step_multiplier), " +
                     "max_limit = VALUES(max_limit), " +
                     "is_active = VALUES(is_active)";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ab.getUserId());
            ps.setInt(2, ab.getProductId());
            ps.setInt(3, ab.getStepMultiplier());
            ps.setDouble(4, ab.getMaxLimit());
            ps.setBoolean(5, ab.isActive());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deactivate(int userId, int productId) {
        String sql = "UPDATE auto_bids SET is_active = 0 WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AutoBid> getActiveAutoBidsByProduct(int productId) {
        List<AutoBid> list = new ArrayList<>();
        // Kết hợp với bảng user để lấy thông tin balance
        String sql = "SELECT a.*, u.balance " +
                     "FROM auto_bids a " +
                     "JOIN user u ON a.user_id = u.id " +
                     "WHERE a.product_id = ? AND a.is_active = 1";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AutoBid ab = new AutoBid(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getInt("step_multiplier"),
                        rs.getDouble("max_limit"),
                        rs.getBoolean("is_active"),
                        rs.getDouble("balance") // Gán balance từ bảng user
                );
                list.add(ab);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
