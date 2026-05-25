package Team2_CS2_Auction.util;

/**
 * Chạy file này MỘT LẦN để tạo bảng auto_bids trong database.
 * Chọn file này trong IntelliJ > Run 'MigrationRunner.main()'
 */
public class MigrationRunner {
    public static void main(String[] args) {
        System.out.println("=== Đang tạo bảng auto_bids trong database ===");
        DatabaseMigrator.migrate();
        System.out.println("=== Hoàn tất! Bạn có thể tắt chương trình này. ===");
        System.exit(0);
    }
}
