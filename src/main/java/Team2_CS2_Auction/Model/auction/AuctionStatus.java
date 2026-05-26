package Team2_CS2_Auction.Model.auction;

public enum AuctionStatus {

    PENDING,     // chờ duyệt
    APPROVED,    // đã duyệt
    OPEN,        // đang đấu giá
    CLOSED,      // kết thúc chung
    FINISHED,    // kết thúc thành công (có người thắng)
    NO_BID,      // kết thúc thất bại (không có người đặt giá)
    EXPIRED,     // hết hạn duyệt
    REJECTED,    // bị từ chối
    CANCELLED    // bị hủy
}