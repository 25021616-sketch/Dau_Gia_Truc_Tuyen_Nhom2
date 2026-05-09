package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Service.AuctionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Đại diện cho thành viên thông thường trong hệ thống.
 * Member có thể vừa đấu giá (IBidder) vừa đăng bán (ISeller).
 */
public class Member extends User implements ISeller, IBidder {

    private static final long serialVersionUID = 1L;

    // Dùng synchronized thay vì volatile — đảm bảo atomicity
    private double balance;

    private final List<Auction> myOwnedAuctions = new ArrayList<>();
    private final List<Auction> joinedAuctions   = new ArrayList<>();

    // transient: không serialize service, inject lại sau khi load
    private transient AuctionService auctionService;

    // ─── Constructors ────────────────────────────────────────────

    public Member(int id, String username, String password, String phone) {
        super(id, username, password, phone, UserRole.MEMBER);
        this.balance = 0.0;
    }

    public Member(String username, String password) {
        super(username, password, UserRole.MEMBER);
        this.balance = 0.0;
    }

    // ─── Service injection ───────────────────────────────────────

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = Objects.requireNonNull(
                auctionService, "AuctionService không được null");
    }

    private AuctionService requireService() {
        if (auctionService == null)
            throw new IllegalStateException(
                    "AuctionService chưa được inject vào Member: " + getUsername());
        return auctionService;
    }

    // ─── Balance ─────────────────────────────────────────────────

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void addBalance(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Số tiền nạp phải > 0");
        balance += amount;
    }

    /**
     * Trừ tiền từ số dư.
     * @throws IllegalStateException nếu số dư không đủ
     */
    public synchronized void subtractBalance(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Số tiền trừ phải > 0");
        if (balance < amount)
            throw new IllegalStateException(
                    "Số dư không đủ. Hiện có: " + balance + ", cần: " + amount);
        balance -= amount;
    }

    // ─── IBidder ─────────────────────────────────────────────────

    @Override
    public void placeBid(String auctionId, int multiplier) throws Exception {
        requireService().placeBid(this, auctionId, multiplier);
    }

    // ─── ISeller ─────────────────────────────────────────────────

    @Override
    public void requestCreateAuction(Item item,
                                     double startPrice,
                                     double stepPrice,
                                     String endDateTime) throws Exception {
        requireService().createAuction(this, item, startPrice, stepPrice, endDateTime);
    }

    // ─── Auction lists ───────────────────────────────────────────

    public synchronized void addOwnedAuction(Auction auction) {
        Objects.requireNonNull(auction, "Auction không được null");
        myOwnedAuctions.add(auction);
    }

    public synchronized void addJoinedAuction(Auction auction) {
        Objects.requireNonNull(auction, "Auction không được null");
        if (!joinedAuctions.contains(auction))
            joinedAuctions.add(auction);
    }

    /** Trả về bản sao để tránh modification từ bên ngoài */
    public synchronized List<Auction> getMyOwnedAuctions() {
        return new ArrayList<>(myOwnedAuctions);
    }

    public synchronized List<Auction> getJoinedAuctions() {
        return new ArrayList<>(joinedAuctions);
    }

    // ─── Abstract override ───────────────────────────────────────

    @Override
    public String getInfo() {
        return String.format("Member{id=%d, username='%s', balance=%.2f}",
                getId(), getUsername(), balance);
    }

    // ─── Object overrides ────────────────────────────────────────

    @Override
    public String toString() {
        return getInfo();
    }
}