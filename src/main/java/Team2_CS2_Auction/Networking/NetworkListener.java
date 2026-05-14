package Team2_CS2_Auction.Networking;

public interface NetworkListener {
    /**
     * Called when a message is received from the server.
     * @param message The deserialized NetworkMessage object.
     */
    void onMessageReceived(NetworkMessage message);
    
    /**
     * Called when the connection to the server is lost or fails.
     */
    void onConnectionError();
}
