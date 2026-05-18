package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Model.user.Member;
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
    public void createAuction(Member seller, String name, String category, String description,
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
    public void placeBid(Member bidder, String auctionId, double bidAmount) throws Exception {
        // 1. Lấy thông tin phiên đấu giá từ Repo
        Auction currentAuction = auctionRepo.findById(auctionId);
        if (currentAuction == null) throw new Exception("Không tìm thấy phiên đấu giá!");

        // 2. GIỮ: Chặn chủ sản phẩm tự đấu giá
        if (currentAuction.getSeller().getId() == bidder.getId()) {
            throw new Exception("Bạn không thể đặt giá cho sản phẩm của chính mình!");
        }

        // 3. GIỮ: Kiểm tra giá đặt phải cao hơn giá hiện tại
        if (bidAmount <= currentAuction.getCurrentPrice()) {
            throw new Exception("Giá đặt phải cao hơn giá hiện tại ($" + currentAuction.getCurrentPrice() + ")");
        }

        // 4. CHỈ CẬP NHẬT GIÁ SẢN PHẨM TRONG DB
        // Hàm này sẽ UPDATE products SET current_price = ...
        boolean success = auctionRepo.updateBidPrice(auctionId, bidAmount, bidder.getId());

        if (success) {
            // KHÔNG gọi userRepo.updateBalance nữa -> Tiền trong ví giữ nguyên
            System.out.println("✅ Đã cập nhật giá mới: $" + bidAmount + " (Không trừ tiền ví)");
        } else {
            throw new Exception("Đặt giá thất bại! Có thể người khác vừa đặt mức giá cao hơn.");
        }
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