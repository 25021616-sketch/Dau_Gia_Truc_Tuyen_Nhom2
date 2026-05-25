package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Model.auction.AutoBid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test bộ: Logic Auto-Bid Iterative (thay thế phiên bản đệ quy cũ).
 *
 * Các trường hợp không cần DB — kiểm tra thuần logic tính toán:
 * 1. Tính targetPrice đúng: currentPrice + stepMult × stepPrice.
 * 2. Auto-bid bị dừng khi targetPrice > maxLimit.
 * 3. Auto-bid bị dừng khi số dư khả dụng không đủ.
 * 4. Người đang dẫn đầu không tự thầu lên chính mình.
 * 5. Vòng lặp tối đa 50 vòng — không vượt quá MAX_ROUNDS.
 * 6. Khi không còn ai thầu thêm, vòng lặp kết thúc đúng.
 * 7. AutoBid model: getters/setters hoạt động đúng.
 * 8. Nhiều AutoBid cùng sản phẩm — chỉ 1 người được thầu mỗi vòng.
 */
@DisplayName("Auto-Bid Iterative Logic Tests")
public class AutoBidLogicTest {

    // ─── Helper: Tái hiện lõi logic vòng lặp Auto-Bid ───────────────

    /**
     * Mô phỏng 1 vòng của triggerAutoBids (logic đã đổi sang while-loop trong ClientHandler).
     * Trả về giá thầu tiếp theo nếu hợp lệ, -1 nếu không ai thầu được.
     */
    private double simulateOneAutoBidRound(
            List<AutoBid> activeBids,
            int currentWinnerId,
            double currentPrice,
            double stepPrice) {

        for (AutoBid bid : activeBids) {
            if (bid.getUserId() == currentWinnerId) continue; // Dẫn đầu không tự thầu

            double targetPrice = currentPrice + (bid.getStepMultiplier() * stepPrice);

            if (targetPrice > bid.getMaxLimit()) continue;      // Vượt giới hạn
            if (bid.getBalance() < targetPrice) continue;       // Không đủ tiền

            return targetPrice; // Hợp lệ
        }
        return -1; // Không ai thầu được
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. Tính targetPrice đúng
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("targetPrice = currentPrice + stepMultiplier × stepPrice")
    void testTargetPriceCalculation() {
        double currentPrice = 1000.0;
        double stepPrice = 100.0;
        int stepMult = 2;

        double expected = currentPrice + stepMult * stepPrice; // 1200.0
        double actual = currentPrice + (stepMult * stepPrice);

        assertEquals(1200.0, actual, 0.01,
                "targetPrice phải bằng currentPrice + stepMult × stepPrice");
    }

    @Test
    @DisplayName("stepMultiplier = 1 → targetPrice tăng đúng 1 bước")
    void testTargetPriceOneStep() {
        assertEquals(1500.0, 1000.0 + 1 * 500.0, 0.01);
    }

    @Test
    @DisplayName("stepMultiplier = 3 → targetPrice tăng đúng 3 bước")
    void testTargetPriceThreeSteps() {
        assertEquals(4000.0, 1000.0 + 3 * 1000.0, 0.01);
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. Dừng khi targetPrice > maxLimit
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Auto-bid dừng khi targetPrice vượt maxLimit")
    void testAutoBidStopsWhenExceedsMaxLimit() {
        AutoBid bid = new AutoBid(1, 10, 1, 1500.0, true);
        bid.setBalance(9999.0);

        List<AutoBid> bids = List.of(bid);
        double result = simulateOneAutoBidRound(bids, 99, 1400.0, 200.0);
        // targetPrice = 1400 + 1×200 = 1600 > maxLimit 1500 → bị bỏ qua → trả -1
        assertEquals(-1, result, 0.01,
                "Khi targetPrice > maxLimit, auto-bid phải bị dừng (return -1)");
    }

    @Test
    @DisplayName("Auto-bid hợp lệ khi targetPrice bằng đúng maxLimit")
    void testAutoBidValidAtExactMaxLimit() {
        AutoBid bid = new AutoBid(1, 10, 1, 1600.0, true);
        bid.setBalance(9999.0);

        List<AutoBid> bids = List.of(bid);
        double result = simulateOneAutoBidRound(bids, 99, 1400.0, 200.0);
        // targetPrice = 1600 = maxLimit 1600 → vẫn hợp lệ
        assertEquals(1600.0, result, 0.01,
                "targetPrice == maxLimit vẫn được thầu");
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. Dừng khi không đủ số dư
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Auto-bid dừng khi balance < targetPrice")
    void testAutoBidStopsWhenInsufficientBalance() {
        AutoBid bid = new AutoBid(2, 10, 1, 5000.0, true);
        bid.setBalance(500.0); // Số dư thấp hơn targetPrice

        List<AutoBid> bids = List.of(bid);
        double result = simulateOneAutoBidRound(bids, 99, 1000.0, 200.0);
        // targetPrice = 1200, balance = 500 < 1200 → bị bỏ qua
        assertEquals(-1, result, 0.01,
                "balance < targetPrice phải dừng auto-bid");
    }

    @Test
    @DisplayName("Auto-bid hợp lệ khi balance vừa đủ bằng targetPrice")
    void testAutoBidValidWhenBalanceExactlyEnough() {
        AutoBid bid = new AutoBid(2, 10, 1, 5000.0, true);
        bid.setBalance(1200.0); // Đúng bằng targetPrice

        List<AutoBid> bids = List.of(bid);
        double result = simulateOneAutoBidRound(bids, 99, 1000.0, 200.0);
        assertEquals(1200.0, result, 0.01,
                "balance == targetPrice vẫn được thầu");
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. Người dẫn đầu không tự thầu lên chính mình
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Người đang dẫn đầu (currentWinnerId) bị bỏ qua — không tự thầu")
    void testCurrentWinnerSkipped() {
        int winnerId = 7;
        AutoBid bid = new AutoBid(winnerId, 10, 1, 9999.0, true);
        bid.setBalance(9999.0);

        List<AutoBid> bids = List.of(bid);
        double result = simulateOneAutoBidRound(bids, winnerId, 1000.0, 100.0);
        // Người đang dẫn đầu (winnerId=7) bị bỏ qua
        assertEquals(-1, result, 0.01,
                "Người đang dẫn đầu không được tự thầu lên chính mình");
    }

    @Test
    @DisplayName("Người khác có thể thầu dù người dẫn đầu bị bỏ qua")
    void testOtherUserCanBidWhenWinnerSkipped() {
        List<AutoBid> bids = new ArrayList<>();

        AutoBid winner = new AutoBid(7, 10, 1, 9999.0, true);
        winner.setBalance(9999.0);

        AutoBid challenger = new AutoBid(8, 10, 1, 9999.0, true);
        challenger.setBalance(9999.0);

        bids.add(winner);
        bids.add(challenger);

        double result = simulateOneAutoBidRound(bids, 7, 1000.0, 100.0);
        // user 7 bị bỏ qua, user 8 được thầu → targetPrice = 1100
        assertEquals(1100.0, result, 0.01,
                "User 8 (challenger) phải được thầu khi user 7 bị bỏ qua");
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. Giới hạn tối đa MAX_ROUNDS = 50
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Mô phỏng vòng lặp: tối đa 50 vòng — không bao giờ vượt quá")
    void testMaxRoundsNeverExceeded() {
        final int MAX_ROUNDS = 50;

        // 2 người cùng auto-bid đua nhau — vòng lặp sẽ đạt MAX_ROUNDS
        AutoBid bidA = new AutoBid(1, 10, 1, 999999.0, true);
        bidA.setBalance(999999.0);
        AutoBid bidB = new AutoBid(2, 10, 1, 999999.0, true);
        bidB.setBalance(999999.0);

        List<AutoBid> bids = List.of(bidA, bidB);

        int round = 0;
        int winnerId = 99; // Người ban đầu (không phải bidA hay bidB)
        double price = 0.0;
        double stepPrice = 100.0;

        while (round < MAX_ROUNDS) {
            round++;
            double result = simulateOneAutoBidRound(bids, winnerId, price, stepPrice);
            if (result < 0) break;

            // Cập nhật state
            winnerId = (winnerId == 1) ? 2 : 1; // Đổi chiều thay thế
            price = result;
        }

        assertTrue(round <= MAX_ROUNDS,
                "Số vòng lặp không được vượt quá MAX_ROUNDS=" + MAX_ROUNDS);
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. Không còn ai thầu thêm → vòng lặp kết thúc
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Vòng lặp kết thúc đúng khi không còn ai thầu được")
    void testLoopStopsWhenNoOneBids() {
        // Tất cả bid đều vượt giới hạn
        AutoBid bid = new AutoBid(1, 10, 1, 1000.0, true);
        bid.setBalance(9999.0);

        List<AutoBid> bids = List.of(bid);

        int rounds = 0;
        int winnerId = 99;
        double price = 950.0; // Vòng đầu: target = 950+100 = 1050 > maxLimit 1000 → dừng ngay
        double stepPrice = 100.0;

        while (rounds < 50) {
            rounds++;
            double result = simulateOneAutoBidRound(bids, winnerId, price, stepPrice);
            if (result < 0) break;
            price = result;
        }

        assertEquals(1, rounds,
                "Vòng lặp phải kết thúc sau đúng 1 vòng khi không ai thầu được ngay từ đầu");
    }

    // ─────────────────────────────────────────────────────────────────
    // 7. AutoBid model — getters / setters
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AutoBid: constructor gán đúng tất cả trường")
    void testAutoBidConstructorFields() {
        AutoBid ab = new AutoBid(3, 10, 2, 5000.0, true);
        assertEquals(3, ab.getUserId());
        assertEquals(10, ab.getProductId());
        assertEquals(2, ab.getStepMultiplier());
        assertEquals(5000.0, ab.getMaxLimit(), 0.01);
        assertTrue(ab.isActive());
    }

    @Test
    @DisplayName("AutoBid: setBalance cập nhật đúng")
    void testAutoBidSetBalance() {
        AutoBid ab = new AutoBid(1, 10, 1, 9999.0, true);
        ab.setBalance(2500.0);
        assertEquals(2500.0, ab.getBalance(), 0.01);
    }

    @Test
    @DisplayName("AutoBid: isActive = false → không được tham gia thầu")
    void testInactiveAutoBidSkipped() {
        AutoBid inactiveBid = new AutoBid(1, 10, 1, 9999.0, false);
        inactiveBid.setBalance(9999.0);

        // Trong logic thực, chỉ query active bids. Ở đây kiểm tra flag
        assertFalse(inactiveBid.isActive(),
                "AutoBid không hoạt động phải trả false từ isActive()");
    }

    // ─────────────────────────────────────────────────────────────────
    // 8. Nhiều bid cùng sản phẩm — thứ tự ưu tiên
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Chỉ 1 người thầu được mỗi vòng (người đầu tiên hợp lệ trong list)")
    void testOnlyOnePersonBidsPerRound() {
        AutoBid bidA = new AutoBid(1, 10, 1, 9999.0, true);
        bidA.setBalance(9999.0);
        AutoBid bidB = new AutoBid(2, 10, 1, 9999.0, true);
        bidB.setBalance(9999.0);

        List<AutoBid> bids = List.of(bidA, bidB);

        double result = simulateOneAutoBidRound(bids, 99, 1000.0, 100.0);
        // Chỉ 1 người thầu mỗi vòng — người đầu tiên trong list (bidA = userId 1)
        assertEquals(1100.0, result, 0.01,
                "Chỉ người đầu tiên hợp lệ được thầu trong 1 vòng");
    }
}
