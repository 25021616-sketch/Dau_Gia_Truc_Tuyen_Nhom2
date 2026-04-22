module Team2_CS2_Auction {
    requires javafx.controls;
    requires javafx.fxml;
    // ... các requires khác nếu có ...

    // Mở và xuất package Controller thực tế của bạn
    opens Team2_CS2_Auction.Controller to javafx.fxml;
    exports Team2_CS2_Auction;
    exports Team2_CS2_Auction.Controller;
}