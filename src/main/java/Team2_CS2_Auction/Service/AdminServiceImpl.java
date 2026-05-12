package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.UserRepository;
import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    public AdminServiceImpl(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
        this.userRepository = new UserRepository();
    }

    @Override
    public List<Member> getMemberList() {
        return userRepository.findAllMembers();
    }

    /**
     * Logic khóa tài khoản nằm ở đây
     */
    @Override
    public void banMember(int id) throws Exception {
        // 1. Kiểm tra ID hợp lệ
        if (id <= 0) {
            throw new Exception("ID người dùng không hợp lệ.");
        }

        // 2. Thực hiện gọi Repository để cập nhật trạng thái
        boolean success = userRepository.updateStatus(id, "BANNED");

        // 3. Xử lý kết quả
        if (!success) {
            throw new Exception("Lỗi hệ thống: Không thể khóa tài khoản ID " + id);
        }

        System.out.println("✅ [Service] Đã khóa thành công ID: " + id);
    }

    /**
     * Logic mở khóa tài khoản
     */
    @Override
    public void unbanMember(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID người dùng không hợp lệ.");
        }

        boolean success = userRepository.updateStatus(id, "ACTIVE");

        if (!success) {
            throw new Exception("Lỗi hệ thống: Không thể mở khóa tài khoản ID " + id);
        }

        System.out.println("✅ [Service] Đã mở khóa thành công ID: " + id);
    }

    @Override
    public void approveAuction(String auctionId) throws Exception {
        auctionRepository.updateStatus(auctionId, "APPROVED");
    }

    @Override
    public void rejectAuction(String auctionId, String reason) throws Exception {
        auctionRepository.updateStatus(auctionId, "REJECTED");
    }
}