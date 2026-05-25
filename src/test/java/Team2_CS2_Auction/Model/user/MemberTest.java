package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Other;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemberTest - Kiểm tra logic Member")
public class MemberTest {

    private Member member;

    @BeforeEach
    public void setUp() {
        member = new Member("testUser", "password123", "0123456789");
    }

    // ─── Constructor & Fields ──────────────────────────────────────

    @Test
    @DisplayName("Tạo Member với đầy đủ thông tin")
    public void testMemberCreation() {
        assertNotNull(member);
        assertEquals("testUser", member.getUsername());
        assertEquals("0123456789", member.getPhone());
        assertEquals(UserRole.MEMBER, member.getRole());
        assertEquals(0.0, member.getBalance(), 0.001, "Số dư ban đầu phải là 0");
    }

    @Test
    @DisplayName("Tạo Member với constructor có ID")
    public void testMemberCreationWithId() {
        Member m = new Member(42, "userWithId", "pass", "0987654321");
        assertEquals(42, m.getId());
        assertEquals("userWithId", m.getUsername());
        assertEquals(UserRole.MEMBER, m.getRole());
    }

    @Test
    @DisplayName("Tạo Member với constructor không có phone")
    public void testMemberCreationWithoutPhone() {
        Member m = new Member("userOnly", "pass123");
        assertEquals("userOnly", m.getUsername());
        assertNull(m.getPhone(), "Phone mặc định phải null");
    }

    @Test
    @DisplayName("getInfo() trả về thông tin đúng định dạng")
    public void testGetInfo() {
        String info = member.getInfo();
        assertNotNull(info);
        assertTrue(info.contains("testUser"), "getInfo phải chứa username");
        assertTrue(info.contains("0.00"), "getInfo phải chứa balance 0.00");
    }

    // ─── Password ─────────────────────────────────────────────────

    @Test
    @DisplayName("checkPassword: đúng mật khẩu → true, sai → false")
    public void testCheckPassword() {
        assertTrue(member.checkPassword("password123"), "Mật khẩu đúng phải trả về true");
        assertFalse(member.checkPassword("wrongpassword"), "Mật khẩu sai phải trả về false");
        assertFalse(member.checkPassword(""), "Mật khẩu rỗng phải trả về false");
        assertFalse(member.checkPassword("PASSWORD123"), "Mật khẩu phân biệt hoa thường");
    }

    // ─── addBalance ────────────────────────────────────────────────

    @Test
    @DisplayName("addBalance: nạp tiền hợp lệ → cộng dồn vào balance")
    public void testAddBalanceSuccess() {
        member.addBalance(100.50);
        assertEquals(100.50, member.getBalance(), 0.001, "Số dư sau khi nạp tiền chưa chính xác");
        member.addBalance(50.0);
        assertEquals(150.50, member.getBalance(), 0.001, "Số dư sau lần nạp thứ 2 chưa chính xác");
    }

    @Test
    @DisplayName("addBalance: nạp số âm → ném IllegalArgumentException")
    public void testAddBalanceNegativeThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                member.addBalance(-50.0));
        assertEquals("Số tiền nạp phải > 0", exception.getMessage());
    }

    @Test
    @DisplayName("addBalance: nạp số 0 → ném IllegalArgumentException")
    public void testAddBalanceZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                member.addBalance(0.0), "Nạp 0 đồng phải ném exception");
    }

    // ─── subtractBalance ──────────────────────────────────────────

    @Test
    @DisplayName("subtractBalance: trừ tiền hợp lệ → balance giảm đúng")
    public void testSubtractBalanceSuccess() {
        member.addBalance(200.0);
        member.subtractBalance(50.0);
        assertEquals(150.0, member.getBalance(), 0.001, "Số dư sau khi trừ tiền chưa chính xác");
    }

    @Test
    @DisplayName("subtractBalance: số dư không đủ → ném IllegalStateException")
    public void testSubtractBalanceInsufficientFundsThrowsException() {
        member.addBalance(100.0);
        Exception exception = assertThrows(IllegalStateException.class, () ->
                member.subtractBalance(150.0));
        assertEquals("Số dư không đủ!", exception.getMessage());
    }

    @Test
    @DisplayName("subtractBalance: trừ số âm → ném IllegalArgumentException")
    public void testSubtractBalanceNegativeThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                member.subtractBalance(-10.0));
        assertEquals("Số tiền trừ phải > 0", exception.getMessage());
    }

    @Test
    @DisplayName("subtractBalance: trừ số 0 → ném IllegalArgumentException")
    public void testSubtractBalanceZeroThrowsException() {
        member.addBalance(100.0);
        assertThrows(IllegalArgumentException.class, () ->
                member.subtractBalance(0.0), "Trừ 0 đồng phải ném exception");
    }

    // ─── setBalance ────────────────────────────────────────────────

    @Test
    @DisplayName("setBalance: gán trực tiếp balance từ DB")
    public void testSetBalance() {
        member.setBalance(9999.99);
        assertEquals(9999.99, member.getBalance(), 0.001);
    }

    // ─── Auction Lists ────────────────────────────────────────────

    @Test
    @DisplayName("addOwnedAuction: thêm phiên vào danh sách phiên của mình")
    public void testAddOwnedAuction() {
        Other item = new Other("ITM1", "Test Item", "Category", "Desc", null);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));
        Auction auction = new Auction("AUC1", item, member, 100.0, 10.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        member.addOwnedAuction(auction);
        assertEquals(1, member.getMyOwnedAuctions().size());
        assertEquals("AUC1", member.getMyOwnedAuctions().get(0).getId());
    }

    @Test
    @DisplayName("addJoinedAuction: thêm phiên vào danh sách đã tham gia, không trùng lặp")
    public void testAddJoinedAuction_NoDuplicates() {
        Other item = new Other("ITM2", "Item2", "Category", "Desc", null);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));
        Auction auction = new Auction("AUC2", item, member, 200.0, 20.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        member.addJoinedAuction(auction);
        member.addJoinedAuction(auction); // thêm lần 2
        assertEquals(1, member.getJoinedAuctions().size(), "Không được thêm phiên trùng lặp");
    }

    @Test
    @DisplayName("addOwnedAuction với null → ném NullPointerException")
    public void testAddOwnedAuctionNull() {
        assertThrows(NullPointerException.class, () -> member.addOwnedAuction(null));
    }

    @Test
    @DisplayName("placeBid khi chưa có AuctionService → ném IllegalStateException")
    public void testPlaceBidWithoutService() {
        assertThrows(IllegalStateException.class, () ->
                member.placeBid("AUC_TEST", 1),
                "Đặt giá khi chưa có service phải ném exception");
    }
}
