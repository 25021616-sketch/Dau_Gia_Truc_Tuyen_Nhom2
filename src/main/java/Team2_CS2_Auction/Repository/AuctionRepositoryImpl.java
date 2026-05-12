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
        // ID truyền vào có thể là "AUC_12", ta cần lấy số 12
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
     * Lấy danh sách các sản phẩm đang chờ duyệt (status = 'PENDING')
     * Dùng cho màn hình Admin
     */
    @Override
    public List<Auction> findPendingAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM products p " +
                "LEFT JOIN user u ON p.seller_id = u.id " + // Giả sử bảng của bạn tên là 'user'
                "WHERE p.status = 'PENDING' ORDER BY p.start_time DESC";

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
     * Cập nhật trạng thái (Duyệt bài: PENDING -> OPENING, hoặc Kết thúc: OPENING -> FINISHED)
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
     * HÀM MAPPING (Dùng chung để chuyển từ SQL sang Object)
     * Đảm bảo đồng nhất với ProductRepository
     */
    private Auction mapResultSetToAuction(ResultSet rs) throws Exception {
        int idInt = rs.getInt("id");
        String idStr = String.valueOf(idInt);


        Item item = ItemFactory.createItem(
                idStr,
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("image_path")
        );

        int sellerId = rs.getInt("seller_id");
        String sellerName = rs.getString("username");
        if (sellerName == null) sellerName = "N/A";
        Member seller = new Member(sellerId, sellerName, "Unknown", "Unknown");

        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        Auction auction = new Auction(
                "AUC_" + rs.getInt("id"),
                item,
                seller,
                rs.getDouble("start_price"),
                rs.getDouble("step_price"),  // Truyền vào constructor
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime()   // Truyền vào constructor
        );

        auction.setCurrentPrice(rs.getDouble("current_price"));
        // Bạn có thể gán thêm status vào Auction object nếu Model có thuộc tính này

        return auction;
    }
}