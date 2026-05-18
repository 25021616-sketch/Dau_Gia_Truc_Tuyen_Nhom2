package Team2_CS2_Auction.Networking;

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
        System.out.println("  IP LAN CỦA MÁY NÀY: " + getServerIp());
        System.out.println("  => Các máy Client sẽ tự động kết nối qua IP LAN này.");
        System.out.println("=================================================");

        // 1. Chạy Flyway Migration để chuẩn hóa cơ sở dữ liệu
        Team2_CS2_Auction.util.DBConnection.runFlywayMigration();

        // 2. Khởi động UDP Discovery Server giúp các máy khách tìm thấy tự động
        System.out.println("[Discovery] Đang khởi động UDP Discovery Server...");
        DiscoveryServer discoveryServer = new DiscoveryServer();
        discoveryServer.start();

        // 3. Khởi động TCP Auction Server
        AuctionServer server = new AuctionServer();
        server.start(port);
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
}
