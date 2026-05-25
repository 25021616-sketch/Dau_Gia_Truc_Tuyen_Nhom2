package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Item_Card_Controller;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Other;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Session.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class ItemCardUITest extends ApplicationTest {

    private static final String TEST_ITEM_NAME = "Test Item Điện Thoại";

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        Session.currentUser = new Member(999, "testBidder", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml"));
        Parent root = fxmlLoader.load();

        Item_Card_Controller controller = fxmlLoader.getController();

        Member seller = new Member(888, "seller", "1", "1");
        Other item = new Other("ITM1", TEST_ITEM_NAME, "Điện thoại", "Desc", null);
        item.setGiaKhoiDiem(100);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        Auction auction = new Auction("AUC1", item, seller, 100.0, 10.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        controller.setData(auction);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testItemNameLabelExists() {
        assertNotNull(lookup("#lblTenSP").query(), "Label tên sản phẩm phải tồn tại");
    }

    @Test
    public void testItemNameLabelDisplaysCorrectText() {
        Label lblTenSP = lookup("#lblTenSP").queryAs(Label.class);
        assertNotNull(lblTenSP, "Label tên sản phẩm không được null");
        assertEquals(TEST_ITEM_NAME, lblTenSP.getText(), "Tên sản phẩm phải hiển thị đúng");
    }

    @Test
    public void testItemCategoryLabelExists() {
        assertNotNull(lookup("#lblLoaiSP").query(), "Label loại sản phẩm phải tồn tại");
    }

    @Test
    public void testCurrentPriceLabelExists() {
        assertNotNull(lookup("#lblGiaHienTai").query(), "Label giá hiện tại phải tồn tại");
    }

    @Test
    public void testTimerLabelExists() {
        assertNotNull(lookup("#lblThoiGian").query(), "Label thời gian còn lại phải tồn tại");
    }

    @Test
    public void testBidButtonExists() {
        // Nút Đặt giá (fx:id="btnDatGia")
        assertNotNull(lookup("#btnDatGia").query(), "Nút đặt giá phải tồn tại");
    }

    @Test
    public void testBidButtonIsVisible() {
        Button btnDatGia = lookup("#btnDatGia").queryButton();
        assertNotNull(btnDatGia);
        assertTrue(btnDatGia.isVisible(), "Nút đặt giá phải hiển thị");
    }

    @Test
    public void testStatusBadgeExists() {
        assertNotNull(lookup("#lblBadgeTrangThai").query(), "Badge trạng thái phải tồn tại");
    }

    @Test
    public void testItemImageExists() {
        assertNotNull(lookup("#imgSanPham").query(), "ImageView sản phẩm phải tồn tại");
    }
}

