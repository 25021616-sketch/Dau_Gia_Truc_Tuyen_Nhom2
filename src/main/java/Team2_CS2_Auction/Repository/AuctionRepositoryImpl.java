package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.auction.BidHistory;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepositoryImpl implements AuctionRepository {

    private final UserRepository userRepo = new UserRepository();
    /**
     * Tìm một phiên đấu giá cụ thể theo ID
     */
    @Override
    public Auction findById(String id) throws Exception {
        String numericId = id.replace("AUC_", "");
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(numericId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách các sản phẩm đang chờ duyệt
     */
    @Override
    public List<Auction> findPendingAuctions() {
        List<Auction> list = new ArrayList<>();
        // Chỉ lấy từ bảng products để tránh lỗi Column not found khi JOIN bảng user chưa chuẩn
        String sql = "SELECT * FROM products WHERE status = 'PENDING' ORDER BY start_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAuction(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái phiên đấu giá
     */
    @Override
    public void updateStatus(String id, String status) throws Exception {
        String numericId = id.replace("AUC_", "");
        String sql = "UPDATE products SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(numericId));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("Không tìm thấy phiên đấu giá với ID: " + id);
            }
        }
    }

    /**
     * HÀM MAPPING - ĐÃ FIX LỖI COLUMN 'USERNAME' NOT FOUND
     */
    private Auction mapResultSetToAuction(ResultSet rs) throws Exception {
        int idInt = rs.getInt("id");
        Item item = ItemFactory.createItem(
                String.valueOf(idInt),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("image_path")
        );

        // Thông tin Seller (Đã có pass mặc định)
        Member seller = new Member(rs.getInt("seller_id"), "User_" + rs.getInt("seller_id"), "123456", "000000");

        Auction auction = new Auction(
                "AUC_" + idInt,
                item,
                seller,
                rs.getDouble("start_price"),
                rs.getDouble("step_price"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime()
        );

        auction.setCurrentPrice(rs.getDouble("current_price"));

        // --- PHẦN FIX LỖI PASSWORD TẠI ĐÂY ---
        int lastBidderId = rs.getInt("last_bidder_id");
        if (lastBidderId > 0) {
            // Cần truyền "123456" (hoặc pass bất kỳ) thay vì để trống ""
            Member winner = new Member(lastBidderId, "Bidder_" + lastBidderId, "123456", "000000");
            auction.setWinner(winner);
        }
        // -------------------------------------

        String dbStatus = rs.getString("status");
        if (dbStatus != null) {
            switch (dbStatus) {
                case "PENDING":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.PENDING);
                    break;
                case "OPENING":
                case "OPEN":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.OPEN);
                    break;
                case "REJECTED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.REJECTED);
                    break;
                case "CLOSED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.CLOSED);
                    break;
                case "CANCELLED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.CANCELLED);
                    break;
            }
        }

        return auction;
    }

    /**
     * Cập nhật giá thầu mới (Có check giá cao hơn để chống tranh chấp)
     */
    @Override
    public boolean updateBidPrice(
            String auctionId,
            double price,
            int bidderId
    ) throws Exception {

        String numericId =
                auctionId.replace("AUC_", "");

        String sqlUpdateProduct =
                "UPDATE products " +
                        "SET current_price = ?, last_bidder_id = ? " +
                        "WHERE id = ? " +
                        "AND ? > current_price " +
                        "AND status = 'OPENING' " +
                        "AND end_time > NOW()";

        String sqlInsertBid =
                "INSERT INTO bid(user_id, product_id, bid_amount, bid_time) " +
                        "VALUES (?, ?, ?, NOW())";

        Connection conn = null;

        try {

            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);

            // UPDATE PRODUCT
            try (
                    PreparedStatement psUpdate =
                            conn.prepareStatement(sqlUpdateProduct)
            ) {

                psUpdate.setDouble(1, price);
                psUpdate.setInt(2, bidderId);
                psUpdate.setInt(3, Integer.parseInt(numericId));
                psUpdate.setDouble(4, price);

                if (psUpdate.executeUpdate() == 0) {

                    conn.rollback();

                    return false;
                }
            }

            // INSERT BID
            try (
                    PreparedStatement psInsert =
                            conn.prepareStatement(sqlInsertBid)
            ) {

                psInsert.setInt(1, bidderId);
                psInsert.setInt(2, Integer.parseInt(numericId));
                psInsert.setDouble(3, price);

                psInsert.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (Exception e) {

            if (conn != null) {

                conn.rollback();
            }

            e.printStackTrace();

            return false;

        } finally {

            if (conn != null) {

                conn.close();
            }
        }
    }

    @Override
    public java.time.LocalDateTime executeBidTransaction(int bidderId, String auctionId, double bidAmount) throws Exception {
        String numericId = auctionId.replace("AUC_", "");
        int productId = Integer.parseInt(numericId);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Get auction info with FOR UPDATE
            String sqlProduct = "SELECT seller_id, current_price, step_price, last_bidder_id, status, end_time FROM products WHERE id = ? FOR UPDATE";
            double currentPrice = 0;
            double stepPrice = 0;
            int sellerId = 0;
            int lastBidderId = 0;
            String status = "";
            java.time.LocalDateTime endTime = null;

            try (PreparedStatement psProduct = conn.prepareStatement(sqlProduct)) {
                psProduct.setInt(1, productId);
                try (ResultSet rs = psProduct.executeQuery()) {
                    if (rs.next()) {
                        sellerId = rs.getInt("seller_id");
                        currentPrice = rs.getDouble("current_price");
                        stepPrice = rs.getDouble("step_price");
                        lastBidderId = rs.getInt("last_bidder_id");
                        status = rs.getString("status");
                        java.sql.Timestamp ts = rs.getTimestamp("end_time");
                        if (ts != null) {
                            endTime = ts.toLocalDateTime();
                        }
                    } else {
                        throw new Exception("Không tìm thấy phiên đấu giá!");
                    }
                }
            }

            if (!"OPENING".equals(status) && !"OPEN".equals(status)) {
                throw new Exception("Phiên đấu giá chưa mở hoặc đã kết thúc!");
            }

            if (sellerId == bidderId) {
                throw new Exception("Bạn không thể đấu giá sản phẩm của chính mình!");
            }

            double minimumBid = currentPrice + stepPrice;
            if (bidAmount < minimumBid) {
                throw new Exception("Giá tối thiểu phải là: " + minimumBid);
            }

            // 2. Get bidder balance with FOR UPDATE
            String sqlBidder = "SELECT balance, locked_balance FROM user WHERE id = ? FOR UPDATE";
            double bidderBalance = 0;
            double bidderLocked = 0;

            try (PreparedStatement psBidder = conn.prepareStatement(sqlBidder)) {
                psBidder.setInt(1, bidderId);
                try (ResultSet rs = psBidder.executeQuery()) {
                    if (rs.next()) {
                        bidderBalance = rs.getDouble("balance");
                        bidderLocked = rs.getDouble("locked_balance");
                    } else {
                        throw new Exception("Không tìm thấy thông tin tài khoản người đấu giá!");
                    }
                }
            }

            double availableMoney = bidderBalance - bidderLocked;
            double requiredExtraMoney;

            if (lastBidderId == bidderId) {
                requiredExtraMoney = bidAmount - currentPrice;
            } else {
                requiredExtraMoney = bidAmount;
            }

            if (availableMoney < requiredExtraMoney) {
                throw new Exception("Số dư khả dụng không đủ!");
            }

            // 3. Update Bidder's Locked Balance
            double newBidderLocked = bidderLocked + requiredExtraMoney;
            String updateBidderLockedSql = "UPDATE user SET locked_balance = ? WHERE id = ?";
            try (PreparedStatement psUpdateBidder = conn.prepareStatement(updateBidderLockedSql)) {
                psUpdateBidder.setDouble(1, newBidderLocked);
                psUpdateBidder.setInt(2, bidderId);
                psUpdateBidder.executeUpdate();
            }

            // 4. Unlock Old Highest Bidder if different
            if (lastBidderId > 0 && lastBidderId != bidderId) {
                String sqlOldBidder = "SELECT locked_balance FROM user WHERE id = ? FOR UPDATE";
                double oldBidderLocked = 0;
                try (PreparedStatement psOldBidder = conn.prepareStatement(sqlOldBidder)) {
                    psOldBidder.setInt(1, lastBidderId);
                    try (ResultSet rsOld = psOldBidder.executeQuery()) {
                        if (rsOld.next()) {
                            oldBidderLocked = rsOld.getDouble("locked_balance");
                        }
                    }
                }

                double newOldBidderLocked = oldBidderLocked - currentPrice;
                if (newOldBidderLocked < 0) newOldBidderLocked = 0;

                String updateOldBidderSql = "UPDATE user SET locked_balance = ? WHERE id = ?";
                try (PreparedStatement psUpdateOld = conn.prepareStatement(updateOldBidderSql)) {
                    psUpdateOld.setDouble(1, newOldBidderLocked);
                    psUpdateOld.setInt(2, lastBidderId);
                    psUpdateOld.executeUpdate();
                }
            }

            // 5. Check Anti-Sniping & Update Product
            java.time.LocalDateTime newEndTime = null;
            if (endTime != null) {
                long secondsLeft = java.time.Duration.between(java.time.LocalDateTime.now(), endTime).getSeconds();
                if (secondsLeft >= 0 && secondsLeft <= 45) {
                    newEndTime = endTime.plusSeconds(45);
                }
            }

            if (newEndTime != null) {
                String updateProductSql = "UPDATE products SET current_price = ?, last_bidder_id = ?, end_time = ? WHERE id = ?";
                try (PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductSql)) {
                    psUpdateProduct.setDouble(1, bidAmount);
                    psUpdateProduct.setInt(2, bidderId);
                    psUpdateProduct.setTimestamp(3, java.sql.Timestamp.valueOf(newEndTime));
                    psUpdateProduct.setInt(4, productId);
                    psUpdateProduct.executeUpdate();
                }
            } else {
                String updateProductSql = "UPDATE products SET current_price = ?, last_bidder_id = ? WHERE id = ?";
                try (PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductSql)) {
                    psUpdateProduct.setDouble(1, bidAmount);
                    psUpdateProduct.setInt(2, bidderId);
                    psUpdateProduct.setInt(3, productId);
                    psUpdateProduct.executeUpdate();
                }
            }

            // 6. Insert Bid History
            String insertBidSql = "INSERT INTO bid(user_id, product_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement psInsertBid = conn.prepareStatement(insertBidSql)) {
                psInsertBid.setInt(1, bidderId);
                psInsertBid.setInt(2, productId);
                psInsertBid.setDouble(3, bidAmount);
                psInsertBid.executeUpdate();
            }

            conn.commit();
            return newEndTime;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public List<Auction> findAuctionsByBidderId(int userId) throws Exception {
        List<Auction> results = new ArrayList<>();
        // SQL: Ẩn phiên kết thúc quá 2 ngày và ưu tiên phiên Đang diễn ra
        String sql = "SELECT DISTINCT p.* FROM products p " +
                     "INNER JOIN bid b ON p.id = b.product_id " +
                     "WHERE b.user_id = ? " +
                     "AND p.end_time >= DATE_SUB(NOW(), INTERVAL 2 DAY) " +
                     "ORDER BY " +
                     "  CASE " +
                     "    WHEN p.start_time <= NOW() AND p.end_time > NOW() THEN 1 " +
                     "    WHEN p.start_time > NOW() THEN 2 " +
                     "    ELSE 3 " +
                     "  END ASC, p.end_time ASC";

        System.out.println("DEBUG REPO: Đang chạy SQL tìm phiên cho User ID: " + userId);

        try (Connection conn = Team2_CS2_Auction.util.DBConnection.getConnection()) {
            if (conn == null) return results;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        // Gọi hàm mapping để chuyển ResultSet thành Object Auction
                        results.add(mapResultSetToAuction(rs));
                    }
                }
                System.out.println("DEBUG REPO: SQL hoàn tất. Tìm thấy: " + results.size() + " phiên.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG REPO: Lỗi SQL: " + e.getMessage());
            throw e;
        }
        return results;
    }

    @Override
    public List<Bid> getBidHistory(String auctionId) throws Exception {
        String numericId = auctionId.replace("AUC_", "").replace("AUC", "");
        List<Bid> history = new ArrayList<>();
        String sql = "SELECT * FROM bid WHERE product_id = ? ORDER BY bid_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(numericId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    double amount = rs.getDouble("bid_amount");
                    LocalDateTime time = rs.getTimestamp("bid_time").toLocalDateTime();

                    Member dummyBidder = new Member(userId, "User_" + userId, "123456", "000");
                    Bid bid = new Bid("BID_" + rs.getInt("id"), dummyBidder, amount, time);
                    history.add(bid);
                }
            }
        }
        return history;
    }
    public List<BidHistory> getBidHistory() throws Exception {

        List<BidHistory> list = new ArrayList<>();

        // ✅ FIX: Thêm khoảng trắng phía sau "transaction` t" → trước "’JOIN"
        // Lỗi cũ: cầu SQL bị tạo thành "...`transaction` tJOIN products..." (thiếu space)
        String sql =
                "SELECT " +
                        "t.id, " +
                        "p.name AS product_name, " +
                        "u.username AS winner_name, " +
                        "t.final_price, " +
                        "p.end_time " +
                        "FROM `transaction` t " +
                        "JOIN products p ON t.product_id = p.id " +
                        "JOIN user u ON t.winner_id = u.id " +
                        "ORDER BY p.end_time DESC";

        try (
                Connection conn = DBConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ) {

            int stt = 1;

            while (rs.next()) {

                BidHistory history = new BidHistory(

                        stt++,

                        "TRANS_" + rs.getInt("id"),

                        rs.getString("product_name"),

                        rs.getString("winner_name"),

                        rs.getDouble("final_price"),

                        rs.getTimestamp("end_time").toString()
                );

                list.add(history);
            }
        }

        return list;
    }

    public int getTotalSessionsOrganized() {
        String sql = "SELECT COUNT(*) FROM products WHERE status != 'PENDING' AND status != 'REJECTED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {

        String sql = "SELECT SUM(final_price) FROM `transaction`";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public int getTotalUsers() {

        String sql = "SELECT COUNT(*) FROM user";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void finishAuction(int productId) throws Exception {

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();

            // ✅ FIX: Bật transaction để đảm bảo toàn vẹn dữ liệu
            // Nếu không dùng transaction, UPDATE products có thể thành công
            // nhưng INSERT `transaction` lại thất bại => dữ liệu bị mất
            conn.setAutoCommit(false);

            // 1. Kiểm tra đã FINISHED chưa
            String checkSql = "SELECT status FROM products WHERE id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, productId);
                try (ResultSet checkRs = checkPs.executeQuery()) {
                    if (checkRs.next()) {
                        String status = checkRs.getString("status");
                        if ("FINISHED".equals(status) || "NO_BID".equals(status)) {
                            System.out.println("[SCHEDULER] Product " + productId + " đã kết thúc trước đó với trạng thái " + status);
                            conn.rollback();
                            return;
                        }
                    }
                }
            }

            // 2. Tìm người thắng (giá đặt cao nhất trong bảng bid)
            String findWinnerSql =
                    "SELECT user_id, bid_amount FROM bid " +
                    "WHERE product_id = ? " +
                    "ORDER BY bid_amount DESC " +
                    "LIMIT 1";

            int winnerId;
            double finalPrice;

            try (PreparedStatement psWinner = conn.prepareStatement(findWinnerSql)) {
                psWinner.setInt(1, productId);
                try (ResultSet rs = psWinner.executeQuery()) {
                    // Không có ai bid => chỉ đủi cập nhật status thành NO_BID
                    if (!rs.next()) {
                        String updateSql = "UPDATE products SET status = 'NO_BID' WHERE id = ?";
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setInt(1, productId);
                            psUpdate.executeUpdate();
                        }
                        conn.commit();
                        System.out.println("[SCHEDULER] Product " + productId + ": không có người tham gia => NO_BID.");
                        return;
                    }
                    winnerId = rs.getInt("user_id");
                    finalPrice = rs.getDouble("bid_amount");
                }
            }

            System.out.println("[SCHEDULER] Product " + productId + " | Winner ID=" + winnerId + " | Final Price=" + finalPrice);

            // ===============================
            // THANH TOÁN NGƯỜI THẮNG
            // ===============================

            // Lấy số tiền đang bị lock
            double lockedMoney = userRepo.getLockedBalance(winnerId);

            // Nếu lock nhỏ hơn giá thắng thì cảnh báo thay vì báo lỗi để không treo hệ thống
            if (lockedMoney < finalPrice) {
                System.err.println("[WARNING] Người thắng " + winnerId + " không đủ locked_balance. Vẫn trừ vào số dư thật.");
            }

            // Trừ locked_balance
            double newWinnerLocked = Math.max(0, lockedMoney - finalPrice);
            boolean unlockSuccess = userRepo.updateLockedBalance(winnerId, newWinnerLocked);

            if (!unlockSuccess) {
                System.err.println("[WARNING] Không thể cập nhật locked balance!");
            }

            // Trừ tiền thật khỏi balance
            boolean paySuccess = userRepo.updateBalance(winnerId, userRepo.getBalance(winnerId) - finalPrice);

            if (!paySuccess) {
                System.err.println("[WARNING] Thanh toán thất bại!");
            }

            System.out.println(
                    "[SCHEDULER] Đã trừ tiền người thắng"
            );

            

            // ===============================
// UNLOCK NGƯỜI THUA
// ===============================

            String loserSql =
                    "SELECT DISTINCT user_id, bid_amount " +
                            "FROM bid " +
                            "WHERE product_id = ? " +
                            "AND user_id != ?";

            try (
                    PreparedStatement psLoser =
                            conn.prepareStatement(loserSql)
            ) {

                psLoser.setInt(1, productId);

                psLoser.setInt(2, winnerId);

                ResultSet rsLoser =
                        psLoser.executeQuery();

                while (rsLoser.next()) {

                    int loserId =
                            rsLoser.getInt("user_id");

                    double loserBid =
                            rsLoser.getDouble("bid_amount");

                    double currentLocked =
                            userRepo.getLockedBalance(loserId);

                    double newLocked =
                            currentLocked - loserBid;

                    if (newLocked < 0) {
                        newLocked = 0;
                    }

                    userRepo.updateLockedBalance(
                            loserId,
                            newLocked
                    );

                    System.out.println(
                            "[SCHEDULER] Unlock tiền user " +
                                    loserId
                    );
                }
            }

            // 3. Kiểm tra `transaction` đã tồn tại chưa
            String checkTransactionSql = "SELECT id FROM `transaction` WHERE product_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkTransactionSql)) {
                psCheck.setInt(1, productId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        // Transaction đã tồn tại, chỉ cần đảm bảo status đúng
                        System.out.println("[SCHEDULER] Transaction đã tồn tại cho product " + productId);
                        conn.rollback();
                        return;
                    }
                }
            }

            // 4. INSERT vào bảng `transaction`
            String insertTransactionSql =
                    "INSERT INTO `transaction` (product_id, winner_id, final_price) VALUES (?, ?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertTransactionSql)) {
                psInsert.setInt(1, productId);
                psInsert.setInt(2, winnerId);
                psInsert.setDouble(3, finalPrice);
                psInsert.executeUpdate();
                System.out.println("[SCHEDULER] Đã INSERT transaction cho product " + productId);
            }

            // 5. UPDATE last_bidder_id và status trên bảng products
            // ✅ FIX QUAN TRỌNG: dùng 'last_bidder_id' (đúng tên cột trong DB),
            //    KHÔNG dùng 'winner_id' (cột không tồn tại => SQL exception => rollback!)
            String updateWinnerSql =
                    "UPDATE products SET last_bidder_id = ?, status = 'FINISHED' WHERE id = ?";
            try (PreparedStatement psWinnerUpdate = conn.prepareStatement(updateWinnerSql)) {
                psWinnerUpdate.setInt(1, winnerId);
                psWinnerUpdate.setInt(2, productId);
                psWinnerUpdate.executeUpdate();
                System.out.println("[SCHEDULER] Đã UPDATE products => FINISHED cho product " + productId);
            }

            // Tất cả thành công => commit
            conn.commit();
            System.out.println("[SCHEDULER] ✅ finish auction thành công cho product " + productId);

        } catch (Exception e) {
            System.err.println("[SCHEDULER] ❌ Lỗi finish auction (product " + productId + "): " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (Exception rollbackEx) { rollbackEx.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
                conn.close();
            }
        }
    }

    @Override
    public List<Integer> getDistinctBidderIds(String auctionId) throws Exception {
        String numericId = auctionId.replace("AUC_", "").replace("AUC", "");
        List<Integer> bidderIds = new ArrayList<>();
        String sql = "SELECT DISTINCT user_id FROM bid WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(numericId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bidderIds.add(rs.getInt("user_id"));
                }
            }
        }
        return bidderIds;
    }

    @Override
    public boolean updateAuctionStatus(String auctionId, String status) throws Exception {
        String numericId = auctionId.replace("AUC_", "").replace("AUC", "");
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(numericId));
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Auction getAuctionById(String auctionId) throws Exception {
        // Dùng lại findById đã có
        return findById(auctionId);
    }
}