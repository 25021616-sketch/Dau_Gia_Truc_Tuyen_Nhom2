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

    public Auction(String id, Item item, Member seller, double startPrice, double stepPrice,
                   LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.item = item;
        this.seller = seller;
        this.currentPrice = startPrice;
        this.stepPrice = stepPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.PENDING;
    }

    public synchronized void addBid(Bid bid) {
        if (status != AuctionStatus.OPEN) throw new IllegalStateException("Phiên đấu giá chưa mở.");
        if (LocalDateTime.now().isAfter(endTime)) throw new IllegalStateException("Phiên đấu giá đã kết thúc.");
        if (bid.getBidder().getId()) throw new IllegalArgumentException("Người bán không thể tự đặt giá.");

        double minBid = currentPrice + stepPrice;
        if (bid.getAmount() < minBid) throw new IllegalArgumentException("Giá đặt tối thiểu phải là: " + minBid);

        bidHistory.add(bid);
        currentPrice = bid.getAmount();
        winner = bid.getBidder();
    }

    // Các hàm Status và Getters/Setters giữ nguyên như code của bạn
    public synchronized void openAuction() { if (status == AuctionStatus.APPROVED) status = AuctionStatus.OPEN; }
    public synchronized void closeAuction() { status = AuctionStatus.CLOSED; }
    public synchronized void cancelAuction() { status = AuctionStatus.CANCELLED; }
    public synchronized void rejectAuction() { status = AuctionStatus.REJECTED; }

    // ... (Giữ toàn bộ getters / setters như code của bạn)
    public String getId() { return id; }
    public Item getItem() { return item; }
    public Member getSeller() { return seller; }
    public synchronized Member getWinner() { return winner; }
    public synchronized double getCurrentPrice() { return currentPrice; }
    public double getStepPrice() { return stepPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public synchronized AuctionStatus getStatus() { return status; }
    public synchronized List<Bid> getBidHistory() { return new ArrayList<>(bidHistory); }
    public void setId(String id) { this.id = id; }
    public synchronized void setStatus(AuctionStatus status) { this.status = status; }
}