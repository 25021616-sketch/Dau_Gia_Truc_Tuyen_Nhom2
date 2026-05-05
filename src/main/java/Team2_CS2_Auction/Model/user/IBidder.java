package Team2_CS2_Auction.Model.user;

public interface IBidder {

    void placeBid(String auctionId, int multiplier) throws Exception;
}