package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.user.Member;
import java.time.LocalDateTime;
import java.util.List;

public interface AuctionService {
    // Đăng sản phẩm mới
    void createAuction(Member seller, String name, String category, String description,
                              String imagePath, String startPrice, String stepPrice,
                              LocalDateTime startTime, LocalDateTime endTime) throws Exception;

    // Lấy danh sách cho Trang Chủ (Tất cả sản phẩm OPENING)
    List<Auction> getActiveAuctions() throws Exception;

    // MỚI: Lấy danh sách sản phẩm của riêng tôi (Cho trang Sản phẩm của tôi)
    List<Auction> getAuctionsBySeller(int sellerId) throws Exception;

    // Đặt giá thầu
    void placeBid(Member bidder, String auctionId, double bidAmount) throws Exception;
   // Admin duyệt
    List<Auction> getPendingAuctions() throws Exception;
    void approveAuction(String auctionId) throws Exception;
    // Admin từ chối
    void rejectAuction(String auctionId) throws Exception;
    List<Auction> getAuctionsByBidder(int bidderId) throws Exception;

    List<Bid> getBidHistory(String auctionId) throws Exception;
}
