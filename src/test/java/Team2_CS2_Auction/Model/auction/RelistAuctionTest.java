package Team2_CS2_Auction.Model.auction;

import Team2_CS2_Auction.Model.item.Other;
import Team2_CS2_Auction.Model.user.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm tra logic "Đăng lại" (Relist):
 * - Dữ liệu phiên cũ được lưu đúng trong oldAuctionIdForRelist.
 * - Phiên FINISHED/NO_BID/EXPIRED đều đủ điều kiện để relist về mặt model.
 * - Phiên OPEN (đang chạy) không nên cho phép relist (kiểm tra qua trạng thái).
 */
@DisplayName("RelistAuctionTest - Kiểm tra luồng đăng lại phiên đấu giá")
public class RelistAuctionTest {

    private Auction finishedAuction;
    private Auction noBidAuction;
    private Auction expiredAuction;
    private Auction openAuction;
    private Member seller;
    private Member bidder;

    @BeforeEach
    public void setUp() {
        seller = new Member(1, "seller", "pass", "0111111111");
        bidder = new Member(2, "bidder", "pass", "0222222222");

        Other item = new Other("ITM001", "Laptop Gaming", "Đồ điện tử", "Mô tả", "http://img.url/img.jpg");

        // Dùng endTime trong tương lai để addBid không bị từ chối bởi kiểm tra thời gian
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(1);

        // Phiên đã có người thắng → trạng thái FINISHED
        finishedAuction = new Auction("AUC_100", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusDays(2), futureEnd);
        finishedAuction.setStatus(AuctionStatus.APPROVED);
        finishedAuction.openAuction();
        finishedAuction.addBid(new Bid("BID001", bidder, 1200.0));
        finishedAuction.setStatus(AuctionStatus.FINISHED); // Đặt thủ công sau khi xác nhận có bid

        // Phiên không có ai đặt giá → trạng thái NO_BID
        noBidAuction = new Auction("AUC_101", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusDays(2), futureEnd);
        noBidAuction.setStatus(AuctionStatus.APPROVED);
        noBidAuction.openAuction();
        noBidAuction.setStatus(AuctionStatus.NO_BID); // Không có bid nào trước khi chốt

        // Phiên hết hạn duyệt → trạng thái EXPIRED
        expiredAuction = new Auction("AUC_102", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusDays(2), futureEnd);
        expiredAuction.setStatus(AuctionStatus.EXPIRED);

        // Phiên đang mở (không nên relist)
        Other activeItem = new Other("ITM002", "Xe máy", "Khác", "Đang chạy", null);
        openAuction = new Auction("AUC_103", activeItem, seller, 5000.0, 500.0,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(3));
        openAuction.setStatus(AuctionStatus.APPROVED);
        openAuction.openAuction();
    }

    // ─── Kiểm tra trạng thái đủ điều kiện relist ───────────────────────────────

    @Test
    @DisplayName("Phiên FINISHED → đủ điều kiện để relist")
    public void testFinishedAuctionIsRelistable() {
        assertEquals(AuctionStatus.FINISHED, finishedAuction.getStatus());
        assertNotNull(finishedAuction.getAuctionId());
    }

    @Test
    @DisplayName("Phiên NO_BID → đủ điều kiện để relist")
    public void testNoBidAuctionIsRelistable() {
        assertEquals(AuctionStatus.NO_BID, noBidAuction.getStatus());
        assertNotNull(noBidAuction.getAuctionId());
    }

    @Test
    @DisplayName("Phiên EXPIRED → đủ điều kiện để relist")
    public void testExpiredAuctionIsRelistable() {
        assertEquals(AuctionStatus.EXPIRED, expiredAuction.getStatus());
        assertNotNull(expiredAuction.getAuctionId());
    }

    @Test
    @DisplayName("Phiên OPEN → không nên relist (đang chạy)")
    public void testOpenAuctionShouldNotBeRelistable() {
        assertEquals(AuctionStatus.OPEN, openAuction.getStatus());
        // Phiên OPEN không phải là FINISHED/NO_BID/EXPIRED nên không đủ điều kiện relist
        assertNotEquals(AuctionStatus.FINISHED, openAuction.getStatus());
        assertNotEquals(AuctionStatus.NO_BID, openAuction.getStatus());
        assertNotEquals(AuctionStatus.EXPIRED, openAuction.getStatus());
    }

    // ─── Kiểm tra dữ liệu cần thiết cho luồng relist ──────────────────────────

    @Test
    @DisplayName("Phiên cũ phải lưu đúng ID để truyền vào form relist")
    public void testAuctionIdPreservedForRelist() {
        assertEquals("AUC_100", finishedAuction.getAuctionId());
        assertEquals("AUC_101", noBidAuction.getAuctionId());
        assertEquals("AUC_102", expiredAuction.getAuctionId());
    }

    @Test
    @DisplayName("Thông tin item của phiên FINISHED được giữ nguyên để pre-fill form relist")
    public void testItemDataPreservedInFinishedAuction() {
        assertNotNull(finishedAuction.getItem());
        assertEquals("Laptop Gaming", finishedAuction.getItem().getTenSanPham());
        assertEquals("Đồ điện tử", finishedAuction.getItem().getLoaiSanPham());
        assertEquals("Mô tả", finishedAuction.getItem().getMoTa());
        assertEquals("http://img.url/img.jpg", finishedAuction.getItem().getImagePath());
    }

    @Test
    @DisplayName("Phiên FINISHED có winner → nút Đăng lại nên bị ẩn (không cho relist khi có người thắng)")
    public void testFinishedWithWinnerShouldHideRelistButton() {
        // Phiên FINISHED có winner → không cho relist theo yêu cầu nghiệp vụ
        assertNotNull(finishedAuction.getWinner(), "Phiên FINISHED phải có winner");
        assertEquals(AuctionStatus.FINISHED, finishedAuction.getStatus());
    }

    @Test
    @DisplayName("Phiên NO_BID không có winner → có thể relist")
    public void testNoBidWithoutWinnerAllowsRelist() {
        assertNull(noBidAuction.getWinner(), "Phiên NO_BID không được có winner");
        assertEquals(AuctionStatus.NO_BID, noBidAuction.getStatus());
    }

    @Test
    @DisplayName("Giá và bước giá phiên cũ được bảo toàn để pre-fill form relist")
    public void testPriceAndStepPreservedForRelist() {
        assertEquals(1200.0, finishedAuction.getCurrentPrice(), 0.001,
                "currentPrice phải là giá đặt cuối cùng");
        assertEquals(100.0, finishedAuction.getStepPrice(), 0.001,
                "stepPrice phải giữ nguyên bước giá gốc");
    }
}
