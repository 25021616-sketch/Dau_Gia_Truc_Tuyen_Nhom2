package Team2_CS2_Auction.Service;

public interface AdminService {

    void approveAuction(String auctionId) throws Exception;

    void rejectAuction(String auctionId,
                       String reason) throws Exception;
}