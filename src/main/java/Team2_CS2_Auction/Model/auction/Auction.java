package Team2_CS2_Auction.Model.auction;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.user.Member;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction implements Serializable {

    private String id;
    private Item item;
    private Member seller;
    private Member winner;

    private AuctionStatus status;

    private double currentPrice;
    private double stepPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private final List<Bid> bidHistory = new ArrayList<>();

    public Auction(String id,
                   Item item,
                   Member seller,
                   double startPrice,
                   double stepPrice,
                   LocalDateTime startTime,
                   LocalDateTime endTime) {

        this.id = id;
        this.item = item;
        this.seller = seller;
        this.currentPrice = startPrice;
        this.stepPrice = stepPrice;
        this.startTime = startTime;
        this.endTime = endTime;

        this.status = AuctionStatus.PENDING;
    }

    // BID
    public synchronized void addBid(Bid bid) {

        if (status != AuctionStatus.OPEN) {
            throw new IllegalStateException("Auction is not open.");
        }

        if (LocalDateTime.now().isAfter(endTime)) {
            throw new IllegalStateException("Auction has ended.");
        }

        if (bid.getBidder().getId() == this.seller.getId()) {
            throw new IllegalArgumentException("Seller cannot bid on their own auction.");
        }

        double minBid = currentPrice + stepPrice;

        if (bid.getAmount() < minBid) {
            throw new IllegalArgumentException(
                    "Bid must be at least: " + minBid
            );
        }

        bidHistory.add(bid);
        currentPrice = bid.getAmount();
        winner = bid.getBidder();
    }

    // STATUS
    public synchronized void openAuction() {
        if (status == AuctionStatus.APPROVED) {
            status = AuctionStatus.OPEN;
        }
    }

    public synchronized void closeAuction() {
        status = AuctionStatus.CLOSED;
    }

    public synchronized void cancelAuction() {
        status = AuctionStatus.CANCELLED;
    }

    public synchronized void rejectAuction() {
        status = AuctionStatus.REJECTED;
    }

    // GETTERS
    public String getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public Member getSeller() {
        return seller;
    }

    public synchronized Member getWinner() {
        return winner;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public synchronized AuctionStatus getStatus() {
        return status;
    }

    public synchronized List<Bid> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }

    // SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public synchronized void setStatus(AuctionStatus status) {
        this.status = status;
    }
}