package Team2_CS2_Auction.Model.auction;

import Team2_CS2_Auction.Model.item.Other;
import Team2_CS2_Auction.Model.user.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionModelTest - Kiểm tra logic Auction")
public class AuctionModelTest {

    private Auction auction;
    private Member seller;
    private Member bidder;

    @BeforeEach
    public void setUp() {
        seller = new Member(1, "seller_user", "pass", "0111111111");
        bidder = new Member(2, "bidder_user", "pass", "0222222222");

        Other item = new Other("ITM001", "Laptop Gaming", "Electronics", "Mô tả sản phẩm", null);
        item.setNgayBatDau(LocalDateTime.now().minusHours(1));
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        auction = new Auction("AUC001", item, seller, 1000.0, 100.0,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1));
    }

    // ─── Constructor & Initial State ──────────────────────────────

    @Test
    @DisplayName("Auction mới tạo phải có trạng thái PENDING")
    public void testInitialStatusIsPending() {
        assertEquals(AuctionStatus.PENDING, auction.getStatus());
    }

    @Test
    @DisplayName("Giá khởi điểm và bước giá lưu đúng")
    public void testInitialPriceAndStep() {
        assertEquals(1000.0, auction.getCurrentPrice(), 0.001);
        assertEquals(100.0, auction.getStepPrice(), 0.001);
    }

    @Test
    @DisplayName("Thông tin seller và id lưu đúng")
    public void testAuctionFields() {
        assertEquals("AUC001", auction.getId());
        assertEquals(seller, auction.getSeller());
        assertNotNull(auction.getItem());
        assertEquals("Laptop Gaming", auction.getItem().getTenSanPham());
    }

    // ─── Status Transitions ───────────────────────────────────────

    @Test
    @DisplayName("PENDING → APPROVED: openAuction() không có effect khi chưa APPROVED")
    public void testOpenAuctionRequiresApproved() {
        // PENDING → open không có tác dụng
        auction.openAuction();
        assertEquals(AuctionStatus.PENDING, auction.getStatus(), "openAuction() không có tác dụng khi PENDING");
    }

    @Test
    @DisplayName("PENDING → APPROVED → OPEN: chuyển trạng thái đúng")
    public void testStatusTransitionPendingToOpen() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        assertEquals(AuctionStatus.OPEN, auction.getStatus());
    }

    @Test
    @DisplayName("OPEN → CLOSED: closeAuction() đóng phiên")
    public void testCloseAuction() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.closeAuction();
        assertEquals(AuctionStatus.CLOSED, auction.getStatus());
    }

    @Test
    @DisplayName("cancelAuction() → CANCELLED")
    public void testCancelAuction() {
        auction.cancelAuction();
        assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
    }

    @Test
    @DisplayName("rejectAuction() → REJECTED")
    public void testRejectAuction() {
        auction.rejectAuction();
        assertEquals(AuctionStatus.REJECTED, auction.getStatus());
    }

    // ─── addBid Validation ────────────────────────────────────────

    @Test
    @DisplayName("addBid khi phiên chưa OPEN → ném IllegalStateException")
    public void testAddBidWhenNotOpen() {
        Bid bid = new Bid("BID001", bidder, 1200.0);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> auction.addBid(bid));
        assertEquals("Phiên đấu giá chưa mở.", ex.getMessage());
    }

    @Test
    @DisplayName("addBid hợp lệ → cập nhật currentPrice và winner")
    public void testAddBidSuccess() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Bid bid = new Bid("BID001", bidder, 1200.0); // >= 1000 + 100 = 1100
        assertDoesNotThrow(() -> auction.addBid(bid));

        assertEquals(1200.0, auction.getCurrentPrice(), 0.001);
        assertEquals(bidder, auction.getWinner());
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    @DisplayName("addBid giá thấp hơn mức tối thiểu → ném IllegalArgumentException")
    public void testAddBidTooLow() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Bid lowBid = new Bid("BID002", bidder, 1050.0); // < 1000 + 100 = 1100
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auction.addBid(lowBid));
        assertTrue(ex.getMessage().contains("1100.0"), "Thông báo lỗi phải chứa giá tối thiểu");
    }

    @Test
    @DisplayName("Người bán không thể tự đặt giá cho phiên của mình")
    public void testSellerCannotBidOnOwnAuction() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Bid selfBid = new Bid("BID003", seller, 1200.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auction.addBid(selfBid));
        assertEquals("Người bán không thể tự đặt giá.", ex.getMessage());
    }

    @Test
    @DisplayName("Nhiều lần đặt giá tăng dần → currentPrice và winner cập nhật đúng")
    public void testMultipleBids() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();

        Member secondBidder = new Member(3, "bidder2", "pass", "0333333333");

        auction.addBid(new Bid("BID001", bidder, 1200.0));
        auction.addBid(new Bid("BID002", secondBidder, 1400.0));

        assertEquals(1400.0, auction.getCurrentPrice(), 0.001);
        assertEquals(secondBidder, auction.getWinner());
        assertEquals(2, auction.getBidHistory().size());
    }

    @Test
    @DisplayName("getBidHistory() trả về bản sao, không phải list gốc")
    public void testGetBidHistoryReturnsCopy() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.addBid(new Bid("BID001", bidder, 1200.0));

        var history = auction.getBidHistory();
        history.clear(); // xóa bản sao

        assertEquals(1, auction.getBidHistory().size(), "getBidHistory() phải trả về bản sao độc lập");
    }

    @Test
    @DisplayName("getCurrentBidderId() trả về -1 khi chưa có ai đặt giá")
    public void testGetCurrentBidderIdNoWinner() {
        assertEquals(-1, auction.getCurrentBidderId());
    }

    @Test
    @DisplayName("getCurrentBidderId() trả về id người thắng")
    public void testGetCurrentBidderIdWithWinner() {
        auction.setStatus(AuctionStatus.APPROVED);
        auction.openAuction();
        auction.addBid(new Bid("BID001", bidder, 1200.0));
        assertEquals(bidder.getId(), auction.getCurrentBidderId());
    }
}
