package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Item_Card_Controller;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Session.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.LocalDateTime;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class ItemCardUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Session.currentUser = new Member(999, "testBidder", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
        Parent root = fxmlLoader.load();
        
        Item_Card_Controller controller = fxmlLoader.getController();
        
        // Tạo Auction giả lập
        Member seller = new Member(888, "seller", "1", "1");
        Item item = new Team2_CS2_Auction.Model.item.Other("ITM1", "Test Item Điện Thoại", "Điện thoại", "Desc", null);
        item.setGiaKhoiDiem(100);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        Auction auction = new Auction("AUC1", item, seller, 100.0, 10.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        
        // Đặt dữ liệu vào Card
        controller.setData(auction);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testItemCardDisplaysCorrectData() {
        // Kiểm tra xem tên sản phẩm hiển thị đúng không
        verifyThat("#lblTenSP", hasText("Test Item Điện Thoại"));
        // Kiểm tra xem nút Đặt Giá có hiển thị không
        verifyThat("ĐẶT GIÁ", isVisible());
    }
}
