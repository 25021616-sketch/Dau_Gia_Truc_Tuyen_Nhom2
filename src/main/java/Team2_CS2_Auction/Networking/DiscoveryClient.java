package Team2_CS2_Auction.Networking;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * UDP Discovery Client - chạy ở máy Client khi khởi động.
 * Tự động broadcast để tìm IP của Server trong mạng LAN.
 * Trả về null nếu không tìm thấy (client sẽ fallback nhập tay).
 */
public class DiscoveryClient {

    private static final int DISCOVERY_PORT = 8888;
    private static final String REQUEST_MSG  = "DISCOVER_AUCTION_SERVER_REQUEST";
    private static final String RESPONSE_PREFIX = "DISCOVER_AUCTION_SERVER_RESPONSE:";
    private static final int TIMEOUT_MS = 4000; // Đợi tối đa 4 giây mỗi lần
    private static final int MAX_ATTEMPTS = 2;  // Thử tối đa 2 lần

    public static String discoverServerIp() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.println("[Discovery] Lần thử " + attempt + "/" + MAX_ATTEMPTS + " tìm Server...");
            String result = tryDiscover();
            if (result != null) return result;
        }
        return null; // Không tìm thấy
    }

    private static String tryDiscover() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            byte[] sendData = REQUEST_MSG.getBytes();

            // 1. Broadcast tới 255.255.255.255 (toàn mạng)
            sendBroadcast(socket, sendData, "255.255.255.255");
            
            // 1.5. Gửi trực tiếp tới 127.0.0.1 để đảm bảo chạy trên cùng 1 máy tính luôn tìm thấy nhau
            sendBroadcast(socket, sendData, "127.0.0.1");

            // 2. Broadcast tới tất cả broadcast address của các interface
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces != null && ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                for (InterfaceAddress ifAddr : iface.getInterfaceAddresses()) {
                    InetAddress broadcast = ifAddr.getBroadcast();
                    if (broadcast != null) {
                        sendBroadcast(socket, sendData, broadcast.getHostAddress());
                    }
                }
            }

            // 3. Lắng nghe phản hồi
            byte[] recvBuf = new byte[256];
            DatagramPacket response = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(response);

            String msg = new String(response.getData(), 0, response.getLength()).trim();
            if (msg.startsWith(RESPONSE_PREFIX)) {
                String ip = msg.substring(RESPONSE_PREFIX.length()).trim();
                System.out.println("[Discovery] ✅ Tìm thấy Server tại: " + ip);
                return ip;
            }

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("[Discovery] Hết thời gian chờ lần này.");
        } catch (Exception e) {
            System.out.println("[Discovery] Lỗi khi tìm Server: " + e.getMessage());
        }
        return null;
    }

    private static void sendBroadcast(DatagramSocket socket, byte[] data, String broadcastAddr) {
        try {
            InetAddress addr = InetAddress.getByName(broadcastAddr);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, DISCOVERY_PORT);
            socket.send(packet);
        } catch (Exception ignored) {}
    }
}
