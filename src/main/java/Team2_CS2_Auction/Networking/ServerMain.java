package Team2_CS2_Auction.Networking;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;
        AuctionServer server = new AuctionServer();
        server.start(port);
    }
}
