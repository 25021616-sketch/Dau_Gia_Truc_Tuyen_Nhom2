package Team2_CS2_Auction.Networking;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
public class TestWS {
    public static void main(String[] args) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI wsUri = URI.create("ws://127.0.0.1:8080/ws/auction");
            WebSocket ws = client.newWebSocketBuilder().buildAsync(wsUri, new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    System.out.println("OPEN!");
                }
                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    System.out.println("ERR: " + error);
                }
            }).join();
            System.out.println("Success");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
