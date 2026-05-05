package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepositoryImpl implements AuctionRepository {

    @Override
    public Auction findById(String id) throws Exception {

        String sql = "SELECT * FROM auctions WHERE id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return null; // TODO: map object Auction sau
        }

        return null;
    }

    @Override
    public List<Auction> findPendingAuctions() {
        return new ArrayList<>();
    }

    @Override
    public void updateStatus(String id, String status) throws Exception {

        String sql = "UPDATE auctions SET status = ? WHERE id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, status);
        ps.setString(2, id);

        ps.executeUpdate();
    }
}