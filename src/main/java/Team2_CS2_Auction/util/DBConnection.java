package Team2_CS2_Auction.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // Railway MySQL
    private static final String URL =
            "jdbc:mysql://turntable.proxy.rlwy.net:27416/railway" +
                    "?useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    private static final String PASSWORD =
            "wRUFbXdBBbdfWquOSqETKpVqzRzEYjEr";

    public static Connection getConnection() throws Exception {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}