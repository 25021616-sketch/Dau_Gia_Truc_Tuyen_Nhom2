package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Phien_Dau_Gia_Controller;
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

@ExtendWith(ApplicationExtension.class)
public class AuctionSessionUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Cần giả lập Session có user đăng nhập
        Session.currentUser = new Member(999, "testBidder", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/phien_dau_gia.fxml"));
        Parent root = fxmlLoader.load();
        
        Phien_Dau_Gia_Controller controller = fxmlLoader.getController();
        
        // Tạo một Auction giả để test UI
        Member seller = new Member(888, "seller", "1", "1");
        Item item = new Team2_CS2_Auction.Model.item.Other("ITM1", "Test Item", "Test Category", "Desc", null);
        item.setGiaKhoiDiem(100);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        Auction auction = new Auction("AUC1", item, seller, 100.0, 10.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        
        try {
            controller.setAuctionData(auction);
        } catch(Exception e) {} // Bỏ qua lỗi kết nối Socket hoặc DB nếu có

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testPlaceBidButtonExists() {
        // Kiểm tra nút Đặt giá có hiển thị không
        verifyThat("XÁC NHẬN ĐẶT GIÁ", isVisible());
    }

    @Test
    public void testAutoBidActivateButton() {
        // Kiểm tra nút Kích hoạt tự động đấu giá
        verifyThat("KÍCH HOẠT", isVisible());
        clickOn("KÍCH HOẠT");
        
        // Sẽ hiện ra Alert "Tính năng đấu giá tự động sẽ sớm ra mắt!"
        // TestFX có thể bắt Alert dialog bằng clickOn("OK") để đóng lại
        clickOn("OK");
    }

    @Test
    public void testCloseButton() {
        // Nút X
        verifyThat("✕", isVisible());
        // Không click thẳng để tránh đóng màn hình và gây lỗi navigation trong TestFX
    }
}
