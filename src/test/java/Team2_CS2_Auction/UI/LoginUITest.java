package Team2_CS2_Auction.UI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class LoginUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
        // Cấu hình TestFX chạy headless (không cần màn hình thật)
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_nhap.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        interact(() -> {
            TextField tfUser = lookup("#Ten_dang_nhap").queryAs(TextField.class);
            javafx.scene.control.PasswordField pfPass = lookup("#Mat_khau").queryAs(javafx.scene.control.PasswordField.class);
            if (tfUser != null) tfUser.clear();
            if (pfPass != null) pfPass.clear();
        });
    }

    @Test
    public void testLoginFormHasRequiredFields() {
        // Kiểm tra TextField đăng nhập tồn tại trong scene
        assertNotNull(lookup("#Ten_dang_nhap").query(), "TextField tên đăng nhập phải tồn tại");
        assertNotNull(lookup("#Mat_khau").query(), "PasswordField mật khẩu phải tồn tại");
        assertNotNull(lookup("#btnLogin").query(), "Nút đăng nhập phải tồn tại");
        assertNotNull(lookup("#lblMessage").query(), "Label thông báo lỗi phải tồn tại");
    }

    @Test
    public void testLoginButtonIsVisible() {
        Button btnLogin = lookup("#btnLogin").queryButton();
        assertNotNull(btnLogin, "Nút đăng nhập không được null");
        assertTrue(btnLogin.isVisible(), "Nút đăng nhập phải hiển thị");
    }

    @Test
    public void testUsernameFieldIsEditable() {
        TextField tfUser = lookup("#Ten_dang_nhap").queryAs(TextField.class);
        assertNotNull(tfUser, "TextField tên đăng nhập không được null");
        assertTrue(tfUser.isEditable(), "TextField tên đăng nhập phải có thể nhập");
    }

    @Test
    public void testEmptyLoginFieldsShowsError() {
        // Bấm vào nút Đăng nhập mà không nhập gì
        clickOn("#btnLogin");
        WaitForAsyncUtils.waitForFxEvents();

        Label lblMessage = lookup("#lblMessage").queryAs(Label.class);
        assertNotNull(lblMessage, "Label thông báo phải tồn tại");
        String msg = lblMessage.getText();
        assertNotNull(msg, "Thông báo lỗi không được null");
        assertFalse(msg.isBlank(), "Thông báo lỗi không được rỗng khi đăng nhập trống");
    }

    @Test
    public void testEnterUsernameAndPassword() {
        // Nhập tài khoản và mật khẩu
        TextField tfUser = lookup("#Ten_dang_nhap").queryAs(TextField.class);
        javafx.scene.control.PasswordField pfPass = lookup("#Mat_khau").queryAs(javafx.scene.control.PasswordField.class);

        interact(() -> {
            tfUser.clear();
            tfUser.requestFocus();
        });
        write("testuser");

        interact(() -> {
            pfPass.clear();
            pfPass.requestFocus();
        });
        write("testpass");

        assertEquals("testuser", tfUser.getText(), "TextField phải lưu đúng username đã nhập");
    }

    @Test
    public void testAdminCheckboxExists() {
        // Kiểm tra checkbox đăng nhập Admin tồn tại
        assertNotNull(lookup("#Dang_nhap_Admin").query(), "Checkbox Đăng nhập Admin phải tồn tại");
    }

    @Test
    public void testTogglePasswordButtonExists() {
        // Nút toggle hiển thị mật khẩu
        assertNotNull(lookup("#btnTogglePassword").query(), "Nút toggle mật khẩu phải tồn tại");
    }
}

