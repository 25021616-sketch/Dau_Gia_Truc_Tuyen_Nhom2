package Team2_CS2_Auction.Repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Team2_CS2_Auction.Model.item.Item;

public class AuctionData {
    // Dùng ObservableList để TableView tự động cập nhật khi thêm phần tử
    public static final ObservableList<Item> listSanPham = FXCollections.observableArrayList();
}