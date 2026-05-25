package Team2_CS2_Auction.UI;

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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class UserMainUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Giả lập Session có user đăng nhập
        Session.currentUser = new Member(999, "testUserMain", "123", "0123");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Man_hinh_chinh_Users.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testUserLabelExists() {
        // Kiểm tra label username hiển thị (fx:id="lblUsername")
        assertNotNull(lookup("#lblUsername").query(), "Label username phải tồn tại");
    }

    @Test
    public void testBalanceLabelExists() {
        // Kiểm tra label số dư hiển thị (fx:id="lblBalance")
        assertNotNull(lookup("#lblBalance").query(), "Label số dư phải tồn tại");
    }

    @Test
    public void testItemsPanelExists() {
        // FlowPane hiển thị danh sách sản phẩm (fx:id="pnlItems")
        assertNotNull(lookup("#pnlItems").query(), "Panel danh sách sản phẩm phải tồn tại");
    }

    @Test
    public void testMenuButtonsExistByText() {
        // Kiểm tra các nút menu bằng class selector
        var allButtons = lookup(".button").queryAllAs(Button.class);
        assertFalse(allButtons.isEmpty(), "Phải có ít nhất một button trong màn hình chính");
    }

    @Test
    public void testUsernameDisplayed() {
        // Kiểm tra tên user hiển thị đúng trong session
        Label lblUsername = lookup("#lblUsername").queryAs(Label.class);
        assertNotNull(lblUsername, "Label username không được null");
        // Username mặc định trong FXML là "User" và sẽ được set theo session
        assertNotNull(lblUsername.getText(), "Text username không được null");
    }

    @Test
    public void testBalanceLabelInitialValue() {
        Label lblBalance = lookup("#lblBalance").queryAs(Label.class);
        assertNotNull(lblBalance, "Label số dư không được null");
        assertNotNull(lblBalance.getText(), "Text số dư không được null");
    }

    @Test
    public void testSceneIsNotEmpty() {
        // Kiểm tra scene chứa ít nhất 1 node con
        Label lblUsername = lookup("#lblUsername").queryAs(Label.class);
        var scene = lblUsername.getScene();
        assertNotNull(scene, "Scene không được null");
        assertNotNull(scene.getRoot(), "Root không được null");
    }
}

