package Team2_CS2_Auction.Networking;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;
        
        // Chạy dịch vụ tự động phát IP
        DiscoveryServer discoveryServer = new DiscoveryServer();
        discoveryServer.start();
        
        AuctionServer server = new AuctionServer();
        server.start(port);
    }
}
