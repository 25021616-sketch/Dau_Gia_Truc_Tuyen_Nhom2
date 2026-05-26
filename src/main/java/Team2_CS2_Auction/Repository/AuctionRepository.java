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
     * Thực thi toàn bộ quá trình đặt giá trong một Transaction duy nhất.
     */
    void executeBidTransaction(int bidderId, String auctionId, double bidAmount) throws Exception;

    /**
     * Lấy danh sách các phiên đấu giá mà người dùng đã từng tham gia đặt giá.
     * Tên hàm phải khớp với AuctionServiceImpl đang gọi.
     */
    List<Auction> findAuctionsByBidderId(int bidderId) throws Exception;

    /**
     * Lấy lịch sử đặt giá của một phiên đấu giá cụ thể.
     */
    List<Bid> getBidHistory(String auctionId) throws Exception;

    /** Lấy danh sách user_id khác nhau đã đặt giá cho 1 phiên đấu giá */
    List<Integer> getDistinctBidderIds(String auctionId) throws Exception;

    /** Cập nhật trạng thái phiên (CANCELLED, OPENING, PENDING, ...) */
    boolean updateAuctionStatus(String auctionId, String status) throws Exception;

    /** Lấy thông tin phiên đấu giá theo ID */
    Auction getAuctionById(String auctionId) throws Exception;
}