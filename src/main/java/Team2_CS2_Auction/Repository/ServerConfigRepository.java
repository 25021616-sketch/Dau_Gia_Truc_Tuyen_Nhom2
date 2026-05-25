package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServerConfigRepository {

    /**
     * Lưu cấu hình IP của Server lên Database.
     * Cập nhật dòng có id = 1.
     */
    public static void saveServerConfig(String ipAddress, int port) {
        String query = "UPDATE server_config SET ip_address = ?, port = ? WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, ipAddress);
            pstmt.setInt(2, port);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                // Đề phòng chưa có dòng id = 1 (do Migration lỗi chẳng hạn)
                String insertQuery = "INSERT IGNORE INTO server_config (id, ip_address, port) VALUES (1, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, ipAddress);
                    insertStmt.setInt(2, port);
                    insertStmt.executeUpdate();
                }
            }
            System.out.println("[DB] Đã cập nhật IP Server lên Database: " + ipAddress + ":" + port);
        } catch (Exception e) {
            System.err.println("[DB] Lỗi cập nhật cấu hình Server: " + e.getMessage());
        }
    }

    /**
     * Lấy IP của Server từ Database.
     */
    public static String getServerIp() {
        String query = "SELECT ip_address FROM server_config WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
             
            if (rs.next()) {
                return rs.getString("ip_address");
            }
        } catch (Exception e) {
            System.err.println("[DB] Lỗi lấy cấu hình Server IP: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy Port của Server từ Database.
     */
    public static int getServerPort() {
        String query = "SELECT port FROM server_config WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
             
            if (rs.next()) {
                return rs.getInt("port");
            }
        } catch (Exception e) {
            System.err.println("[DB] Lỗi lấy cấu hình Server Port: " + e.getMessage());
        }
        return 8080; // Mặc định
    }
}
