package Team2_CS2_Auction.Networking;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryServer implements Runnable {
    private DatagramSocket socket;
    private boolean isRunning;

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            // Lắng nghe trên cổng 8888 cho gói tin UDP
            socket = new DatagramSocket(8888);
            socket.setBroadcast(true);
            System.out.println("UDP Discovery Server đang chạy trên cổng 8888...");

            while (isRunning) {
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                
                if (message.equals("DISCOVER_AUCTION_SERVER_REQUEST")) {
                    System.out.println("Nhận được yêu cầu tìm Server từ: " + packet.getAddress().getHostAddress());
                    
                    // Phản hồi lại Client
                    byte[] sendData = "DISCOVER_AUCTION_SERVER_RESPONSE".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                }
            }
        } catch (Exception ex) {
            if (isRunning) {
                System.out.println("Lỗi DiscoveryServer: " + ex.getMessage());
            }
        } finally {
            stop();
        }
    }
}
