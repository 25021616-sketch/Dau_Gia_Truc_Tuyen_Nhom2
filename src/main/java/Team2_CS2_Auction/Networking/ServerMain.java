package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.util.DBConnection;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;

/**
 * Điểm khởi động của Máy Chủ.
 * ---------------------------------------------------------------
 * Chỉ cần chạy file này trên MỘT máy duy nhất (cùng WiFi với Client).
 * Máy Client chỉ cần chạy Main.java — hệ thống tự động kết nối.
 * ---------------------------------------------------------------
 * Cách chạy:
 *   mvn exec:java -Dexec.mainClass="Team2_CS2_Auction.Networking.ServerMain"
 */
public class ServerMain {

    public static void main(String[] args) {
        int port = 8080;

        System.out.println("=================================================");
        System.out.println("  HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - MÁY CHỦ");
        System.out.println("=================================================");

        // Lấy IP LAN thật của máy này
        String serverIp = getLanIp();
        System.out.println("  IP CỦA MÁY CHỦ (LAN): " + serverIp + ":" + port);

        // Lưu IP lên Database → Client sẽ tự đọc
        saveIpToDatabase(serverIp, port);

        System.out.println("  => Đã lưu địa chỉ vào Database.");
        System.out.println("  => Máy Client chỉ cần chạy Main.java là kết nối tự động.");
        System.out.println("=================================================");

        // Khởi động Discovery Server (UDP broadcast — fallback cho Client)
        new DiscoveryServer().start();

        // Khởi động TCP Server
        AuctionServer server = new AuctionServer();
        Thread serverThread = new Thread(() -> server.start(port));
        serverThread.setDaemon(true);
        serverThread.start();

        // Khởi động Scheduler tự động kết thúc phiên đấu giá
        new AuctionScheduler().start();

        System.out.println("[SERVER] Đang chờ Client kết nối tại cổng " + port + "...");
    }

    /**
     * Lấy IP LAN chính xác bằng UDP socket trick.
     * Phương pháp này hoạt động đúng trên Windows mà không cần
     * lọc thủ công tên card mạng ảo (VirtualBox, VMware, WSL...).
     */
    public static String getLanIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Chỉ "connect" để OS chọn interface phù hợp, không gửi dữ liệu thật
            socket.connect(InetAddress.getByName("8.8.8.8"), 80);
            String ip = socket.getLocalAddress().getHostAddress();
            if (ip != null && !ip.equals("0.0.0.0")) {
                return ip;
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    /**
     * Ghi IP và Port của Server lên bảng server_config trong MySQL.
     * Client đọc bảng này khi khởi động để tự động kết nối.
     */
    private static void saveIpToDatabase(String ip, int port) {
        try (Connection conn = DBConnection.getConnection()) {
            // Tạo bảng nếu chưa có
            conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS server_config (" +
                "  id INT PRIMARY KEY," +
                "  ip_address VARCHAR(255) NOT NULL," +
                "  port INT NOT NULL," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            ).executeUpdate();

            // Ghi IP (upsert — ghi đè nếu đã có)
            conn.prepareStatement(
                "INSERT INTO server_config (id, ip_address, port) VALUES (1,'" + ip + "'," + port + ") " +
                "ON DUPLICATE KEY UPDATE ip_address='" + ip + "', port=" + port
            ).executeUpdate();

            System.out.println("[DB] ✅ Đã lưu: " + ip + ":" + port);
        } catch (Exception e) {
            System.err.println("[DB] ❌ Lỗi lưu IP Server: " + e.getMessage());
        }
    }
}
