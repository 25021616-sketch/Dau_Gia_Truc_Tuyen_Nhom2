package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {
    private Admin admin;

    @BeforeEach
    public void setUp() {
        // Khởi tạo một tài khoản Admin để test
        admin = new Admin(1, "admin_test", "123456", "0987654321");
    }

    @Test
    public void testRoleIsAdmin() {
        assertEquals(UserRole.ADMIN, admin.getRole(), "Quyền của Admin phải luôn là ADMIN");
    }

    @Test
    public void testApproveAuctionWithoutServiceThrowsException() {
        // Kiểm tra logic: Nếu Admin chưa được inject service thì khi duyệt sẽ văng lỗi
        Exception exception = assertThrows(Exception.class, () -> {
            admin.approveAuction("AUC_123");
        });
        assertEquals("Chưa kết nối AdminService", exception.getMessage());
    }

    @Test
    public void testRejectAuctionWithoutServiceThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            admin.rejectAuction("AUC_123", "Thiếu thông tin");
        });
        assertEquals("Chưa kết nối AdminService", exception.getMessage());
    }

    @Test
    public void testApproveAuctionWithServiceSuccess() {
        // Giả lập (Mock) một AdminService để test hành vi
        AdminService mockService = new AdminService() {
            @Override public void approveAuction(String auctionId) throws Exception {
                // Giả lập thành công không ném lỗi
            }
            @Override public void rejectAuction(String auctionId, String reason) throws Exception {}
            @Override public List<Member> getMemberList() { return null; }
            @Override public void banMember(int id) throws Exception {}
            @Override public void unbanMember(int id) throws Exception {}
        };
        
        admin.setAdminService(mockService);
        
        // Gọi approve không sinh ra lỗi là pass test
        assertDoesNotThrow(() -> {
            admin.approveAuction("AUC_123");
        });
    }
}
