package Team2_CS2_Auction.UI;

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

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
public class UserMainUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Cần giả lập Session có user đăng nhập
        Session.currentUser = new Member(999, "testUserMain", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Man_hinh_chinh_Users.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testMenuButtonsExist() {
        // Kiểm tra xem các nút menu có hiển thị không
        verifyThat("CÁC PHIÊN ĐẤU GIÁ", isVisible());
        verifyThat("NẠP TIỀN", isVisible());
        verifyThat("PHIÊN ĐÃ THAM GIA", isVisible());
        verifyThat("PHIÊN CỦA TÔI", isVisible());
        verifyThat("TẠO PHIÊN ĐẤU GIÁ", isVisible());
        verifyThat("ĐĂNG XUẤT", isVisible());
    }

    @Test
    public void testClickNapTienOpensPopup() {
        // Bấm nút Nạp tiền
        clickOn("NẠP TIỀN");
        // Kiểm tra xem Popup Nạp Tiền (có nút XÁC NHẬN NẠP) có mở lên hay không
        verifyThat("XÁC NHẬN NẠP", isVisible());
    }
}
