package Team2_CS2_Auction.Repository;

import Team2_CS2_Auction.Model.item.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AuctionData {
    // Dùng ObservableList để TableView tự động cập nhật khi thêm phần tử
    public static final ObservableList<Item> listSanPham = FXCollections.observableArrayList();
}