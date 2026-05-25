package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminTest - Kiểm tra logic Admin")
public class AdminTest {

    private Admin admin;

    @BeforeEach
    public void setUp() {
        admin = new Admin(1, "admin_test", "123456", "0987654321");
    }

    // ─── Constructor & Role ─────────────────────────────────────────

    @Test
    @DisplayName("Admin phải có role ADMIN")
    public void testRoleIsAdmin() {
        assertNotNull(admin, "Admin không được null");
        assertEquals(UserRole.ADMIN, admin.getRole(), "Quyền của Admin phải luôn là ADMIN");
    }

    @Test
    @DisplayName("Tạo Admin với constructor không có ID")
    public void testAdminConstructorWithoutId() {
        Admin adminNoId = new Admin("admin2", "pass123");
        assertNotNull(adminNoId);
        assertEquals("admin2", adminNoId.getUsername());
        assertEquals(UserRole.ADMIN, adminNoId.getRole());
        assertEquals(0, adminNoId.getId(), "ID mặc định phải là 0");
    }

    @Test
    @DisplayName("getInfo() phải trả về thông tin đúng định dạng")
    public void testGetInfo() {
        String info = admin.getInfo();
        assertNotNull(info);
        assertTrue(info.contains("admin_test"), "getInfo phải chứa username");
        assertTrue(info.contains("1"), "getInfo phải chứa id");
    }

    @Test
    @DisplayName("Admin username và phone lưu đúng")
    public void testAdminFields() {
        assertEquals("admin_test", admin.getUsername());
        assertEquals("0987654321", admin.getPhone());
        assertEquals(1, admin.getId());
    }

    // ─── approveAuction ────────────────────────────────────────────

    @Test
    @DisplayName("Duyệt phiên khi chưa có AdminService → ném Exception")
    public void testApproveAuctionWithoutServiceThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            admin.approveAuction("AUC_123");
        });
        assertEquals("Chưa kết nối AdminService", exception.getMessage());
    }

    @Test
    @DisplayName("Từ chối phiên khi chưa có AdminService → ném Exception")
    public void testRejectAuctionWithoutServiceThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            admin.rejectAuction("AUC_123", "Thiếu thông tin");
        });
        assertEquals("Chưa kết nối AdminService", exception.getMessage());
    }

    @Test
    @DisplayName("Duyệt phiên thành công khi có AdminService (mock)")
    public void testApproveAuctionWithServiceSuccess() {
        AdminService mockService = new AdminService() {
            @Override public void approveAuction(String auctionId) throws Exception { /* giả lập thành công */ }
            @Override public void rejectAuction(String auctionId, String reason) throws Exception { }
            @Override public List<Member> getMemberList() { return List.of(); }
            @Override public void banMember(int id) throws Exception { }
            @Override public void unbanMember(int id) throws Exception { }
        };

        admin.setAdminService(mockService);
        assertDoesNotThrow(() -> admin.approveAuction("AUC_123"),
                "Duyệt phiên thành công không được ném lỗi");
    }

    @Test
    @DisplayName("Từ chối phiên thành công khi có AdminService (mock)")
    public void testRejectAuctionWithServiceSuccess() {
        AdminService mockService = new AdminService() {
            @Override public void approveAuction(String auctionId) throws Exception { }
            @Override public void rejectAuction(String auctionId, String reason) throws Exception { /* giả lập thành công */ }
            @Override public List<Member> getMemberList() { return List.of(); }
            @Override public void banMember(int id) throws Exception { }
            @Override public void unbanMember(int id) throws Exception { }
        };

        admin.setAdminService(mockService);
        assertDoesNotThrow(() -> admin.rejectAuction("AUC_123", "Lý do test"),
                "Từ chối phiên thành công không được ném lỗi");
    }

    @Test
    @DisplayName("AdminService ném exception → admin.approveAuction ném exception tương ứng")
    public void testApproveAuctionServiceThrowsException() {
        AdminService failingService = new AdminService() {
            @Override public void approveAuction(String auctionId) throws Exception {
                throw new Exception("Lỗi từ server");
            }
            @Override public void rejectAuction(String auctionId, String reason) throws Exception { }
            @Override public List<Member> getMemberList() { return List.of(); }
            @Override public void banMember(int id) throws Exception { }
            @Override public void unbanMember(int id) throws Exception { }
        };

        admin.setAdminService(failingService);
        Exception ex = assertThrows(Exception.class, () -> admin.approveAuction("AUC_999"));
        assertEquals("Lỗi từ server", ex.getMessage());
    }
}
