package Team2_CS2_Auction.Model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MemberTest {

    @Test
    public void testMemberCreation() {
        Member member = new Member("testUser", "password123", "0123456789");
        assertEquals("testUser", member.getUsername());
        assertEquals("0123456789", member.getPhone());
        assertEquals(UserRole.MEMBER, member.getRole());
        assertEquals(0.0, member.getBalance(), "Số dư ban đầu phải là 0");
    }

    @Test
    public void testAddBalanceSuccess() {
        Member member = new Member("testUser", "password123", "0123456789");
        member.addBalance(100.50);
        assertEquals(100.50, member.getBalance(), "Số dư sau khi nạp tiền chưa chính xác");
    }

    @Test
    public void testAddBalanceNegativeThrowsException() {
        Member member = new Member("testUser", "password123", "0123456789");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            member.addBalance(-50.0);
        });
        assertEquals("Số tiền nạp phải > 0", exception.getMessage());
    }

    @Test
    public void testSubtractBalanceSuccess() {
        Member member = new Member("testUser", "password123", "0123456789");
        member.addBalance(200.0);
        member.subtractBalance(50.0);
        assertEquals(150.0, member.getBalance(), "Số dư sau khi trừ tiền chưa chính xác");
    }

    @Test
    public void testSubtractBalanceInsufficientFundsThrowsException() {
        Member member = new Member("testUser", "password123", "0123456789");
        member.addBalance(100.0);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            member.subtractBalance(150.0);
        });
        assertEquals("Số dư không đủ!", exception.getMessage());
    }

    @Test
    public void testSubtractBalanceNegativeThrowsException() {
        Member member = new Member("testUser", "password123", "0123456789");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            member.subtractBalance(-10.0);
        });
        assertEquals("Số tiền trừ phải > 0", exception.getMessage());
    }

    @Test
    public void testCheckPassword() {
        Member member = new Member("testUser", "password123", "0123456789");
        assertTrue(member.checkPassword("password123"), "Mật khẩu đúng phải trả về true");
        assertFalse(member.checkPassword("wrongpassword"), "Mật khẩu sai phải trả về false");
    }
}
