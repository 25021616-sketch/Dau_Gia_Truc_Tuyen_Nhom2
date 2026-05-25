package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.auction.AuctionStatus;
import Team2_CS2_Auction.Model.auction.Bid;
import Team2_CS2_Auction.Model.item.Other;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test kiểm tra toàn bộ logic validation nhập liệu:
 * - Tên tài khoản, mật khẩu, số điện thoại
 * - Số tiền nạp/trừ
 * - Giá đặt trong phiên đấu giá
 * - Thông tin phiên đấu giá (tên, thời gian)
 */
@DisplayName("InputValidationTest - Kiểm tra validation nhập liệu")
public class InputValidationTest {

    // ─── User - Username Validation ───────────────────────────────

    @Test
    @DisplayName("Tạo User với username rỗng → ném IllegalArgumentException")
    public void testEmptyUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Member("", "password123", "0123456789"),
                "Username rỗng phải ném exception");
    }

    @Test
    @DisplayName("Tạo User với username null → ném IllegalArgumentException")
    public void testNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Member(null, "password123", "0123456789"),
                "Username null phải ném exception");
    }

    @Test
    @DisplayName("Tạo User với username chỉ khoảng trắng → ném IllegalArgumentException")
    public void testBlankUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Member("   ", "password123", "0123456789"),
                "Username chỉ khoảng trắng phải ném exception");
    }

    @Test
    @DisplayName("Tạo User với username hợp lệ → thành công")
    public void testValidUsernameSuccess() {
        assertDoesNotThrow(() -> new Member("valid_user", "password123", "0123456789"));
    }

    // ─── User - Password Validation ───────────────────────────────

    @Test
    @DisplayName("Tạo User với mật khẩu rỗng → ném IllegalArgumentException")
    public void testEmptyPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Member("testUser", "", "0123456789"),
                "Mật khẩu rỗng phải ném exception");
    }

    @Test
    @DisplayName("Tạo User với mật khẩu null → ném IllegalArgumentException")
    public void testNullPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Member("testUser", null, "0123456789"),
                "Mật khẩu null phải ném exception");
    }

    @Test
    @DisplayName("setUsername với giá trị rỗng → ném IllegalArgumentException")
    public void testSetEmptyUsername() {
        Member m = new Member("user1", "pass", "0123");
        assertThrows(IllegalArgumentException.class, () -> m.setUsername(""),
                "setUsername rỗng phải ném exception");
    }

    @Test
    @DisplayName("setPassword với giá trị rỗng → ném IllegalArgumentException")
    public void testSetEmptyPassword() {
        Member m = new Member("user1", "pass", "0123");
        assertThrows(IllegalArgumentException.class, () -> m.setPassword(""),
                "setPassword rỗng phải ném exception");
    }

    // ─── Balance - Nhập Tiền Validation ──────────────────────────

    @Test
    @DisplayName("Nạp số tiền cực lớn (BigDecimal-like) → thành công")
    public void testAddVeryLargeBalance() {
        Member m = new Member("user1", "pass", "0123");
        m.addBalance(Double.MAX_VALUE / 2);
        assertTrue(m.getBalance() > 0, "Số dư phải dương sau khi nạp số lớn");
    }

    @Test
    @DisplayName("Nạp tiền nhiều lần → cộng dồn đúng")
    public void testAddBalanceMultipleTimes() {
        Member m = new Member("user1", "pass", "0123");
        m.addBalance(100.0);
        m.addBalance(200.0);
        m.addBalance(300.0);
        assertEquals(600.0, m.getBalance(), 0.001, "Tổng số dư phải là 600.0");
    }

    @Test
    @DisplayName("Trừ đúng số dư hiện có → balance = 0")
    public void testSubtractExactBalance() {
        Member m = new Member("user1", "pass", "0123");
        m.addBalance(500.0);
        m.subtractBalance(500.0);
        assertEquals(0.0, m.getBalance(), 0.001, "Số dư phải là 0 sau khi trừ hết");
    }

    @Test
    @DisplayName("Trừ 0.01 đồng khi số dư là 0 → ném IllegalStateException")
    public void testSubtractSmallAmountFromZeroBalance() {
        Member m = new Member("user1", "pass", "0123");
        assertThrows(IllegalStateException.class, () -> m.subtractBalance(0.01));
    }

    // ─── User ID Validation ───────────────────────────────────────

    @Test
    @DisplayName("setId với giá trị âm → ném IllegalArgumentException")
    public void testSetNegativeId() {
        Member m = new Member("user1", "pass", "0123");
        assertThrows(IllegalArgumentException.class, () -> m.setId(-1),
                "ID âm phải ném exception");
    }

    @Test
    @DisplayName("setId với giá trị 0 → hợp lệ (ID tạm thời)")
    public void testSetIdZero() {
        Member m = new Member("user1", "pass", "0123");
        assertDoesNotThrow(() -> m.setId(0), "ID = 0 là hợp lệ (chưa lưu vào DB)");
    }

    @Test
    @DisplayName("setId với giá trị dương → hợp lệ")
    public void testSetPositiveId() {
        Member m = new Member("user1", "pass", "0123");
        m.setId(42);
        assertEquals(42, m.getId());
    }

    // ─── Auction Bid Validation ───────────────────────────────────

    @Test
    @DisplayName("Bid với giá = giá hiện tại + bước giá - 0.01 → không hợp lệ")
    public void testBidJustBelowMinimum() {
        Member seller = new Member(1, "seller", "pass", "0111");
        Member bidder = new Member(2, "bidder", "pass", "0222");

        Other item = new Other("ITM1", "Test", "Cat", "Desc", null);
        item.setNgayBatDau(LocalDateTime.now().minusHours(1));
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        // Giá khởi điểm 1000, bước giá 100 → giá tối thiểu = 1100
        Auction auction = new Auction("AUC1", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1));
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Bid invalidBid = new Bid("BID001", bidder, 1099.99); // Dưới 1100
        assertThrows(IllegalArgumentException.class, () -> auction.addBid(invalidBid),
                "Giá đặt dưới mức tối thiểu phải ném exception");
    }

    @Test
    @DisplayName("Bid với giá = giá hiện tại + bước giá (đúng minimum) → hợp lệ")
    public void testBidAtExactMinimum() {
        Member seller = new Member(1, "seller", "pass", "0111");
        Member bidder = new Member(2, "bidder", "pass", "0222");

        Other item = new Other("ITM1", "Test", "Cat", "Desc", null);
        item.setNgayBatDau(LocalDateTime.now().minusHours(1));
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        Auction auction = new Auction("AUC1", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1));
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Bid validBid = new Bid("BID001", bidder, 1100.0); // Đúng minimum
        assertDoesNotThrow(() -> auction.addBid(validBid),
                "Giá đặt đúng mức tối thiểu phải hợp lệ");
        assertEquals(1100.0, auction.getCurrentPrice(), 0.001);
    }

    // ─── Admin Constructor Validation ─────────────────────────────

    @Test
    @DisplayName("Tạo Admin với username rỗng → ném IllegalArgumentException")
    public void testAdminEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                new Admin("", "pass123"),
                "Admin username rỗng phải ném exception");
    }

    @Test
    @DisplayName("Tạo Admin với password rỗng → ném IllegalArgumentException")
    public void testAdminEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                new Admin("adminUser", ""),
                "Admin password rỗng phải ném exception");
    }

    // ─── Member equals/hashCode ───────────────────────────────────

    @Test
    @DisplayName("Hai Member có cùng ID → bằng nhau")
    public void testMemberEquality() {
        Member m1 = new Member(1, "user1", "pass", "0111");
        Member m2 = new Member(1, "user_different_name", "other_pass", "0222");
        assertEquals(m1, m2, "Hai Member cùng ID phải bằng nhau");
    }

    @Test
    @DisplayName("Hai Member có ID = 0 → không bằng nhau (chưa lưu DB)")
    public void testMemberEqualityWithZeroId() {
        Member m1 = new Member("user1", "pass", "0111");
        Member m2 = new Member("user1", "pass", "0111");
        assertNotEquals(m1, m2, "Hai Member ID=0 không bằng nhau");
    }
}
