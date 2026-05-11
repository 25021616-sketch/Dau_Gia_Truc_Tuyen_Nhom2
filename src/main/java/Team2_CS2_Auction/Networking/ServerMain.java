package Team2_CS2_Auction.Networking;

import Team2_CS2_Auction.Networking.AuctionServer;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import Team2_CS2_Auction.Repository.UserRepository;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Service.UserService;
import Team2_CS2_Auction.util.DBConnection;

import java.sql.Connection;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("========== KHỞI ĐỘNG HỆ THỐNG BACKEND SERVER ==========");

        // 1. Kết nối Database (Dùng class DBConnection có sẵn của bạn)
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("[LỖI] Không thể kết nối Database Railway. Vui lòng kiểm tra Internet!");
            return;
        }
        System.out.println("[DB] Kết nối Database thành công.");

        // 2. Khởi tạo các tầng Repository
        UserRepository userRepo = new UserRepository();
        AuctionRepositoryImpl auctionRepo = new AuctionRepositoryImpl();

        // 3. Khởi tạo các tầng Service và truyền Repo vào
        UserService userService = new UserService(userRepo);
        AuctionServiceImpl auctionService = new AuctionServiceImpl(auctionRepo);

        // 4. Khởi tạo và chạy AuctionServer tại cổng 8080
        int port = 8080;
        AuctionServer server = new AuctionServer(port, auctionService, userService);

        // Bắt đầu lắng nghe kết nối
        server.start();
    }
}