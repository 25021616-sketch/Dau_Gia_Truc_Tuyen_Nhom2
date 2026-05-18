package Team2_CS2_Auction.Model.auction;

public class BidHistory {

    private int stt;
    private String auctionId;
    private String productName;
    private String bidderName;
    private double bidAmount;
    private String bidTime;

    public BidHistory(int stt,
                      String auctionId,
                      String productName,
                      String bidderName,
                      double bidAmount,
                      String bidTime) {

        this.stt = stt;
        this.auctionId = auctionId;
        this.productName = productName;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    public int getStt() {
        return stt;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBidderName() {
        return bidderName;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public String getBidTime() {
        return bidTime;
    }
}