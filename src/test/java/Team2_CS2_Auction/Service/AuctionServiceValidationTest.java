package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm tra logic validation của AuctionService.createAuction().
 * Không cần kết nối Database — chỉ test các điều kiện ném Exception.
 */
@DisplayName("AuctionServiceValidationTest - Kiểm tra validate đầu vào khi tạo phiên đấu giá")
public class AuctionServiceValidationTest {

    private AuctionServiceImpl service;
    private User seller;

    private final LocalDateTime futureStart = LocalDateTime.now().plusHours(1);
    private final LocalDateTime futureEnd   = LocalDateTime.now().plusDays(2);

    @BeforeEach
    public void setUp() {
        service = new AuctionServiceImpl();
        seller = new Member(1, "seller", "pass", "0111111111");
        seller.setBalance(10_000_000);
    }

    @Test
    @DisplayName("Tên sản phẩm trống → ném Exception")
    public void testEmptyProductNameThrows() {
        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, "", "Khác", "Mô tả", "", "1000", "100", futureStart, futureEnd)
        );
        assertEquals("Tên sản phẩm trống!", ex.getMessage());
    }

    @Test
    @DisplayName("Tên sản phẩm null → ném Exception")
    public void testNullProductNameThrows() {
        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, null, "Khác", "Mô tả", "", "1000", "100", futureStart, futureEnd)
        );
        assertEquals("Tên sản phẩm trống!", ex.getMessage());
    }

    @Test
    @DisplayName("Giá khởi điểm = 0 → ném Exception")
    public void testZeroStartPriceThrows() {
        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "0", "100", futureStart, futureEnd)
        );
        assertTrue(ex.getMessage().contains("Giá khởi điểm"));
    }

    @Test
    @DisplayName("Giá khởi điểm âm → ném Exception")
    public void testNegativeStartPriceThrows() {
        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "-500", "100", futureStart, futureEnd)
        );
        assertTrue(ex.getMessage().contains("Giá khởi điểm"));
    }

    @Test
    @DisplayName("Bước giá = 0 → ném Exception")
    public void testZeroStepPriceThrows() {
        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "1000", "0", futureStart, futureEnd)
        );
        assertTrue(ex.getMessage().contains("Bước giá"));
    }

    @Test
    @DisplayName("Thời gian kết thúc trước thời gian bắt đầu → ném Exception")
    public void testEndBeforeStartThrows() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end   = LocalDateTime.now().plusDays(1);

        Exception ex = assertThrows(Exception.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "1000", "100", start, end)
        );
        assertTrue(ex.getMessage().contains("Thời gian"));
    }

    @Test
    @DisplayName("Giá nhập không phải số → ném NumberFormatException")
    public void testNonNumericPriceThrows() {
        assertThrows(NumberFormatException.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "abc", "100", futureStart, futureEnd)
        );
    }

    @Test
    @DisplayName("Bước giá không phải số → ném NumberFormatException")
    public void testNonNumericStepThrows() {
        assertThrows(NumberFormatException.class, () ->
                service.createAuction(seller, "Sản phẩm A", "Khác", "Mô tả", "", "1000", "xyz", futureStart, futureEnd)
        );
    }
}
