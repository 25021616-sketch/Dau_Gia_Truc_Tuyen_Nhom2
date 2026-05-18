package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import java.util.List;

public interface AuctionRepository {

    Auction findById(String id) throws Exception;

    List<Auction> findPendingAuctions() throws Exception;

    void updateStatus(String id, String status) throws Exception;

    boolean updateBidPrice(String auctionId, double price, int bidderId) throws Exception;

    /**
     * Lấy danh sách các phiên đấu giá mà người dùng đã từng tham gia đặt giá.
     * Tên hàm phải khớp với AuctionServiceImpl đang gọi.
     */
    List<Auction> findAuctionsByBidderId(int bidderId) throws Exception;

    List<Bid> getBidHistory(String auctionId) throws Exception;
}