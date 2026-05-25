package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Model.user.User;
import Team2_CS2_Auction.Repository.ProductRepository;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionServiceImpl implements AuctionService {
    private final ProductRepository productRepo = new ProductRepository();
    private final AuctionRepository auctionRepo = new AuctionRepositoryImpl();
    private final UserRepository userRepo = new UserRepository(); // Thêm để check số dư chuẩn

    public AuctionServiceImpl(AuctionRepositoryImpl auctionRepo) {
    }

    public AuctionServiceImpl() {

    }

    @Override
    public void createAuction(User seller, String name, String category, String description,
                              String imagePath, String startPrice, String stepPrice,
                              LocalDateTime startTime, LocalDateTime endTime) throws Exception {

        if (name == null || name.isEmpty()) throw new Exception("Tên sản phẩm trống!");

        double price = Double.parseDouble(startPrice);
        double step = Double.parseDouble(stepPrice);

        if (price <= 0) throw new Exception("Giá khởi điểm phải > 0!");
        if (startTime.isAfter(endTime)) throw new Exception("Thời gian không hợp lệ!");

        Item newItem = ItemFactory.createItem(null, name, category, description, imagePath);

        boolean success = productRepo.insertProduct(
                newItem.getTenSanPham(),
                newItem.getMoTa(),
                newItem.getLoaiSanPham(),
                price,
                price,
                step,
                seller.getId(),
                startTime,
                endTime,
                "PENDING",
                imagePath
        );

        if (!success) throw new Exception("Lỗi lưu Database!");
    }

    @Override
    public List<Auction> getActiveAuctions() throws Exception {
        return productRepo.getAllActiveProducts();
    }

    @Override
    public List<Auction> getAuctionsBySeller(int sellerId) throws Exception {
        return productRepo.getProductsBySellerId(sellerId);
    }

    /**
     * HÀM ĐẶT GIÁ THỐNG NHẤT - ĐÃ SỬA LOGIC CHUẨN
     */
    @Override
    public void placeBid(
            User bidder,
            String auctionId,
            double bidAmount
    ) throws Exception {

        // 1. Lấy auction
        Auction currentAuction =
                auctionRepo.findById(auctionId);

        if (currentAuction == null) {

            throw new Exception(
                    "Không tìm thấy phiên đấu giá!"
            );
        }

        // 2. Không cho chủ tự bid
        if (currentAuction.getSeller().getId()
                == bidder.getId()) {

            throw new Exception(
                    "Bạn không thể đấu giá sản phẩm của chính mình!"
            );
        }

        // 3. Check giá tối thiểu
        double minimumBid =
                currentAuction.getCurrentPrice()
                        + currentAuction.getStepPrice();

        if (bidAmount < minimumBid) {

            throw new Exception(
                    "Giá tối thiểu phải là: "
                            + minimumBid
            );
        }

        // 4. Check số dư khả dụng
        double currentBalance = userRepo.getBalance(bidder.getId());
        double currentLocked = userRepo.getLockedBalance(bidder.getId());
        double availableMoney = currentBalance - currentLocked;

        // 5. Lấy người giữ giá cao nhất cũ
        int oldHighestBidderId = userRepo.getHighestBidderId(auctionId);

        // Giá hiện tại trước khi update
        double oldCurrentPrice = currentAuction.getCurrentPrice();

        double requiredExtraMoney;

        // ======================================
        // CASE 1: USER TỰ NÂNG GIÁ
        // Chỉ cộng phần chênh lệch
        // ======================================
        if (oldHighestBidderId == bidder.getId()) {
            requiredExtraMoney = bidAmount - oldCurrentPrice;
        } else {
            // ======================================
            // CASE 2: USER MỚI THAM GIA
            // Lock full giá bid
            // ======================================
            requiredExtraMoney = bidAmount;
        }

        System.out.println("========== DEBUG BID ==========");
        System.out.println("Balance DB = " + currentBalance);
        System.out.println("Locked DB = " + currentLocked);
        System.out.println("Available = " + availableMoney);
        System.out.println("Bid Amount = " + bidAmount);
        System.out.println("Required Extra = " + requiredExtraMoney);

        // KIỂM TRA SỐ DƯ TRƯỚC KHI LOCK TIỀN!
        if (availableMoney < requiredExtraMoney) {
            throw new Exception("Số dư khả dụng không đủ!");
        }

        double newLockedAmount = currentLocked + requiredExtraMoney;

        // Update lock mới
        boolean locked = userRepo.updateLockedBalance(bidder.getId(), newLockedAmount);

        if (!locked) {
            throw new Exception("Không thể lock tiền!");
        }


        // ======================================
        // UNLOCK NGƯỜI BỊ VƯỢT GIÁ
        // ======================================

        if (oldHighestBidderId > 0 &&
                oldHighestBidderId != bidder.getId()) {

            double oldLocked =
                    userRepo.getLockedBalance(
                            oldHighestBidderId
                    );

            double newOldLocked =
                    oldLocked - oldCurrentPrice;

            if (newOldLocked < 0) {
                newOldLocked = 0;
            }

            userRepo.updateLockedBalance(
                    oldHighestBidderId,
                    newOldLocked
            );

            System.out.println(
                    "UNLOCK user cũ: "
                            + oldHighestBidderId
            );
        }

        // 8. Update giá mới
        boolean success =
                auctionRepo.updateBidPrice(
                        auctionId,
                        bidAmount,
                        bidder.getId()
                );

        // 9. Rollback nếu fail
        if (!success) {

            userRepo.updateLockedBalance(
                    bidder.getId(),
                    currentLocked
            );

            throw new Exception(
                    "Đặt giá thất bại!"
            );
        }

        System.out.println(
                "✅ Đặt giá thành công và đã lock tiền"
        );

        bidder.setLockedBalance(
                userRepo.getLockedBalance(
                        bidder.getId()
                )
        );
    }

    @Override
    public List<Auction> getPendingAuctions() throws Exception {
        return auctionRepo.findPendingAuctions();
    }

    @Override
    public void approveAuction(String auctionId) throws Exception {
        auctionRepo.updateStatus(auctionId, "OPENING");
    }

    @Override
    public void rejectAuction(String auctionId) throws Exception {
        int productId = Integer.parseInt(auctionId.replace("AUC_", ""));
        if (!productRepo.rejectProduct(productId)) {
            throw new Exception("Không thể từ chối sản phẩm!");
        }
    }
    @Override
    public List<Auction> getAuctionsByBidder(int bidderId) throws Exception {
        System.out.println("DEBUG SERVICE: Đang gọi Repo lấy danh sách cho bidder: " + bidderId);
        return auctionRepo.findAuctionsByBidderId(bidderId);
    }

    @Override
    public List<Bid> getBidHistory(String auctionId) throws Exception {
        return auctionRepo.getBidHistory(auctionId);
    }
}