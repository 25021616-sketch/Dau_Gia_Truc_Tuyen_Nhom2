package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            // ✅ FIX: Bắt Throwable thay vì Exception
            // scheduleAtFixedRate sẽ DỪNG HẲN nếu Runnable ném bất kỳ exception nào
            // (kể cả RuntimeException) mà không được bắt => scheduler chết âm thầm
            try {
                checkEndedAuctions();
            } catch (Throwable t) {
                System.err.println("[SCHEDULER] ❌ Lỗi khi check phiên đấu giá: " + t.getMessage());
                t.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);

        System.out.println("[SCHEDULER] ✅ Đã khởi động, kiểm tra mỗi 5 giây.");
    }

    private void checkEndedAuctions() throws Exception {

        String sql =
                "SELECT id, end_time, status " +
                        "FROM products " +
                        "WHERE status = 'OPENING'";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            AuctionRepositoryImpl repo =
                    new AuctionRepositoryImpl();

            while (rs.next()) {

                int productId = rs.getInt("id");

                java.sql.Timestamp endTime =
                        rs.getTimestamp("end_time");

                String status =
                        rs.getString("status");

                // CHECK THỜI GIAN THẬT
                if (
                        endTime.toLocalDateTime()
                                .isBefore(java.time.LocalDateTime.now())

                                &&

                                status.equals("OPENING")
                ) {

                    repo.finishAuction(productId);

                    System.out.println(
                            "AUTO FINISH PRODUCT: "
                                    + productId
                    );
                }
            }
        }
    }
}