package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.Auction;
import java.util.List;

public interface AuctionRepository {

    Auction findById(String id) throws Exception;

    List<Auction> findPendingAuctions() throws Exception;

    void updateStatus(String id, String status) throws Exception;
}