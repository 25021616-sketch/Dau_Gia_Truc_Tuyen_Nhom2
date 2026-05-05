package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.user.Member;

import java.util.List;

public interface AuctionService {

    void createAuction(Member seller,
                       Item item,
                       double startPrice,
                       double stepPrice,
                       String endTime) throws Exception;

    void placeBid(Member bidder,
                  String auctionId,
                  int multiplier) throws Exception;

    List<Auction> getActiveAuctions();
}