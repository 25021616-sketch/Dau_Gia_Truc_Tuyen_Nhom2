package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Repository.ServerConfigRepository;
import Team2_CS2_Auction.util.DBConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Điểm khởi động của Máy Chủ.
 * Chỉ cần chạy file này trên MỘT máy duy nhất.
 * Các máy khác chạy Main.java và nhập IP của máy này để kết nối.
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;

        System.out.println("=================================================");
        System.out.println("  HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - MÁY CHỦ");
        System.out.println("=================================================");

        // Đảm bảo Database đã được chuẩn bị
        DBConnection.runFlywayMigration();

        // Thử lấy IP Ngrok trước, nếu không có thì lấy IP LAN
        String serverIp = getNgrokIp();
        if (serverIp != null && !serverIp.isEmpty()) {
            System.out.println("  [AUTO-DETECT] TÌM THẤY NGROK ĐANG CHẠY!");
            System.out.println("  NGROK PUBLIC IP : " + serverIp);
        } else {
            serverIp = getServerIp();
            System.out.println("  IP LAN CỦA MÁY  : " + serverIp);
        }
        
        // Ghi lên Database để Client tự động tìm được
        ServerConfigRepository.saveServerConfig(serverIp, port);

        System.out.println("  => Các máy Client sẽ tự động lấy IP này từ DB.");
        System.out.println("=================================================");

        AuctionServer server = new AuctionServer();

        // ✅ FIX: Chạy server trên Thread riêng để không block luồng chính
        // Nếu để server.start(port) chạy trực tiếp, nó sẽ block mãi mãi
        // khiến scheduler.start() phía dưới KHÔNG BAO GIỜ được thực thi.
        Thread serverThread = new Thread(() -> server.start(port));
        serverThread.setDaemon(true);
        serverThread.start();

        AuctionScheduler scheduler = new AuctionScheduler();
        scheduler.start();

        System.out.println("[SCHEDULER] Auto-finish scheduler đã khởi động.");
    }

    /** Lấy địa chỉ IPv4 LAN thực của máy (bỏ qua loopback, mạng ảo) */
    private static String getServerIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> ifaces =
                    java.net.NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                java.net.NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;
                String name = iface.getName().toLowerCase();
                if (name.contains("vbox") || name.contains("vmware") || name.contains("wsl")) continue;

                java.util.Enumeration<java.net.InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1 (Không tìm được IP LAN - kiểm tra lại mạng)";
    }

    /**
     * Tự động lấy Public IP của Ngrok nếu đang chạy.
     */
    private static String getNgrokIp() {
        try {
            URL url = new URL("http://127.0.0.1:4040/api/tunnels");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
                JsonArray tunnels = json.getAsJsonArray("tunnels");
                if (tunnels.size() > 0) {
                    // Ưu tiên tunnel kiểu tcp
                    for (int i = 0; i < tunnels.size(); i++) {
                        JsonObject tunnel = tunnels.get(i).getAsJsonObject();
                        String proto = tunnel.get("proto").getAsString();
                        if ("tcp".equalsIgnoreCase(proto)) {
                            String publicUrl = tunnel.get("public_url").getAsString();
                            return publicUrl.replace("tcp://", "");
                        }
                    }
                    // Nếu không có tcp cụ thể, lấy cái đầu tiên
                    String publicUrl = tunnels.get(0).getAsJsonObject().get("public_url").getAsString();
                    return publicUrl.replace("tcp://", "").replace("http://", "").replace("https://", "");
                }
            }
        } catch (Exception e) {
            // Bỏ qua nếu Ngrok không chạy
        }
        return null;
    }
}
