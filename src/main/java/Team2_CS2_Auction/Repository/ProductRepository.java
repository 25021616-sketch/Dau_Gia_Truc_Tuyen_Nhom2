package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.util.DBConnection;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import Team2_CS2_Auction.Model.item.*;
import java.sql.ResultSet;

public class ProductRepository {

    public boolean insertProduct(
            String name,
            String description,
            String category,
            double startPrice,
            double currentPrice,
            double stepPrice,
            int sellerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            String imagePath
    ) {

        String sql = """
            INSERT INTO products
            (name, description, category, start_price, current_price,
             step_price, seller_id, start_time, end_time, status, image_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setDouble(4, startPrice);
            ps.setDouble(5, currentPrice);
            ps.setDouble(6, stepPrice);
            ps.setInt(7, sellerId);

            // 🔥 FIX QUAN TRỌNG
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
    public List<Item> getAllProducts() {

        List<Item> list = new ArrayList<>();

        String sql = "SELECT * FROM products";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                String id = String.valueOf(rs.getInt("id"));
                String name = rs.getString("name");
                String description = rs.getString("description");
                String category = rs.getString("category");

                double startPrice = rs.getDouble("start_price");
                double stepPrice = rs.getDouble("step_price");

                LocalDateTime startTime =
                        rs.getTimestamp("start_time").toLocalDateTime();

                LocalDateTime endTime =
                        rs.getTimestamp("end_time").toLocalDateTime();

                String imagePath = rs.getString("image_path");

                Item item;

                switch (category) {

                    case "Đồ điện tử" -> item = new Electronics(
                            id, name, category, description,
                            startPrice, stepPrice,
                            startTime, endTime,
                            imagePath,
                            "Unknown", "Unknown"
                    );

                    case "Bất động sản" -> item = new RealEstate(
                            id, name, category, description,
                            startPrice, stepPrice,
                            startTime, endTime,
                            imagePath,
                            "Unknown", 0, "Unknown"
                    );

                    case "Xe hơi" -> item = new Vehicle(
                            id, name, category, description,
                            startPrice, stepPrice,
                            startTime, endTime,
                            imagePath,
                            "Unknown", "Unknown"
                    );

                    default -> item = new Art(
                            id, name, category, description,
                            startPrice, stepPrice,
                            startTime, endTime,
                            imagePath,
                            "Unknown", "Unknown"
                    );
                }

                list.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}