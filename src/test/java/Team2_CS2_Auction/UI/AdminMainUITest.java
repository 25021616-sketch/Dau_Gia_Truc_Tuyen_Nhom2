package Team2_CS2_Auction.UI;

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
public class AdminMainUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Trang_chu_Admin.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testAdminSidebarButtonsExist() {
        // Kiểm tra xem các nút điều hướng của Admin có hiển thị hay không
        verifyThat("Bảng điều khiển", isVisible());
        verifyThat("Người dùng", isVisible());
        verifyThat("Kho hàng", isVisible());
        verifyThat("Lịch sử", isVisible());
        verifyThat("Cài đặt", isVisible());
        verifyThat("Đăng xuất", isVisible());
    }

    @Test
    public void testDashboardStatsExist() {
        // Kiểm tra xem các thành phần thống kê số liệu của Admin có hiển thị không
        verifyThat("Tổng doanh thu (30 ngày gần nhất)", isVisible());
        verifyThat("Tổng số phiên đấu giá", isVisible());
        verifyThat("Tổng số người dùng", isVisible());
        verifyThat("Hệ thống Quản trị Đấu giá", isVisible());
    }
}
