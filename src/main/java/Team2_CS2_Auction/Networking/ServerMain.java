package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Điểm khởi động của Máy Chủ.
 * Chỉ cần chạy file này trên MỘT máy duy nhất.
 * Máy Client chỉ cần chạy Main.java — sẽ tự động kết nối qua Database.
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;

        System.out.println("=================================================");
        System.out.println("  HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - MÁY CHỦ");
        System.out.println("=================================================");

        // 1. Lấy IP thực của máy (ưu tiên Ngrok nếu đang chạy)
        String serverIp = getNgrokPublicAddress();
        if (serverIp != null) {
            System.out.println("  [NGROK] Địa chỉ Ngrok    : " + serverIp);
        } else {
            serverIp = getLanIp();
            System.out.println("  [LAN]   IP nội bộ        : " + serverIp);
        }

        // 2. Lưu IP lên Database → Client sẽ tự đọc
        saveIpToDatabase(serverIp, port);
        System.out.println("  => Đã lưu địa chỉ vào Database.");
        System.out.println("  => Máy Client chỉ cần chạy Main.java là kết nối tự động.");
        System.out.println("=================================================");

        // 3. Khởi động Server TCP
        AuctionServer server = new AuctionServer();
        Thread serverThread = new Thread(() -> server.start(port));
        serverThread.setDaemon(true);
        serverThread.start();

        // 4. Khởi động Scheduler tự động kết thúc phiên
        AuctionScheduler scheduler = new AuctionScheduler();
        scheduler.start();
        System.out.println("[SCHEDULER] Auto-finish scheduler đã khởi động.");
    }

    // ===========================================================
    // LẤY ĐỊA CHỈ NGROK (nếu đang chạy ngrok trên máy này)
    // ===========================================================
    private static String getNgrokPublicAddress() {
        try {
            java.net.URL url = new java.net.URL("http://127.0.0.1:4040/api/tunnels");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return null;

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            String json = sb.toString();

            // Tìm tcp tunnel: "public_url":"tcp://X.tcp.ngrok.io:PORT"
            int tcpIdx = json.indexOf("\"tcp://");
            if (tcpIdx == -1) tcpIdx = json.indexOf("\"tls://");
            if (tcpIdx != -1) {
                int start = tcpIdx + 1; // bỏ dấu "
                int end = json.indexOf("\"", start);
                String rawUrl = json.substring(start, end); // tcp://X.tcp.ngrok.io:PORT
                return rawUrl.replace("tcp://", "").replace("tls://", "");
            }
        } catch (Exception ignored) {
            // Ngrok chưa chạy → bỏ qua
        }
        return null;
    }

    // ===========================================================
    // LẤY IP LAN NỘI BỘ
    // ===========================================================
    private static String getLanIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> ifaces =
                    java.net.NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                java.net.NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;
                String name = iface.getName().toLowerCase();
                if (name.contains("vbox") || name.contains("vmware") ||
                    name.contains("wsl") || name.contains("docker")) continue;

                java.util.Enumeration<java.net.InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    // ===========================================================
    // GHI IP + PORT LÊN DATABASE
    // ===========================================================
    private static void saveIpToDatabase(String ip, int port) {
        try (Connection conn = DBConnection.getConnection()) {
            // Tạo bảng nếu chưa có
            conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS server_config (" +
                "  id INT PRIMARY KEY, " +
                "  ip_address VARCHAR(255) NOT NULL, " +
                "  port INT NOT NULL, " +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            ).executeUpdate();

            // Upsert dòng id=1
            conn.prepareStatement(
                "INSERT INTO server_config (id, ip_address, port) VALUES (1, '" + ip + "', " + port + ") " +
                "ON DUPLICATE KEY UPDATE ip_address = '" + ip + "', port = " + port
            ).executeUpdate();

            System.out.println("[DB] ✅ Đã lưu: " + ip + ":" + port);
        } catch (Exception e) {
            System.err.println("[DB] ❌ Lỗi lưu IP Server: " + e.getMessage());
        }
    }
}
