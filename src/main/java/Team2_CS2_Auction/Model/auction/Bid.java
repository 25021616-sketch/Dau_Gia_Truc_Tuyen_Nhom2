package Team2_CS2_Auction.Model.auction;

import Team2_CS2_Auction.Model.user.Member;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Bid implements Serializable {

    private final String id;
    private final Member bidder;
    private final double amount;
    private final LocalDateTime time;

    public Bid(String id, Member bidder, double amount) {
        this.id = id;
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public Bid(String id, Member bidder, double amount, LocalDateTime time) {
        this.id = id;
        this.bidder = bidder;
        this.amount = amount;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public Member getBidder() {
        return bidder;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }
}