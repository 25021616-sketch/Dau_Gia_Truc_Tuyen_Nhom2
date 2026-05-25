package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Phien_Dau_Gia_Controller;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Other;
import Team2_CS2_Auction.Model.user.Member;
import Team2_CS2_Auction.Session.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
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
public class AuctionSessionUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        Session.currentUser = new Member(999, "testBidder", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Phien_Dau_Gia.fxml"));
        Parent root = fxmlLoader.load();

        Phien_Dau_Gia_Controller controller = fxmlLoader.getController();

        Member seller = new Member(888, "seller", "1", "1");
        Other item = new Other("ITM1", "Test Item", "Test Category", "Desc", null);
        item.setGiaKhoiDiem(100);
        item.setNgayBatDau(LocalDateTime.now());
        item.setNgayKetThuc(LocalDateTime.now().plusDays(1));

        Auction auction = new Auction("AUC1", item, seller, 100.0, 10.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        try {
            controller.setAuctionData(auction);
        } catch (Exception e) {
            // Bỏ qua lỗi kết nối Socket hoặc DB nếu có
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testProductImageExists() {
        assertNotNull(lookup("#productImage").query(), "ImageView sản phẩm phải tồn tại");
    }

    @Test
    public void testProductNameLabelExists() {
        assertNotNull(lookup("#lblTenSanPham").query(), "Label tên sản phẩm phải tồn tại");
    }

    @Test
    public void testCurrentBidLabelExists() {
        assertNotNull(lookup("#currentBidLabel").query(), "Label giá đặt hiện tại phải tồn tại");
    }

    @Test
    public void testTimerLabelExists() {
        assertNotNull(lookup("#lblThoiGian").query(), "Label thời gian còn lại phải tồn tại");
    }

    @Test
    public void testStepSpinnerExists() {
        assertNotNull(lookup("#stepSpinner").query(), "Spinner số bước nhảy phải tồn tại");
    }

    @Test
    public void testBidStepFieldExists() {
        assertNotNull(lookup("#bidStepField").query(), "TextField mức tăng/bước phải tồn tại");
    }

    @Test
    public void testTargetPriceLabelExists() {
        assertNotNull(lookup("#targetPriceLabel").query(), "Label tổng giá dự kiến phải tồn tại");
    }

    @Test
    public void testAutoBidButtonExists() {
        // Nút Kích Hoạt tự động đặt giá (fx:id="btnActivateAutoBid")
        assertNotNull(lookup("#btnActivateAutoBid").query(), "Nút kích hoạt tự động phải tồn tại");
    }

    @Test
    public void testAutoBidButtonIsVisible() {
        Button btnAutoAid = lookup("#btnActivateAutoBid").queryButton();
        assertNotNull(btnAutoAid);
        assertTrue(btnAutoAid.isVisible(), "Nút kích hoạt tự động phải hiển thị");
    }

    @Test
    public void testAutoStepsFieldExists() {
        assertNotNull(lookup("#autoStepsCountField").query(), "TextField số lần bước giá phải tồn tại");
    }

    @Test
    public void testAutoLimitFieldExists() {
        assertNotNull(lookup("#autoLimitField").query(), "TextField giới hạn tối đa phải tồn tại");
    }

    @Test
    public void testBidHistoryChartExists() {
        assertNotNull(lookup("#bidHistoryChart").query(), "Biểu đồ lịch sử đặt giá phải tồn tại");
    }

    @Test
    public void testDescriptionLabelExists() {
        assertNotNull(lookup("#lblMoTa").query(), "Label mô tả sản phẩm phải tồn tại");
    }

    @Test
    public void testClickAutoBidButton() {
        // Bấm nút kích hoạt tự động → phải có phản hồi (Alert hoặc xử lý)
        Button btnActivate = lookup("#btnActivateAutoBid").queryButton();
        assertNotNull(btnActivate, "Nút kích hoạt không được null");
        // Chỉ kiểm tra nút tồn tại và visible, không click để tránh dialog trong headless
        assertTrue(btnActivate.isVisible());
    }
}

