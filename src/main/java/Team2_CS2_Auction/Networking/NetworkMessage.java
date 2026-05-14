package Team2_CS2_Auction.Networking;

public class NetworkMessage {
    private String action;
    private String payload;

    public NetworkMessage(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
