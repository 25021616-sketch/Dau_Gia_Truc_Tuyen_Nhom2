package Team2_CS2_Auction.UI;

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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class AdminMainUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Trang_chu_Admin.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testAdminDashboardButtonExists() {
        // Kiểm tra nút Dashboard tồn tại (fx:id="Dashboard")
        assertNotNull(lookup("#Dashboard").query(), "Nút Bảng điều khiển phải tồn tại");
    }

    @Test
    public void testAdminUsersButtonExists() {
        // Kiểm tra nút Người dùng (fx:id="users")
        Button btnUsers = lookup("#users").queryButton();
        assertNotNull(btnUsers, "Nút Người dùng phải tồn tại");
        assertTrue(btnUsers.isVisible(), "Nút Người dùng phải hiển thị");
    }

    @Test
    public void testAdminInventoryButtonExists() {
        // Kiểm tra nút Kho hàng (fx:id="inventory")
        Button btnInventory = lookup("#inventory").queryButton();
        assertNotNull(btnInventory, "Nút Kho hàng phải tồn tại");
        assertTrue(btnInventory.isVisible(), "Nút Kho hàng phải hiển thị");
    }

    @Test
    public void testAdminHistoryButtonExists() {
        // Kiểm tra nút Lịch sử (fx:id="history")
        Button btnHistory = lookup("#history").queryButton();
        assertNotNull(btnHistory, "Nút Lịch sử phải tồn tại");
        assertTrue(btnHistory.isVisible(), "Nút Lịch sử phải hiển thị");
    }

    @Test
    public void testAdminLogoutButtonExists() {
        // Kiểm tra nút Đăng xuất (fx:id="btnLogout")
        Button btnLogout = lookup("#btnLogout").queryButton();
        assertNotNull(btnLogout, "Nút Đăng xuất phải tồn tại");
        assertTrue(btnLogout.isVisible(), "Nút Đăng xuất phải hiển thị");
    }

    @Test
    public void testDashboardStatsLabelsExist() {
        // Kiểm tra các label thống kê (fx:id)
        assertNotNull(lookup("#lblRevenue").query(), "Label doanh thu phải tồn tại");
        assertNotNull(lookup("#lblTotalSessions").query(), "Label tổng phiên phải tồn tại");
        assertNotNull(lookup("#lblTotalUsers").query(), "Label tổng người dùng phải tồn tại");
    }

    @Test
    public void testRevenueLabelInitialValue() {
        Label lblRevenue = lookup("#lblRevenue").queryAs(Label.class);
        assertNotNull(lblRevenue);
        // Ban đầu giá trị có thể là "$0" hoặc được load từ data
        assertNotNull(lblRevenue.getText(), "Label doanh thu không được null");
    }

    @Test
    public void testAllSidebarButtonsAreVisible() {
        // Kiểm tra toàn bộ sidebar buttons hiển thị
        String[] buttonIds = {"Dashboard", "users", "inventory", "history", "btnLogout"};
        for (String id : buttonIds) {
            var node = lookup("#" + id).query();
            assertNotNull(node, "Button [#" + id + "] phải tồn tại trong scene");
            assertTrue(node.isVisible(), "Button [#" + id + "] phải hiển thị");
        }
    }
}

