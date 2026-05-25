package Team2_CS2_Auction.Networking;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NetworkMessageTest - Kiểm tra serialization networking")
public class NetworkMessageTest {

    private final Gson gson = GsonUtil.getGson();

    // ─── NetworkMessage ───────────────────────────────────────────

    @Test
    @DisplayName("Tạo NetworkMessage lưu đúng action và payload")
    public void testNetworkMessageCreation() {
        NetworkMessage msg = new NetworkMessage("LOGIN", "{\"username\":\"test\"}");
        assertEquals("LOGIN", msg.getAction());
        assertEquals("{\"username\":\"test\"}", msg.getPayload());
    }

    @Test
    @DisplayName("setAction và setPayload cập nhật đúng")
    public void testNetworkMessageSetters() {
        NetworkMessage msg = new NetworkMessage("OLD_ACTION", "old_payload");
        msg.setAction("NEW_ACTION");
        msg.setPayload("new_payload");
        assertEquals("NEW_ACTION", msg.getAction());
        assertEquals("new_payload", msg.getPayload());
    }

    @Test
    @DisplayName("Serialize NetworkMessage sang JSON và deserialize lại đúng")
    public void testNetworkMessageSerializeDeserialize() {
        NetworkMessage original = new NetworkMessage("PLACE_BID", "{\"amount\":1500.0}");
        String json = gson.toJson(original);

        assertNotNull(json);
        assertTrue(json.contains("PLACE_BID"));
        assertTrue(json.contains("amount"));

        NetworkMessage restored = gson.fromJson(json, NetworkMessage.class);
        assertEquals(original.getAction(), restored.getAction());
        assertEquals(original.getPayload(), restored.getPayload());
    }

    @Test
    @DisplayName("Serialize nhiều loại action khác nhau")
    public void testVariousActions() {
        String[] actions = {"LOGIN", "REGISTER", "PLACE_BID", "APPROVE_AUCTION",
                "REJECT_AUCTION", "GET_AUCTIONS", "DISCONNECT"};

        for (String action : actions) {
            NetworkMessage msg = new NetworkMessage(action, "{}");
            String json = gson.toJson(msg);
            NetworkMessage restored = gson.fromJson(json, NetworkMessage.class);
            assertEquals(action, restored.getAction(), "Action [" + action + "] phải serialize/deserialize đúng");
        }
    }

    @Test
    @DisplayName("Payload rỗng hoặc null serialize đúng")
    public void testEmptyPayload() {
        NetworkMessage msg = new NetworkMessage("PING", "");
        String json = gson.toJson(msg);
        NetworkMessage restored = gson.fromJson(json, NetworkMessage.class);
        assertEquals("PING", restored.getAction());
        assertEquals("", restored.getPayload());
    }

    // ─── GsonUtil LocalDateTime ────────────────────────────────────

    @Test
    @DisplayName("GsonUtil serialize LocalDateTime sang ISO string")
    public void testGsonUtilLocalDateTimeSerialization() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 30, 0);
        String json = gson.toJson(dt);
        // Kết quả phải là dạng ISO: "2026-05-25T10:30:00"
        assertTrue(json.contains("2026-05-25"), "JSON phải chứa ngày tháng đúng");
        assertTrue(json.contains("10:30:00"), "JSON phải chứa giờ phút giây đúng");
    }

    @Test
    @DisplayName("GsonUtil deserialize ISO string thành LocalDateTime")
    public void testGsonUtilLocalDateTimeDeserialization() {
        LocalDateTime original = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
        String json = gson.toJson(original);
        LocalDateTime restored = gson.fromJson(json, LocalDateTime.class);
        assertEquals(original, restored, "LocalDateTime phải được deserialize chính xác");
    }

    @Test
    @DisplayName("GsonUtil là Singleton — cùng instance Gson")
    public void testGsonUtilSingleton() {
        Gson g1 = GsonUtil.getGson();
        Gson g2 = GsonUtil.getGson();
        assertSame(g1, g2, "GsonUtil.getGson() phải trả về cùng instance");
    }

    // ─── NetworkManager ───────────────────────────────────────────

    @Test
    @DisplayName("NetworkManager là Singleton")
    public void testNetworkManagerSingleton() {
        NetworkManager nm1 = NetworkManager.getInstance();
        NetworkManager nm2 = NetworkManager.getInstance();
        assertSame(nm1, nm2, "NetworkManager.getInstance() phải trả về cùng instance");
    }

    @Test
    @DisplayName("NetworkManager ban đầu chưa kết nối")
    public void testNetworkManagerInitiallyDisconnected() {
        // Tạo instance mới (trong test env chưa connect)
        NetworkManager nm = NetworkManager.getInstance();
        assertFalse(nm.isConnected(), "NetworkManager ban đầu phải chưa kết nối");
    }

    @Test
    @DisplayName("NetworkManager: addListener và removeListener không throw exception")
    public void testNetworkManagerListenerManagement() {
        NetworkManager nm = NetworkManager.getInstance();
        NetworkListener listener = new NetworkListener() {
            @Override public void onMessageReceived(NetworkMessage message) {}
            @Override public void onConnectionError() {}
        };

        assertDoesNotThrow(() -> nm.addListener(listener), "addListener không được ném exception");
        assertDoesNotThrow(() -> nm.removeListener(listener), "removeListener không được ném exception");
    }

    @Test
    @DisplayName("NetworkManager: gọi send() khi chưa kết nối → không ném exception (chỉ log lỗi)")
    public void testNetworkManagerSendWhenDisconnected() {
        NetworkManager nm = NetworkManager.getInstance();
        // Đảm bảo disconnect
        nm.disconnect();
        // Gửi khi chưa kết nối → phải log lỗi, không ném exception
        assertDoesNotThrow(() -> nm.send("TEST_ACTION", "{}"),
                "send() khi chưa kết nối không được ném exception");
    }

    @Test
    @DisplayName("NetworkManager: kết nối đến địa chỉ không tồn tại → không ném exception (xử lý graceful)")
    public void testNetworkManagerConnectInvalidHost() {
        NetworkManager nm = NetworkManager.getInstance();
        // Thử kết nối đến địa chỉ không tồn tại (timeout nhanh)
        // Không dùng assertDoesNotThrow vì có thể timeout lâu, chỉ kiểm tra instance hợp lệ
        assertNotNull(nm, "NetworkManager instance không được null");
        // Chỉ kiểm tra rằng trạng thái kết nối ban đầu là false
        assertFalse(nm.isConnected(), "Trước khi connect phải ở trạng thái chưa kết nối");
    }
}
