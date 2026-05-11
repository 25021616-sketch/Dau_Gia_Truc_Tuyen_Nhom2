package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Repository.AuctionRepository;
import Team2_CS2_Auction.Repository.UserRepository;
import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    // Constructor nhận vào AuctionRepository, và tự khởi tạo UserRepository
    public AdminServiceImpl(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
        this.userRepository = new UserRepository();
    }

    // --- Triển khai Quản lý Người dùng ---
    @Override
    public List<Member> getMemberList() {
        return userRepository.findAllMembers();
    }

    @Override
    public void banMember(int id) throws Exception {
        // Gọi xuống Repo để UPDATE status = 'BANNED'
        if (!userRepository.updateStatus(id, "BANNED")) {
            throw new Exception("Không thể khóa tài khoản ID: " + id);
        }
    }

    @Override
    public void unbanMember(int id) throws Exception {
        // Gọi xuống Repo để UPDATE status = 'ACTIVE'
        if (!userRepository.updateStatus(id, "ACTIVE")) {
            throw new Exception("Không thể mở khóa tài khoản ID: " + id);
        }
    }

    // --- Triển khai Quản lý Đấu giá ---
    @Override
    public void approveAuction(String auctionId) throws Exception {
        auctionRepository.updateStatus(auctionId, "APPROVED");
    }

    @Override
    public void rejectAuction(String auctionId, String reason) throws Exception {
        // Có thể bổ sung lưu lý do 'reason' vào DB nếu bảng auction có cột này
        auctionRepository.updateStatus(auctionId, "REJECTED");
    }
}