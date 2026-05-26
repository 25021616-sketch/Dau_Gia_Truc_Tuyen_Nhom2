package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.util.DBConnection;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import Team2_CS2_Auction.Model.item.*;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.user.Member;

public class ProductRepository {

    // --- Hàm insertProduct: Giữ nguyên logic thêm mới ---
    public boolean insertProduct(String name, String description, String category, double startPrice,
                                 double currentPrice, double stepPrice, int sellerId,
                                 LocalDateTime startTime, LocalDateTime endTime, String status, String imagePath) {
        String sql = "INSERT INTO products (name, description, category, start_price, current_price, " +
                "step_price, seller_id, start_time, end_time, status, image_path) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setDouble(4, startPrice);
            ps.setDouble(5, currentPrice);
            ps.setDouble(6, stepPrice);
            ps.setInt(7, sellerId);
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(startTime));
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(endTime));
            ps.setString(10, status);
            ps.setString(11, imagePath);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy sản phẩm hiển thị trên MÀN HÌNH CHÍNH.
     * Hiển thị:
     *   - Các phiên đang/sắp diễn ra (status = 'OPENING')
     *   - Các phiên đã kết thúc (status = 'FINISHED') nhưng chưa quá 18:00 ngày kết thúc
     * Sau 18:00 ngày kết thúc, phiên tự ẩn khỏi màn hình chính.
     */
    public List<Auction> getAllActiveProducts() {
        String sql = "SELECT * FROM products " +
                     "WHERE (status = 'OPENING') " +
                     "   OR (status IN ('FINISHED', 'NO_BID') AND NOW() < DATE(end_time) + INTERVAL 18 HOUR) " +
                     "ORDER BY " +
                     "  CASE " +
                     "    WHEN start_time <= NOW() AND end_time > NOW() THEN 1 " + // Đang diễn ra
                     "    WHEN start_time > NOW() THEN 2 " +                       // Sắp diễn ra
                     "    ELSE 3 " +                                               // Đã kết thúc
                     "  END ASC, end_time ASC";
        return getListFromQuery(sql, null);
    }

    /**
     * Lấy sản phẩm của RIÊNG TÔI
     * Tự động ẩn các phiên đã kết thúc quá 12h và ưu tiên phiên Đang diễn ra lên đầu.
     */
    public List<Auction> getProductsBySellerId(int sellerId) {
        String sql = "SELECT * FROM products WHERE seller_id = ? AND status != 'CANCELLED' " +
                     "ORDER BY " +
                     "  CASE " +
                     "    WHEN start_time <= NOW() AND end_time > NOW() THEN 1 " +
                     "    WHEN start_time > NOW() THEN 2 " +
                     "    ELSE 3 " +
                     "  END ASC, end_time ASC";
        return getListFromQuery(sql, sellerId);
    }

    /**
     * Cập nhật giá hiện tại khi có người đấu giá
     */
    public boolean updateCurrentPrice(int id, double newPrice) {
        String sql = "UPDATE products SET current_price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm bổ trợ để thực thi Query và Map dữ liệu
     */
    private List<Auction> getListFromQuery(String sql, Integer paramId) {
        List<Auction> auctionList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (paramId != null) {
                ps.setInt(1, paramId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    auctionList.add(mapResultSetToAuction(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auctionList;
    }

    /**
     * Hàm tách biệt logic Map dữ liệu từ ResultSet sang Object Auction
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
        Member seller = new Member(sellerId, "Unknown", "Unknown", "Unknown");

        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        Auction auction = new Auction(
                "AUC_" + idStr,
                item,
                seller,
                rs.getDouble("start_price"),
                rs.getDouble("step_price"),
                startTime,
                endTime
        );

        auction.setCurrentPrice(rs.getDouble("current_price"));
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
                case "FINISHED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.FINISHED);
                    break;
                case "NO_BID":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.NO_BID);
                    break;
                case "EXPIRED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.EXPIRED);
                    break;
                case "CANCELLED":
                    auction.setStatus(Team2_CS2_Auction.Model.auction.AuctionStatus.CANCELLED);
                    break;
            }
        }

        return auction;
    }
    /**
     * Admin duyệt sản phẩm
     * status: PENDING -> OPENING
     */
    public boolean approveProduct(int productId) {

        String sql =
                "UPDATE products " +
                        "SET status = 'OPENING' " +
                        "WHERE id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, productId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    /**
     * Admin từ chối sản phẩm
     * status: PENDING -> REJECTED
     */
    public boolean rejectProduct(int productId) {

        String sql =
                "UPDATE products " +
                        "SET status = 'REJECTED' " +
                        "WHERE id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, productId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}