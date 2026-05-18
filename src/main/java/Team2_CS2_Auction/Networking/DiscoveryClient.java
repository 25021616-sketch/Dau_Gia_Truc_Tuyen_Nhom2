package Team2_CS2_Auction.Networking;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DiscoveryClient {
    public static String discoverServerIp() {
        DatagramSocket c = null;
        try {
            c = new DatagramSocket();
            c.setBroadcast(true);
            c.setSoTimeout(3000); // Đợi tối đa 3 giây

            byte[] sendData = "DISCOVER_AUCTION_SERVER_REQUEST".getBytes();

            // Thử gửi broadcast tới địa chỉ 255.255.255.255
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                c.send(sendPacket);
            } catch (Exception e) {}

            // Thử gửi broadcast tới tất cả các network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {}
                }
            }

            // Lắng nghe phản hồi
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket); // Chờ phản hồi từ server

            String message = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            if (message.equals("DISCOVER_AUCTION_SERVER_RESPONSE")) {
                return receivePacket.getAddress().getHostAddress();
            }

        } catch (Exception ex) {
            System.out.println("Không tìm thấy Server tự động: " + ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }
}
