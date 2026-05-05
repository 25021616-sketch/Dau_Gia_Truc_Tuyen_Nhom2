package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.item.Item;

public interface ISeller {

    void requestCreateAuction(Item item,
                              double startPrice,
                              double stepPrice,
                              String endDateTime) throws Exception;
}