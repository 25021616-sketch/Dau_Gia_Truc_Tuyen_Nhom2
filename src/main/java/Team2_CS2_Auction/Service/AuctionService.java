package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.user.User;
import java.time.LocalDateTime;
import java.util.List;

public interface AuctionService {
    // Đăng sản phẩm mới
    void createAuction(User seller, String name, String category, String description,
                              String imagePath, String startPrice, String stepPrice,
                              LocalDateTime startTime, LocalDateTime endTime) throws Exception;

    // Lấy danh sách cho Trang Chủ (Tất cả sản phẩm OPENING)
    List<Auction> getActiveAuctions() throws Exception;

    // MỚI: Lấy danh sách sản phẩm của riêng tôi (Cho trang Sản phẩm của tôi)
    List<Auction> getAuctionsBySeller(int sellerId) throws Exception;

    // Đặt giá thầu
    void placeBid(User bidder, String auctionId, double bidAmount) throws Exception;
   // Admin duyệt
    List<Auction> getPendingAuctions() throws Exception;
    void approveAuction(String auctionId) throws Exception;
    // Admin từ chối
    void rejectAuction(String auctionId) throws Exception;
    List<Auction> getAuctionsByBidder(int bidderId) throws Exception;

    List<Bid> getBidHistory(String auctionId) throws Exception;

    // Chủ sản phẩm hủy/xóa phiên đấu giá
    void cancelAuction(String auctionId) throws Exception;

    // Lấy phiên đấu giá theo ID (dùng khi cần thông tin item để đăng lại)
    Auction getAuctionById(String auctionId) throws Exception;
}
