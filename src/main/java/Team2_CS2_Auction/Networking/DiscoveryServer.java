package Team2_CS2_Auction.Networking;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * UDP Discovery Server - chạy ngầm ở máy chủ.
 * Khi client broadcast hỏi "Server ở đâu?", lớp này phản hồi IP LAN của máy chủ.
 */
public class DiscoveryServer implements Runnable {
    private static final int DISCOVERY_PORT = 8888;
    private static final String REQUEST_MSG  = "DISCOVER_AUCTION_SERVER_REQUEST";
    private static final String RESPONSE_MSG = "DISCOVER_AUCTION_SERVER_RESPONSE";

    private DatagramSocket socket;
    private boolean isRunning;

    public void start() {
        isRunning = true;
        String ip = getLanIp();
        System.out.println("=================================================");
        System.out.println("  IP CỦA MÁY CHỦ (LAN): " + ip);
        System.out.println("  Các máy Client sẽ tự tìm IP này qua UDP.");
        System.out.println("  Nếu không tự tìm được, nhập tay: " + ip);
        System.out.println("=================================================");
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        // Thử bind cổng 8888, nếu bị chiếm thì đợi và thử lại tối đa 5 lần
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);  // Cho phép bind lại cổng ngay sau khi restart
                socket.bind(new java.net.InetSocketAddress(DISCOVERY_PORT));
                socket.setBroadcast(true);
                break; // Bind thành công, thoát vòng lặp retry
            } catch (Exception e) {
                System.err.println("[Discovery] Cổng " + DISCOVERY_PORT + " đang bị chiếm, thử lại lần " + attempt + "/" + maxRetries + "...");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                if (attempt == maxRetries) {
                    System.err.println("[Discovery] ❌ Không thể khởi động Discovery Server sau " + maxRetries + " lần thử. Client sẽ cần nhập IP thủ công.");
                    return;
                }
            }
        }

        try {
            System.out.println("[Discovery] ✅ Đang lắng nghe yêu cầu tìm Server trên cổng " + DISCOVERY_PORT + "...");

            while (isRunning) {
                byte[] buf = new byte[256];
                DatagramPacket request = new DatagramPacket(buf, buf.length);
                socket.receive(request);

                String msg = new String(request.getData(), 0, request.getLength()).trim();
                if (!REQUEST_MSG.equals(msg)) continue;

                System.out.println("[Discovery] Client " + request.getAddress().getHostAddress() + " đang tìm Server...");

                // Phản hồi IP LAN thực của Server
                String response = RESPONSE_MSG + ":" + getLanIp();
                byte[] respData = response.getBytes();
                DatagramPacket reply = new DatagramPacket(
                        respData, respData.length, request.getAddress(), request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            if (isRunning) {
                System.err.println("[Discovery] Lỗi: " + e.getMessage());
            }
        } finally {
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    /** Lấy IP LAN chính xác bằng UDP socket trick (hoạt động tốt trên Windows) */
    public static String getLanIp() {
        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
            socket.connect(java.net.InetAddress.getByName("8.8.8.8"), 80);
            String ip = socket.getLocalAddress().getHostAddress();
            if (ip != null && !ip.equals("0.0.0.0")) return ip;
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}
