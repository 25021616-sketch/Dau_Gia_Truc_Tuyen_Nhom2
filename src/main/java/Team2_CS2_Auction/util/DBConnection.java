package Team2_CS2_Auction.util;

import java.sql.Connection;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://turntable.proxy.rlwy.net:27416/auction_db" +
                    "?useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    private static final String PASSWORD = "wRUFbXdBBbdfWquOSqETKpVqzRzEYjEr";

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30s
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("[DB] ❌ Khởi tạo HikariCP thất bại: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
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