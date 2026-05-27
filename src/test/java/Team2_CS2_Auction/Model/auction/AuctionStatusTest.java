package Team2_CS2_Auction.Model.auction;

import Team2_CS2_Auction.Model.item.Other;
import Team2_CS2_Auction.Model.user.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm tra các trạng thái mới được thêm vào AuctionStatus:
 * EXPIRED (hết hạn duyệt), NO_BID (không ai đặt giá), FINISHED (thắng cuộc).
 */
@DisplayName("AuctionStatusTest - Kiểm tra các trạng thái phiên đấu giá mới")
public class AuctionStatusTest {

    private Auction auction;
    private Member seller;
    private Member bidder;

    @BeforeEach
    public void setUp() {
        seller = new Member(1, "seller", "pass", "0111111111");
        bidder = new Member(2, "bidder", "pass", "0222222222");

        Other item = new Other("ITM001", "Đồng hồ cổ", "Khác", "Mô tả", null);
        item.setNgayBatDau(LocalDateTime.now().minusHours(2));
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        auction = new Auction("AUC001", item, seller, 500.0, 50.0,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusDays(1));
    }

    // ─── EXPIRED ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setStatus(EXPIRED) áp dụng đúng cho phiên PENDING chưa được duyệt")
    public void testSetStatusExpired() {
        assertEquals(AuctionStatus.PENDING, auction.getStatus());
        auction.setStatus(AuctionStatus.EXPIRED);
        assertEquals(AuctionStatus.EXPIRED, auction.getStatus());
    }

    @Test
    @DisplayName("Phiên EXPIRED không thể đặt giá (cần chuyển OPEN trước)")
    public void testExpiredAuctionCannotAcceptBid() {
        auction.setStatus(AuctionStatus.EXPIRED);
        Bid bid = new Bid("BID001", bidder, 600.0);
        assertThrows(IllegalStateException.class, () -> auction.addBid(bid),
                "Phiên EXPIRED không được phép nhận bid");
    }

    @Test
    @DisplayName("Phiên EXPIRED không có winner")
    public void testExpiredAuctionHasNoWinner() {
        auction.setStatus(AuctionStatus.EXPIRED);
        assertNull(auction.getWinner());
        assertEquals(-1, auction.getCurrentBidderId());
    }

    // ─── NO_BID ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setStatus(NO_BID) áp dụng đúng cho phiên OPEN không có người đặt giá")
    public void testSetStatusNoBid() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        assertEquals(AuctionStatus.OPEN, auction.getStatus());

        auction.setStatus(AuctionStatus.NO_BID);
        assertEquals(AuctionStatus.NO_BID, auction.getStatus());
    }

    @Test
    @DisplayName("Phiên NO_BID không có winner và bid history trống")
    public void testNoBidAuctionHasNoWinnerAndEmptyHistory() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.setStatus(AuctionStatus.NO_BID);

        assertNull(auction.getWinner());
        assertTrue(auction.getBidHistory().isEmpty());
    }

    // ─── FINISHED ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setStatus(FINISHED) áp dụng đúng sau khi phiên kết thúc thành công")
    public void testSetStatusFinished() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.addBid(new Bid("BID001", bidder, 600.0));

        auction.setStatus(AuctionStatus.FINISHED);
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }

    @Test
    @DisplayName("Phiên FINISHED phải có winner là người đặt giá cao nhất")
    public void testFinishedAuctionHasWinner() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.addBid(new Bid("BID001", bidder, 600.0));

        auction.setStatus(AuctionStatus.FINISHED);

        assertNotNull(auction.getWinner());
        assertEquals(bidder.getId(), auction.getWinner().getId());
        assertEquals(600.0, auction.getCurrentPrice(), 0.001);
    }

    @Test
    @DisplayName("Phiên FINISHED với nhiều bid → winner là người đặt giá cuối cùng cao nhất")
    public void testFinishedAuctionWinnerIsLastHighestBidder() {
        Member secondBidder = new Member(3, "bidder2", "pass", "0333333333");

        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.addBid(new Bid("BID001", bidder, 600.0));
        auction.addBid(new Bid("BID002", secondBidder, 700.0));

        auction.setStatus(AuctionStatus.FINISHED);

        assertEquals(secondBidder.getId(), auction.getWinner().getId());
        assertEquals(700.0, auction.getCurrentPrice(), 0.001);
    }

    // ─── Phân biệt 3 trạng thái kết thúc ──────────────────────────────────────

    @Test
    @DisplayName("EXPIRED, NO_BID, FINISHED là 3 trạng thái kết thúc khác nhau")
    public void testThreeDistinctEndStatuses() {
        assertNotEquals(AuctionStatus.EXPIRED, AuctionStatus.NO_BID);
        assertNotEquals(AuctionStatus.EXPIRED, AuctionStatus.FINISHED);
        assertNotEquals(AuctionStatus.NO_BID, AuctionStatus.FINISHED);
    }

    @Test
    @DisplayName("Enum AuctionStatus có đủ tất cả trạng thái cần thiết")
    public void testAllRequiredStatusesExist() {
        AuctionStatus[] statuses = AuctionStatus.values();
        assertNotNull(AuctionStatus.valueOf("PENDING"));
        assertNotNull(AuctionStatus.valueOf("APPROVED"));
        assertNotNull(AuctionStatus.valueOf("OPEN"));
        assertNotNull(AuctionStatus.valueOf("CLOSED"));
        assertNotNull(AuctionStatus.valueOf("FINISHED"));
        assertNotNull(AuctionStatus.valueOf("NO_BID"));
        assertNotNull(AuctionStatus.valueOf("EXPIRED"));
        assertNotNull(AuctionStatus.valueOf("REJECTED"));
        assertNotNull(AuctionStatus.valueOf("CANCELLED"));
        assertEquals(9, statuses.length, "Phải có đúng 9 trạng thái trong AuctionStatus");
    }
}
