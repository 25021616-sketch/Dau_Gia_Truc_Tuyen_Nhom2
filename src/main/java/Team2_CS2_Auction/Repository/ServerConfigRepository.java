package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServerConfigRepository {

    /**
     * Tự động tạo bảng server_config và dòng mặc định nếu chưa tồn tại.
     * Được gọi trước mọi thao tác để đảm bảo bảng luôn sẵn sàng,
     * không phụ thuộc vào Flyway migration.
     */
    private static void ensureTableExists(Connection conn) throws Exception {
        String createTable =
            "CREATE TABLE IF NOT EXISTS server_config (" +
            "  id INT PRIMARY KEY," +
            "  ip_address VARCHAR(255) NOT NULL," +
            "  port INT NOT NULL," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")";
        try (PreparedStatement ps = conn.prepareStatement(createTable)) {
            ps.executeUpdate();
        }

        // Đảm bảo luôn có 1 dòng mặc định với id = 1
        String insertDefault =
            "INSERT IGNORE INTO server_config (id, ip_address, port) VALUES (1, '127.0.0.1', 8080)";
        try (PreparedStatement ps = conn.prepareStatement(insertDefault)) {
            ps.executeUpdate();
        }
    }

    /**
     * Lưu cấu hình IP và Port của Server lên Database.
     */
    public static void saveServerConfig(String ipAddress, int port) {
        try (Connection conn = DBConnection.getConnection()) {
            ensureTableExists(conn);

            String query = "UPDATE server_config SET ip_address = ?, port = ? WHERE id = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, ipAddress);
                pstmt.setInt(2, port);
                pstmt.executeUpdate();
            }
            System.out.println("[DB] ✅ Đã cập nhật IP Server lên Database: " + ipAddress + ":" + port);
        } catch (Exception e) {
            System.err.println("[DB] ❌ Lỗi cập nhật cấu hình Server: " + e.getMessage());
        }
    }

    /**
     * Lấy IP của Server từ Database.
     * Trả về null nếu chưa có cấu hình hoặc lỗi kết nối.
     */
    public static String getServerIp() {
        try (Connection conn = DBConnection.getConnection()) {
            ensureTableExists(conn);

            String query = "SELECT ip_address FROM server_config WHERE id = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String ip = rs.getString("ip_address");
                    // Bỏ qua giá trị mặc định "127.0.0.1" — chỉ kết nối nếu Server đã ghi IP thật
                    if (ip != null && !ip.equals("127.0.0.1")) {
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] ❌ Lỗi lấy cấu hình Server IP: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy Port của Server từ Database.
     */
    public static int getServerPort() {
        try (Connection conn = DBConnection.getConnection()) {
            ensureTableExists(conn);

            String query = "SELECT port FROM server_config WHERE id = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("port");
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] ❌ Lỗi lấy cấu hình Server Port: " + e.getMessage());
        }
        return 8080; // Mặc định
    }
}
