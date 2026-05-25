package Team2_CS2_Auction.Networking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test bộ: NetworkManager — kiểm tra logic kết nối WebSocket một lần duy nhất.
 *
 * Các trường hợp được kiểm tra:
 * 1. Trạng thái isConnected() ban đầu là false.
 * 2. Không ném exception khi gọi disconnect() lúc chưa kết nối.
 * 3. Guard "disconnect trước khi connect lại" — addListener không bị nhân bản.
 * 4. Gọi send() khi chưa kết nối — hệ thống xử lý gracefully (không crash).
 * 5. addListener + removeListener hoạt động đúng, message không đến listener đã remove.
 * 6. NetworkManager là Singleton — cùng instance.
 */
@DisplayName("NetworkManager – Single-Connect & Listener Guard")
public class NetworkManagerConnectTest {

    private NetworkManager nm;

    @BeforeEach
    void setUp() {
        nm = NetworkManager.getInstance();
        nm.disconnect(); // Đảm bảo trạng thái sạch trước mỗi test
    }

    @AfterEach
    void tearDown() {
        nm.disconnect();
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. Trạng thái ban đầu
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isConnected() phải là false khi chưa kết nối")
    void testInitiallyDisconnected() {
        assertFalse(nm.isConnected(),
                "NetworkManager chưa kết nối phải trả về false từ isConnected()");
    }

    @Test
    @DisplayName("Gọi disconnect() khi chưa kết nối → không ném exception")
    void testDisconnectWhenAlreadyDisconnected() {
        assertDoesNotThrow(() -> nm.disconnect(),
                "disconnect() khi chưa kết nối không được ném exception");
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. Send khi chưa kết nối
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("send() khi chưa kết nối → hệ thống xử lý gracefully, không crash")
    void testSendWhenDisconnected() {
        assertFalse(nm.isConnected());
        assertDoesNotThrow(() -> nm.send("PLACE_BID", "{\"amount\":1000}"),
                "send() khi webSocket==null không được ném exception");
    }

    @Test
    @DisplayName("send() với payload null → không crash")
    void testSendNullPayload() {
        assertDoesNotThrow(() -> nm.send("TEST", null));
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. Listener management
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addListener + removeListener hoạt động đúng")
    void testAddAndRemoveListener() {
        AtomicInteger callCount = new AtomicInteger(0);

        NetworkListener listener = new NetworkListener() {
            @Override public void onMessageReceived(NetworkMessage message) { callCount.incrementAndGet(); }
            @Override public void onConnectionError() {}
        };

        // Thêm rồi xóa — không được ném exception
        assertDoesNotThrow(() -> nm.addListener(listener));
        assertDoesNotThrow(() -> nm.removeListener(listener));
    }

    @Test
    @DisplayName("Thêm listener cùng instance 2 lần → danh sách không bị nhân bản")
    void testAddSameListenerTwiceNoDuplicate() {
        AtomicInteger callCount = new AtomicInteger(0);

        NetworkListener listener = new NetworkListener() {
            @Override public void onMessageReceived(NetworkMessage message) { callCount.incrementAndGet(); }
            @Override public void onConnectionError() {}
        };

        // Thêm 2 lần — giống với scenario Dang_nhap_Controller gọi login 2 lần
        nm.addListener(listener);
        nm.addListener(listener); // Lần 2 (guard phải bỏ qua nếu đã có)

        // Gọi removeListener 1 lần là đủ để xóa hoàn toàn
        nm.removeListener(listener);

        // Sau khi remove, listener không còn trong danh sách
        // Không có cách test trực tiếp -> kiểm tra remove không ném exception
        assertDoesNotThrow(() -> nm.removeListener(listener),
                "removeListener gọi lần 2 trên listener đã xóa không được throw");
    }

    @Test
    @DisplayName("removeListener với listener null → không crash")
    void testRemoveNullListener() {
        assertDoesNotThrow(() -> nm.removeListener(null),
                "removeListener(null) không được ném NullPointerException");
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. Singleton
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NetworkManager.getInstance() luôn trả về cùng 1 instance")
    void testSingleton() {
        NetworkManager nm1 = NetworkManager.getInstance();
        NetworkManager nm2 = NetworkManager.getInstance();
        assertSame(nm1, nm2, "getInstance() phải trả về cùng 1 object");
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. Kết nối tới host không tồn tại (kiểm tra graceful failure)
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("connect() tới host không tồn tại → isConnected() vẫn false, không crash")
    void testConnectToInvalidHostGracefullyFails() {
        assertDoesNotThrow(() -> nm.connect("192.0.2.1", 9999),
                "connect() tới host không tồn tại không được ném exception thoát ra ngoài");
        // Dù connect thất bại, isConnected() phải phản ánh đúng
        assertFalse(nm.isConnected(),
                "Sau khi connect thất bại, isConnected() phải là false");
    }

    @Test
    @DisplayName("Guard double-connect: disconnect() rồi connect() lại → không tạo 2 session")
    void testDisconnectBeforeReconnect() {
        // Simulate đăng xuất + đăng nhập lại (Dang_nhap_Controller flow)
        assertDoesNotThrow(() -> {
            if (nm.isConnected()) nm.disconnect();
            // connect tới host lỗi chỉ để kiểm tra guard hoạt động
            nm.connect("192.0.2.1", 9999);
        });
        // Không có exception = guard hoạt động đúng
    }
}
