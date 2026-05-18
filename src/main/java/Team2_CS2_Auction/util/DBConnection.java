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

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (Exception e) {
            System.out.println("Kết nối database thất bại!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Chạy Flyway migration tự động để đồng bộ cấu trúc database
     */
    public static void runFlywayMigration() {
        try {
            System.out.println("[Flyway] Bắt đầu kiểm tra và cập nhật cấu trúc database...");
            Flyway flyway = Flyway.configure()
                    .dataSource(URL, USER, PASSWORD)
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
            System.out.println("[Flyway] Cập nhật database thành công!");
        } catch (Exception e) {
            System.err.println("[Flyway] Lỗi khi chạy migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}