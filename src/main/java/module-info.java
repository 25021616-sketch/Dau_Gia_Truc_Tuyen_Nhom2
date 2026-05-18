module Team2_CS2_Auction {
    // 1. Khai báo các module JavaFX cần thiết
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;
    requires java.sql;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;
    requires org.flywaydb.core;

    // 2. Cho phép JavaFX (FXML) truy cập vào Controller để bắt sự kiện (Button click,...)
    opens Team2_CS2_Auction.Controller to javafx.fxml;

    // 3. QUAN TRỌNG: Cho phép JavaFX (Base) truy cập Model để hiển thị dữ liệu lên TableView
    // Đây chính là dòng giải quyết lỗi "IllegalAccessException" bạn gặp phải
    opens Team2_CS2_Auction.Model.item to javafx.base;

    // 4. Xuất các package để các module khác hoặc JavaFX runtime có thể nhìn thấy
    exports Team2_CS2_Auction;
    exports Team2_CS2_Auction.Controller;
    exports Team2_CS2_Auction.Model.item;
    exports Team2_CS2_Auction.Service;
    
    opens Team2_CS2_Auction.Service to javafx.fxml, com.google.gson;
    opens Team2_CS2_Auction.Model.user to com.google.gson, javafx.base;
    opens Team2_CS2_Auction.Model.auction to com.google.gson, javafx.base;
    opens Team2_CS2_Auction.Networking to com.google.gson;
    opens db.migration to org.flywaydb.core;
}