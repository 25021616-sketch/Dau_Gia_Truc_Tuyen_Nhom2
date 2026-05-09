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

    // Hàm insertProduct bạn giữ nguyên, nhớ để ý kiểu dữ liệu LocalDateTime khi lưu vào DB
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

    public List<Auction> getAllProducts() {
        List<Auction> auctionList = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 1. Lấy dữ liệu cơ bản
                int dbId = rs.getInt("id");
                String idStr = String.valueOf(dbId);
                String name = rs.getString("name");
                String description = rs.getString("description");
                String category = rs.getString("category");
                double startPrice = rs.getDouble("start_price");
                double stepPrice = rs.getDouble("step_price");

                // Tránh lỗi NullPointerException nếu cột thời gian trong DB bị trống
                LocalDateTime startTime = rs.getTimestamp("start_time") != null ?
                        rs.getTimestamp("start_time").toLocalDateTime() : LocalDateTime.now();
                LocalDateTime endTime = rs.getTimestamp("end_time") != null ?
                        rs.getTimestamp("end_time").toLocalDateTime() : LocalDateTime.now().plusDays(1);

                String imagePath = rs.getString("image_path");

                // 2. Xử lý Member (Seller) - Khớp với UserRepository
                int sellerId = rs.getInt("seller_id");
                // Vì Member cần 4 tham số (int, String, String, String)
                Member seller = new Member(sellerId, "Unknown", "Unknown", "Unknown");

                // 3. Xử lý danh sách ảnh cho Item
                List<String> images = new ArrayList<>();
                if (imagePath != null && !imagePath.isEmpty()) {
                    images.add(imagePath);
                }

                // 4. Tạo Item bằng Factory (Chỉ 5 tham số: id, name, category, description, images)
                // Đảm bảo các lớp Art, Electronics... của bạn chỉ nhận 5 tham số này
                Item item = ItemFactory.createItem(category, idStr, name, description, images);

                // 5. Tạo Auction (Cấu trúc mới nhất)
                Auction auction = new Auction(
                        "AUC_" + idStr,
                        item,
                        seller,
                        startPrice,
                        stepPrice,
                        startTime,
                        endTime
                );

                auctionList.add(auction);
            }
        } catch (Exception e) {
            System.err.println("Lỗi getAllProducts: " + e.getMessage());
            e.printStackTrace();
        }
        return auctionList;
    }
}