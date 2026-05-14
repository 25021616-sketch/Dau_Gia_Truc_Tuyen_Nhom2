package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepositoryImpl implements AuctionRepository {

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
        if ("OPENING".equals(dbStatus) || "OPEN".equals(dbStatus)) {
            auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.OPEN);
        }

        return auction;
    }

    /**
     * Cập nhật giá thầu mới (Có check giá cao hơn để chống tranh chấp)
     */
    @Override
    public boolean updateBidPrice(String auctionId, double price, int bidderId) throws Exception {
        String numericId = auctionId.replace("AUC_", "");
        // 1. SQL cập nhật giá hiện tại của sản phẩm
        String sqlUpdateProduct = "UPDATE products SET current_price = ?, last_bidder_id = ? WHERE id = ? AND ? > current_price";
        // 2. SQL ghi lại lịch sử đặt giá (Để hàm lấy lịch sử có dữ liệu mà JOIN)
        String sqlInsertBid = "INSERT INTO bid (user_id, product_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật transaction để đảm bảo lưu cả 2 hoặc không gì cả

            // Thực hiện Update Product
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateProduct)) {
                psUpdate.setDouble(1, price);
                psUpdate.setInt(2, bidderId);
                psUpdate.setInt(3, Integer.parseInt(numericId));
                psUpdate.setDouble(4, price);

                if (psUpdate.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Thực hiện Insert vào bảng bid
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertBid)) {
                psInsert.setInt(1, bidderId);
                psInsert.setInt(2, Integer.parseInt(numericId));
                psInsert.setDouble(3, price);
                psInsert.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) conn.close();
        }
    }
    @Override
    public List<Auction> findAuctionsByBidderId(int userId) throws Exception {
        List<Auction> results = new ArrayList<>();
        // SQL: Sử dụng bảng 'bid' và cột 'user_id' như trong ảnh cấu trúc DB của bạn
        String sql = "SELECT DISTINCT p.* FROM products p " +
                "INNER JOIN bid b ON p.id = b.product_id " +
                "WHERE b.user_id = ?";

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
}