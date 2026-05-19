package Team2_CS2_Auction.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://turntable.proxy.rlwy.net:27416/auction_db" +
                    "?useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    private static final String PASSWORD = "wRUFbXdBBbdfWquOSqETKpVqzRzEYjEr";

    public static Connection getConnection() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            // ✅ FIX: throw exception thay vì return null
            // Trả về null khiến caller bị NullPointerException âm thầm,
            // gây rollback mà không có log lỗi rõ ràng.
            System.err.println("[DB] ❌ Kết nối database thất bại: " + e.getMessage());
            throw new Exception("Không thể kết nối Database: " + e.getMessage(), e);
        }
    }
}