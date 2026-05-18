package Team2_CS2_Auction.Networking;

public class ServerMain {
    public static void main(String[] args) {
        int tcpPort = 8080;

        // 1. Khởi động UDP Discovery để Client tự tìm IP
        DiscoveryServer discoveryServer = new DiscoveryServer();
        discoveryServer.start(); // In IP LAN ra Terminal, lắng nghe yêu cầu tìm Server

        // 2. Khởi động TCP Auction Server (chấp nhận đặt giá)
        AuctionServer server = new AuctionServer();
        server.start(tcpPort);
    }
}
