package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.Member; // Import Model Member của bạn
import java.util.List; // Import thư viện List của Java

public interface AdminService {

    // --- Quản lý Đấu giá ---
    void approveAuction(String auctionId) throws Exception;

    void rejectAuction(String auctionId, String reason) throws Exception;

    // --- Quản lý Người dùng ---
    List<Member> getMemberList();

    void banMember(int id) throws Exception;

    void unbanMember(int id) throws Exception;
}