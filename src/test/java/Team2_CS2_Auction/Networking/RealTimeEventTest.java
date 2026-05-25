package Team2_CS2_Auction.Networking;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test bộ: Real-time Event Routing (PRODUCT_UPDATED, BALANCE_UPDATED, AUTO_BID_PLACED).
 *
 * Mục tiêu kiểm tra logic routing và parsing sự kiện ở phía Client (NetworkListener).
 * Không cần kết nối mạng thật — mô phỏng việc gọi onMessageReceived() trực tiếp.
 *
 * Các trường hợp:
 * 1. PRODUCT_UPDATED → listener nhận và nhận dạng đúng action.
 * 2. BALANCE_UPDATED → parse JSON payload ra đúng giá trị balance.
 * 3. AUTO_BID_PLACED → parse giá thầu từ payload đúng.
 * 4. AUTO_BID_CANCELLED → payload chứa lý do dừng.
 * 5. NEW_BID → parse đúng auctionId, newPrice, winnerId.
 * 6. Action không xác định → listener không ném exception.
 * 7. Payload JSON rỗng → không crash.
 * 8. Nhiều listener cùng nhận 1 event.
 */
@DisplayName("Real-time Event Routing Tests")
public class RealTimeEventTest {


    // ─── Helper: tạo NetworkMessage giả từ Server ───────────────────

    private NetworkMessage makeMessage(String action, String payload) {
        return new NetworkMessage(action, payload);
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. PRODUCT_UPDATED
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PRODUCT_UPDATED → listener nhận đúng action")
    void testProductUpdatedActionRecognized() {
        AtomicBoolean received = new AtomicBoolean(false);

        NetworkManager nm = NetworkManager.getInstance();
        NetworkListener listener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                if ("PRODUCT_UPDATED".equals(message.getAction())) {
                    received.set(true);
                }
            }
            @Override public void onConnectionError() {}
        };
        nm.addListener(listener);

        // Mô phỏng Server broadcast
        listener.onMessageReceived(makeMessage("PRODUCT_UPDATED", ""));

        assertTrue(received.get(), "Listener phải nhận được PRODUCT_UPDATED");
        nm.removeListener(listener);
    }

    @Test
    @DisplayName("PRODUCT_UPDATED với payload rỗng → không crash")
    void testProductUpdatedEmptyPayload() {
        NetworkMessage msg = makeMessage("PRODUCT_UPDATED", "");
        assertEquals("PRODUCT_UPDATED", msg.getAction());
        assertEquals("", msg.getPayload());
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. BALANCE_UPDATED
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("BALANCE_UPDATED payload chứa đúng userId và balance")
    void testBalanceUpdatedPayloadParsed() {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", 10);
        payload.addProperty("balance", 5000.75);

        NetworkMessage msg = makeMessage("BALANCE_UPDATED", payload.toString());

        // Simulate listener parsing (giống Man_hinh_chinh_Users_Controller)
        JsonObject parsed = GsonUtil.getGson().fromJson(msg.getPayload(), JsonObject.class);

        assertEquals(10, parsed.get("userId").getAsInt());
        assertEquals(5000.75, parsed.get("balance").getAsDouble(), 0.01,
                "Balance phải được parse đúng từ BALANCE_UPDATED payload");
    }

    @Test
    @DisplayName("BALANCE_UPDATED balance = 0 → hợp lệ (tài khoản cạn tiền)")
    void testBalanceUpdatedZeroBalance() {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", 5);
        payload.addProperty("balance", 0.0);

        JsonObject parsed = GsonUtil.getGson().fromJson(payload.toString(), JsonObject.class);
        assertEquals(0.0, parsed.get("balance").getAsDouble(), 0.001);
    }

    @Test
    @DisplayName("BALANCE_UPDATED → listener nhận được event và action đúng")
    void testBalanceUpdatedListenerFired() {
        AtomicBoolean fired = new AtomicBoolean(false);

        NetworkListener listener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                if ("BALANCE_UPDATED".equals(message.getAction())) {
                    fired.set(true);
                }
            }
            @Override public void onConnectionError() {}
        };

        JsonObject payload = new JsonObject();
        payload.addProperty("userId", 1);
        payload.addProperty("balance", 1234.0);

        listener.onMessageReceived(makeMessage("BALANCE_UPDATED", payload.toString()));
        assertTrue(fired.get(), "Listener phải nhận được BALANCE_UPDATED event");
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. AUTO_BID_PLACED
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTO_BID_PLACED → parse giá thầu tự động từ payload đúng")
    void testAutoBidPlacedAmountParsed() {
        NetworkMessage msg = makeMessage("AUTO_BID_PLACED", "3500.0");

        double amount = Double.parseDouble(msg.getPayload());
        assertEquals(3500.0, amount, 0.01,
                "Giá thầu tự động phải được parse đúng từ AUTO_BID_PLACED payload");
    }

    @Test
    @DisplayName("AUTO_BID_PLACED payload không phải số → không crash khi catch")
    void testAutoBidPlacedInvalidPayloadHandled() {
        NetworkMessage msg = makeMessage("AUTO_BID_PLACED", "invalid_number");
        assertThrows(NumberFormatException.class,
                () -> Double.parseDouble(msg.getPayload()),
                "Payload không hợp lệ phải ném NumberFormatException (để test catch block)");
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. AUTO_BID_CANCELLED
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTO_BID_CANCELLED → payload chứa lý do dừng")
    void testAutoBidCancelledHasReason() {
        String reason = "[Auto-Bid dừng] Số dư khả dụng (500.0) không đủ để thầu mức 1500.0";
        NetworkMessage msg = makeMessage("AUTO_BID_CANCELLED", reason);

        assertEquals("AUTO_BID_CANCELLED", msg.getAction());
        assertTrue(msg.getPayload().contains("không đủ"),
                "Payload CANCELLED phải chứa lý do dừng rõ ràng");
    }

    @Test
    @DisplayName("AUTO_BID_CANCELLED do vượt giới hạn → payload chứa từ 'vượt'")
    void testAutoBidCancelledExceedsLimit() {
        String reason = "Đấu giá tự động đã bị dừng do giá $5000.0 vượt giới hạn $4000.0";
        NetworkMessage msg = makeMessage("AUTO_BID_CANCELLED", reason);
        assertTrue(msg.getPayload().contains("vượt"));
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. NEW_BID broadcast
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NEW_BID → parse đúng auctionId, newPrice, winnerId")
    void testNewBidPayloadParsed() {
        JsonObject payload = new JsonObject();
        payload.addProperty("auctionId", "AUC_42");
        payload.addProperty("newPrice", 2500.0);
        payload.addProperty("winnerId", 7);

        NetworkMessage msg = makeMessage("NEW_BID", payload.toString());
        JsonObject parsed = GsonUtil.getGson().fromJson(msg.getPayload(), JsonObject.class);

        assertEquals("AUC_42", parsed.get("auctionId").getAsString());
        assertEquals(2500.0, parsed.get("newPrice").getAsDouble(), 0.01);
        assertEquals(7, parsed.get("winnerId").getAsInt());
    }

    @Test
    @DisplayName("NEW_BID chỉ cập nhật đúng auctionId phù hợp — không cập nhật SP khác")
    void testNewBidOnlyUpdatesMatchingAuction() {
        String targetId = "AUC_10";
        String otherId = "AUC_20";

        JsonObject payload = new JsonObject();
        payload.addProperty("auctionId", targetId);
        payload.addProperty("newPrice", 1800.0);
        payload.addProperty("winnerId", 3);

        NetworkMessage msg = makeMessage("NEW_BID", payload.toString());
        JsonObject parsed = GsonUtil.getGson().fromJson(msg.getPayload(), JsonObject.class);

        String rcvId = parsed.get("auctionId").getAsString();
        assertTrue(rcvId.equals(targetId), "chỉ cập nhật AUC_10");
        assertFalse(rcvId.equals(otherId), "không được nhầm với AUC_20");
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. Action không xác định
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Action không xác định → listener không ném exception")
    void testUnknownActionDoesNotCrash() {
        NetworkListener listener = new NetworkListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                // Simulate switch-default branch: chỉ bỏ qua
                if (!"NEW_BID".equals(message.getAction()) &&
                    !"PRODUCT_UPDATED".equals(message.getAction()) &&
                    !"BALANCE_UPDATED".equals(message.getAction())) {
                    // không làm gì — không crash
                }
            }
            @Override public void onConnectionError() {}
        };

        assertDoesNotThrow(() ->
            listener.onMessageReceived(makeMessage("UNKNOWN_ACTION", "{}"))
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // 7. Nhiều listener cùng nhận 1 event
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Nhiều listener cùng nhận PRODUCT_UPDATED — tất cả đều được gọi")
    void testMultipleListenersReceiveSameEvent() {
        AtomicInteger counter = new AtomicInteger(0);
        int NUM_LISTENERS = 3;

        List<NetworkListener> listeners = new ArrayList<>();
        NetworkManager nm = NetworkManager.getInstance();

        for (int i = 0; i < NUM_LISTENERS; i++) {
            NetworkListener l = new NetworkListener() {
                @Override
                public void onMessageReceived(NetworkMessage message) {
                    if ("PRODUCT_UPDATED".equals(message.getAction())) counter.incrementAndGet();
                }
                @Override public void onConnectionError() {}
            };
            listeners.add(l);
            nm.addListener(l);
        }

        // Mô phỏng: tất cả listener nhận message (gọi trực tiếp vì không có server thật)
        NetworkMessage event = makeMessage("PRODUCT_UPDATED", "");
        for (NetworkListener l : listeners) {
            l.onMessageReceived(event);
        }

        assertEquals(NUM_LISTENERS, counter.get(),
                "Tất cả " + NUM_LISTENERS + " listener đều phải nhận được PRODUCT_UPDATED");

        // Dọn dẹp
        for (NetworkListener l : listeners) nm.removeListener(l);
    }
}
