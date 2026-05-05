package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Service.AuctionService;

import java.util.ArrayList;
import java.util.List;

public class Member extends User implements ISeller, IBidder {

    private volatile double balance;

    private final List<Auction> myOwnedAuctions = new ArrayList<>();
    private final List<Auction> joinedAuctions = new ArrayList<>();

    private transient AuctionService auctionService;

    public Member(int id, String username, String password, String phone) {
        super(id, username, password, phone, "MEMBER");
        this.balance = 0;
    }
    @Override
    public String getRole() {
        return "MEMBER";
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void addBalance(double amount) {
        balance += amount;
    }

    public synchronized void subtractBalance(double amount) {
        balance -= amount;
    }

    @Override
    public void placeBid(String auctionId, int multiplier) throws Exception {
        auctionService.placeBid(this, auctionId, multiplier);
    }

    @Override
    public void requestCreateAuction(Item item,
                                     double startPrice,
                                     double stepPrice,
                                     String endDateTime) throws Exception {

        auctionService.createAuction(
                this,
                item,
                startPrice,
                stepPrice,
                endDateTime
        );
    }

    public synchronized void addOwnedAuction(Auction auction) {
        myOwnedAuctions.add(auction);
    }

    public synchronized void addJoinedAuction(Auction auction) {
        if (!joinedAuctions.contains(auction)) {
            joinedAuctions.add(auction);
        }
    }

    public synchronized List<Auction> getMyOwnedAuctions() {
        return new ArrayList<>(myOwnedAuctions);
    }

    public synchronized List<Auction> getJoinedAuctions() {
        return new ArrayList<>(joinedAuctions);
    }
}