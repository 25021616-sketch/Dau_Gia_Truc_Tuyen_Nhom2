package Team2_CS2_Auction.Model.auction;

public enum AuctionStatus {

    PENDING,     // chờ duyệt
    APPROVED,    // đã duyệt
    OPEN,        // đang đấu giá
    CLOSED,      // kết thúc
    REJECTED,    // bị từ chối
    CANCELLED    // bị hủy
}