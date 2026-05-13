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
        String idStr = String.valueOf(idInt);

        // 1. Map thông tin Item
        Item item = ItemFactory.createItem(
                idStr,
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("image_path")
        );

        // 2. Lấy seller_id quan trọng nhất để so sánh "Sản phẩm của tôi"
        int sellerId = rs.getInt("seller_id");

        // Tạo đối tượng Member với ID thật từ DB
        // Các thông tin khác có thể để mặc định, nhưng ID phải chuẩn
        Member seller = new Member(sellerId, "User_" + sellerId, "password", "000000");

        // 3. Map thời gian
        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        // 4. Tạo đối tượng Auction
        Auction auction = new Auction(
                "AUC_" + idInt,
                item,
                seller, // Đã có ID thật bên trong
                rs.getDouble("start_price"),
                rs.getDouble("step_price"),
                startTime,
                endTime
        );

        auction.setCurrentPrice(rs.getDouble("current_price"));

        return auction;
    }

    /**
     * Cập nhật giá thầu mới (Có check giá cao hơn để chống tranh chấp)
     */
    @Override
    public boolean updateBidPrice(String auctionId, double price, int bidderId) throws Exception {
        String numericId = auctionId.replace("AUC_", "");
        // SQL: Chỉ cập nhật nếu giá mới (price) > giá hiện tại (current_price)
        String sql = "UPDATE products SET current_price = ?, last_bidder_id = ? WHERE id = ? AND ? > current_price";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, bidderId);
            ps.setInt(3, Integer.parseInt(numericId));
            ps.setDouble(4, price);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Auction> getAuctionsUserHasParticipatedIn(int userId) {
        List<Auction> auctions = new ArrayList<>();
        // SQL lấy DISTINCT để một phiên chỉ hiện 1 lần dù bạn đặt giá nhiều lần
        String sql = "SELECT DISTINCT p.* FROM products p " +
                "INNER JOIN bids b ON p.id = b.product_id " +
                "WHERE b.user_id = ?";

        // Thực thi query tương tự như các hàm getAll khác của bạn...
        // Nhớ nạp dữ liệu vào đối tượng Auction (id, tên, giá hiện tại, image...)
        return auctions;
    }
    @Override
    public List<Auction> findAuctionsByBidderId(int userId) throws Exception {
        List<Auction> results = new ArrayList<>();
        // Kiểm tra xem bảng của bạn là 'bid' hay 'bids' trong DB rồi sửa ở đây nhé
        String sql = "SELECT DISTINCT p.* FROM products p " +
                "INNER JOIN bids b ON p.id = b.product_id " +
                "WHERE b.user_id = ?";

        System.out.println("DEBUG REPO: Đang chạy SQL tìm phiên cho User ID: " + userId);

        try (Connection conn = Team2_CS2_Auction.util.DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("DEBUG REPO: Kết nối DB thất bại (NULL)!");
                return results;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Auction auction = mapResultSetToAuction(rs);
                    results.add(auction);
                }
                System.out.println("DEBUG REPO: SQL hoàn tất. Tìm thấy: " + results.size() + " phiên.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG REPO: Lỗi truy vấn: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}