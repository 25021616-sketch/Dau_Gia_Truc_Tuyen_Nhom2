package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Service.AuctionService;
import java.time.LocalDateTime; // Cần dùng LocalDateTime
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Member extends User implements ISeller, IBidder {

    private static final long serialVersionUID = 1L;
    private double balance;
    private final List<Auction> myOwnedAuctions = new ArrayList<>();
    private final List<Auction> joinedAuctions = new ArrayList<>();
    private transient AuctionService auctionService;

    // --- Constructors giữ nguyên ---
    public Member(int id, String username, String password, String phone) {
        super(id, username, password, phone, UserRole.MEMBER);
        this.balance = 0.0;
    }

    public Member(String username, String password) {
        super(username, password, UserRole.MEMBER);
        this.balance = 0.0;
    }

    // --- Service injection ---
    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = Objects.requireNonNull(auctionService, "AuctionService không được null");
    }

    private AuctionService requireService() {
        if (auctionService == null)
            throw new IllegalStateException("AuctionService chưa được inject vào Member: " + getUsername());
        return auctionService;
    }

    // --- Balance Management ---
    public synchronized double getBalance() { return balance; }

    public synchronized void addBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền nạp phải > 0");
        balance += amount;
    }

    public synchronized void subtractBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền trừ phải > 0");
        if (balance < amount) throw new IllegalStateException("Số dư không đủ!");
        balance -= amount;
    }

    // ─── IBidder (Đặt giá) ────────────────────────────────────────

    @Override
    public void placeBid(String auctionId, int multiplier) throws Exception {
        // multiplier ở đây có thể hiểu là số bước giá (n * stepPrice)
        // Service sẽ tính toán bidAmount = currentPrice + (multiplier * stepPrice)
        requireService().placeBid(this, auctionId, (double) multiplier);
    }

    // ─── ISeller (Đăng bán) ────────────────────────────────────────

    /**
     * CẬP NHẬT: Nhận tham số thô để đẩy xuống Service xử lý Validation và Factory
     */
    public void requestCreateAuction(String name, String category, String description,
                                     String imagePath, String startPrice, String stepPrice,
                                     LocalDateTime startTime, LocalDateTime endTime) throws Exception {

        requireService().createAuction(this, name, category, description, imagePath,
                startPrice, stepPrice, startTime, endTime);
    }

    /**
     * Giữ lại hàm cũ để tránh lỗi compile nếu bạn chưa sửa hết các interface liên quan,
     * nhưng nội dung sẽ gọi về hàm mới.
     */
    @Override
    public void requestCreateAuction(Item item, double startPrice, double stepPrice, String endDateTime) throws Exception {
        // Chuyển đổi từ Item object sang tham số thô để Service xử lý tập trung
        requireService().createAuction(this, item.getTenSanPham(), item.getLoaiSanPham(),
                item.getMoTa(), item.getImagePath(),
                String.valueOf(startPrice), String.valueOf(stepPrice),
                item.getNgayBatDau(), item.getNgayKetThuc());
    }

    // ─── Quản lý danh sách Auction ───────────────────────────────

    public synchronized void addOwnedAuction(Auction auction) {
        Objects.requireNonNull(auction, "Auction không được null");
        myOwnedAuctions.add(auction);
    }

    public synchronized void addJoinedAuction(Auction auction) {
        Objects.requireNonNull(auction, "Auction không được null");
        if (!joinedAuctions.contains(auction)) joinedAuctions.add(auction);
    }

    public synchronized List<Auction> getMyOwnedAuctions() { return new ArrayList<>(myOwnedAuctions); }
    public synchronized List<Auction> getJoinedAuctions() { return new ArrayList<>(joinedAuctions); }

    @Override
    public String getInfo() {
        return String.format("Member{id=%d, username='%s', balance=%.2f}", getId(), getUsername(), balance);
    }
}