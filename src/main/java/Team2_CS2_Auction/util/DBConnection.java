package Team2_CS2_Auction.util;

import java.sql.Connection;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;

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

    /**
     * Chạy Flyway migration tự động để đồng bộ cấu trúc database.
     * Dùng ClassLoader tường minh để tìm đúng file SQL dù chạy qua exec:java hay JAR.
     */
    public static void runFlywayMigration() {
        try {
            System.out.println("[Flyway] Bắt đầu kiểm tra và cập nhật cấu trúc database...");
            // Dùng ClassLoader của class hiện tại để Flyway tìm đúng SQL trong classpath
            ClassLoader cl = DBConnection.class.getClassLoader();
            Flyway flyway = Flyway.configure(cl)
                    .dataSource(URL, USER, PASSWORD)
                    // Cung cấp 2 location: classpath cho khi build ra file JAR, và filesystem cho khi chạy dev bằng exec:java
                    .locations("classpath:db/migration", "filesystem:src/main/resources/db/migration")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)              // tắt validate với MySQL 9.4 mới hơn Flyway 8.2
                    .load();
            flyway.migrate();
            System.out.println("[Flyway] Cập nhật database thành công!");
        } catch (Exception e) {
            System.err.println("[Flyway] Lỗi khi chạy migration: " + e.getMessage());
            // Không crash server — DB có thể đã được init trước đó
            System.out.println("[Flyway] Server vẫn tiếp tục khởi động (DB có thể đã được init từ trước).");
        }
    }
}