package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.AuctionStatus;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.user.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionServiceImpl implements AuctionService {

    private final List<Auction> databaseAuctions = new ArrayList<>();

    @Override
    public void createAuction(Member seller,
                              Item item,
                              double startPrice,
                              double stepPrice,
                              String endTime) throws Exception {

        if (seller == null || item == null) {
            throw new Exception("Dữ liệu không hợp lệ");
        }

        LocalDateTime end = LocalDateTime.parse(endTime);

        if (end.isBefore(LocalDateTime.now())) {
            throw new Exception("Thời gian phải ở tương lai");
        }

        String itemId = UUID.randomUUID().toString();
        item.setId(itemId);

        String auctionId = UUID.randomUUID().toString();

        Auction newAuction = new Auction(
                auctionId,
                item,
                seller,
                startPrice,
                stepPrice,
                LocalDateTime.now(),
                end
        );

        synchronized (databaseAuctions) {
            databaseAuctions.add(newAuction);
        }

        seller.addOwnedAuction(newAuction);
    }

    @Override
    public synchronized void placeBid(Member bidder,
                                      String auctionId,
                                      int multiplier) throws Exception {

        Auction auction = findAuctionById(auctionId);

        if (auction == null) {
            throw new Exception("Không tìm thấy phiên đấu giá");
        }

        if (auction.getSeller().getId() == bidder.getId()) {
            throw new Exception("Không thể tự đấu giá");
        }

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new Exception("Phiên chưa mở hoặc đã đóng");
        }

        if (multiplier <= 0) {
            throw new Exception("Multiplier phải > 0");
        }

        double newPrice =
                auction.getCurrentPrice()
                        + multiplier * auction.getStepPrice();

        if (bidder.getBalance() < newPrice) {
            throw new Exception("Không đủ số dư");
        }

        bidder.subtractBalance(newPrice);

        List<Bid> history = auction.getBidHistory();

        if (!history.isEmpty()) {
            Bid lastBid = history.get(history.size() - 1);
            lastBid.getBidder().addBalance(lastBid.getAmount());
        }

        Bid bid = new Bid(
                UUID.randomUUID().toString(),
                bidder,
                newPrice
        );

        auction.addBid(bid);
        bidder.addJoinedAuction(auction);
    }

    @Override
    public List<Auction> getActiveAuctions() {

        List<Auction> result = new ArrayList<>();

        synchronized (databaseAuctions) {
            for (Auction auction : databaseAuctions) {
                if (auction.getStatus() == AuctionStatus.OPEN) {
                    result.add(auction);
                }
            }
        }

        return result;
    }

    private Auction findAuctionById(String id) {

        synchronized (databaseAuctions) {
            for (Auction auction : databaseAuctions) {
                if (auction.getId().equals(id)) {
                    return auction;
                }
            }
        }

        return null;
    }
}