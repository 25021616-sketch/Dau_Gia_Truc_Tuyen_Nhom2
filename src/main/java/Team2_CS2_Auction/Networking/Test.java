package Team2_CS2_Auction.Networking;
import Team2_CS2_Auction.Networking.NetworkManager;
public class Test {
    public static void main(String[] args) throws Exception {
        NetworkManager nm = NetworkManager.getInstance();
        System.out.println("Connect 1");
        nm.connect("127.0.0.1", 8080);
        System.out.println("Connect 2");
        nm.connect("127.0.0.1", 8080);
        System.out.println("Done");
    }
}
