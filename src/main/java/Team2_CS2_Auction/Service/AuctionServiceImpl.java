package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Repository.ProductRepository;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionServiceImpl implements AuctionService {
    private final ProductRepository productRepo = new ProductRepository();
    private final AuctionRepository auctionRepo = new AuctionRepositoryImpl();

    @Override
    public void createAuction(Member seller, String name, String category, String description,
                              String imagePath, String startPrice, String stepPrice,
                              LocalDateTime startTime, LocalDateTime endTime) throws Exception {

        // 1. Validation (Giữ nguyên logic tốt của bạn)
        if (name == null || name.isEmpty()) throw new Exception("Tên sản phẩm trống!");

        double price = Double.parseDouble(startPrice);
        double step = Double.parseDouble(stepPrice);

        if (price <= 0) throw new Exception("Giá khởi điểm phải > 0!");
        if (startTime.isAfter(endTime)) throw new Exception("Thời gian không hợp lệ!");

        // 2. Factory (Giữ nguyên)
        Item newItem = ItemFactory.createItem(null, name, category, description, imagePath);

        // 3. ĐẨY XUỐNG DB VỚI TRẠNG THÁI PENDING (Để Admin duyệt)
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
                "PENDING", // Đổi từ OPENING sang PENDING
                imagePath
        );

        if (!success) throw new Exception("Lỗi lưu Database!");
    }

    @Override
    public List<Auction> getActiveAuctions() throws Exception {
        // CHỈ LẤY HÀNG ĐÃ DUYỆT (OPENING)
        return productRepo.getAllActiveProducts();
    }

    @Override
    public List<Auction> getAuctionsBySeller(int sellerId) throws Exception {
        // LẤY TẤT CẢ (Để User theo dõi tình trạng duyệt bài của mình)
        return productRepo.getProductsBySellerId(sellerId);
    }

    @Override
    public void placeBid(Member bidder, String auctionId, double bidAmount) throws Exception {
        // 1. Kiểm tra ví tiền (Giả sử Member có hàm getBalance)
        if (bidder.getBalance() < bidAmount) {
            throw new Exception("Số dư tài khoản không đủ!");
        }

        // 2. Chuyển đổi ID từ "AUC_12" thành 12
        int id = Integer.parseInt(auctionId.replace("AUC_", ""));

        // 3. Ghi dữ liệu xuống DB
        boolean success = productRepo.updateCurrentPrice(id, bidAmount);

        if (success) {
            System.out.println("User " + bidder.getUsername() + " đặt giá thành công: " + bidAmount);
        } else {
            throw new Exception("Lỗi khi cập nhật giá vào hệ thống!");
        }
    }
    @Override
    public List<Auction> getPendingAuctions() throws Exception {
        List<Auction> list = auctionRepo.findPendingAuctions();
        for (Auction a : list) {
            // In ra console để xem Service đã nhận được giá chưa
            System.out.println("Service nhận - ID: " + a.getAuctionId() + " | Giá: " + a.getCurrentPrice());
        }
        return list;
    }

    @Override
    public void approveAuction(String auctionId) throws Exception {
        // Gọi sang AuctionRepositoryImpl để cập nhật status: PENDING -> OPENING
        auctionRepo.updateStatus(auctionId, "OPENING");
    }

    @Override
    public void rejectAuction(String auctionId) throws Exception {

        // Chuyển AUC_12 -> 12
        int productId =
                Integer.parseInt(
                        auctionId.replace("AUC_", "")
                );

        boolean success =
                productRepo.rejectProduct(productId);

        if (!success) {

            throw new Exception(
                    "Không thể từ chối sản phẩm!"
            );
        }
    }
}