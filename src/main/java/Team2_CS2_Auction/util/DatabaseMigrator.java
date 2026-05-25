package Team2_CS2_Auction.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility class để tự động tạo các bảng cần thiết nếu chưa tồn tại.
 * Không cần Flyway runtime, chỉ cần JDBC thông thường.
 */
public class DatabaseMigrator {

    /**
     * Chạy toàn bộ quá trình tạo bảng.
     * Gọi hàm này từ ServerMain trước khi khởi động server.
     */
    public static void migrate() {
        System.out.println("[DB] Bắt đầu kiểm tra và tạo bảng...");
        createAutoBidsTable();
        System.out.println("[DB] Hoàn tất kiểm tra schema.");
    }

    private static void createAutoBidsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS auto_bids (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    user_id INT NOT NULL," +
                "    product_id INT NOT NULL," +
                "    step_multiplier INT NOT NULL DEFAULT 1," +
                "    max_limit DOUBLE NOT NULL," +
                "    is_active TINYINT(1) NOT NULL DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE," +
                "    UNIQUE KEY unique_user_product (user_id, product_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] ✅ Bảng 'auto_bids' đã sẵn sàng.");
        } catch (Exception e) {
            System.err.println("[DB] ❌ Không thể tạo bảng 'auto_bids': " + e.getMessage());
        }
    }
}
