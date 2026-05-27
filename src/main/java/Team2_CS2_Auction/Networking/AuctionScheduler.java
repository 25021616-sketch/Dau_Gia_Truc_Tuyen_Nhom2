package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler chạy nền trên Server, tự động cập nhật trạng thái phiên đấu giá theo thời gian thực.
 * Chạy mỗi 5 giây một lần.
 */
public class AuctionScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void start() {
        // Bắt Throwable thay vì Exception vì scheduleAtFixedRate sẽ dừng hẳn
        // nếu Runnable ném bất kỳ unchecked exception nào mà không được bắt.
        scheduler.scheduleAtFixedRate(() -> {
            try {
                expirePendingAuctions();
                checkEndedAuctions();
            } catch (Throwable t) {
                System.err.println("[SCHEDULER] Lỗi khi check phiên đấu giá: " + t.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);

        System.out.println("[SCHEDULER] Đã khởi động, kiểm tra mỗi 5 giây.");
    }

    /**
     * Chuyển tất cả phiên PENDING đã hết hạn sang trạng thái EXPIRED (không được duyệt kịp).
     */
    private void expirePendingAuctions() {
        String sql = "UPDATE products SET status = 'EXPIRED' WHERE status = 'PENDING' AND end_time < NOW()";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            int expiredCount = ps.executeUpdate();
            if (expiredCount > 0) {
                System.out.println("[SCHEDULER] Đã EXPIRED " + expiredCount + " phiên chưa duyệt hết hạn.");
            }
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Lỗi expire PENDING: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra và chốt kết quả các phiên OPENING đã hết thời gian đấu giá.
     */
    private void checkEndedAuctions() throws Exception {
        String sql = "SELECT id, end_time, status FROM products WHERE status = 'OPENING'";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            AuctionRepositoryImpl repo = new AuctionRepositoryImpl();

            while (rs.next()) {
                int productId = rs.getInt("id");
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

                if (endTime.isBefore(LocalDateTime.now())) {
                    repo.finishAuction(productId);
                    System.out.println("[SCHEDULER] Đã chốt phiên đấu giá: " + productId);
                }
            }
        }
    }
}