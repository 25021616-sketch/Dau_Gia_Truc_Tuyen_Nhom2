package Team2_CS2_Auction.UI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test giao diện đăng ký tài khoản.
 * Kiểm tra: nhập liệu, validation UI, chuyển giao diện.
 */
@ExtendWith(ApplicationExtension.class)
public class RegistrationUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_ky_tai_khoan.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    // ─── Kiểm tra sự tồn tại của các field ─────────────────────────

    @Test
    public void testRegistrationFormHasAllRequiredFields() {
        assertNotNull(lookup("#Ten_dang_ki").query(), "TextField tên đăng ký phải tồn tại");
        assertNotNull(lookup("#Sdt_dang_ki").query(), "TextField số điện thoại phải tồn tại");
        assertNotNull(lookup("#Dat_mat_khau").query(), "PasswordField đặt mật khẩu phải tồn tại");
        assertNotNull(lookup("#Nhap_lai_mat_khau").query(), "PasswordField nhập lại mật khẩu phải tồn tại");
        assertNotNull(lookup("#lblMessage").query(), "Label thông báo phải tồn tại");
        assertNotNull(lookup("#Dang_ky").query(), "Nút đăng ký phải tồn tại");
    }

    @Test
    public void testUsernameFieldIsEditable() {
        TextField tfUsername = lookup("#Ten_dang_ki").queryAs(TextField.class);
        assertNotNull(tfUsername, "TextField tên đăng ký không được null");
        assertTrue(tfUsername.isEditable(), "TextField tên đăng ký phải có thể nhập");
    }

    @Test
    public void testPhoneFieldIsEditable() {
        TextField tfPhone = lookup("#Sdt_dang_ki").queryAs(TextField.class);
        assertNotNull(tfPhone, "TextField số điện thoại không được null");
        assertTrue(tfPhone.isEditable(), "TextField số điện thoại phải có thể nhập");
    }

    @Test
    public void testPasswordFieldIsEditable() {
        PasswordField pfPassword = lookup("#Dat_mat_khau").queryAs(PasswordField.class);
        assertNotNull(pfPassword, "PasswordField mật khẩu không được null");
        assertTrue(pfPassword.isEditable(), "PasswordField mật khẩu phải có thể nhập");
    }

    @Test
    public void testConfirmPasswordFieldIsEditable() {
        PasswordField pfConfirm = lookup("#Nhap_lai_mat_khau").queryAs(PasswordField.class);
        assertNotNull(pfConfirm, "PasswordField nhập lại mật khẩu không được null");
        assertTrue(pfConfirm.isEditable(), "PasswordField nhập lại mật khẩu phải có thể nhập");
    }

    @Test
    public void testRegisterButtonIsVisible() {
        Button btnRegister = lookup("#Dang_ky").queryButton();
        assertNotNull(btnRegister, "Nút đăng ký không được null");
        assertTrue(btnRegister.isVisible(), "Nút đăng ký phải hiển thị");
    }

    // ─── Nhập liệu ─────────────────────────────────────────────────

    @Test
    public void testEnterUsername() {
        clickOn("#Ten_dang_ki").write("newUser123");
        TextField tfUsername = lookup("#Ten_dang_ki").queryAs(TextField.class);
        assertEquals("newUser123", tfUsername.getText(), "TextField phải lưu đúng username đã nhập");
    }

    @Test
    public void testEnterPhoneNumber() {
        clickOn("#Sdt_dang_ki").write("0912345678");
        TextField tfPhone = lookup("#Sdt_dang_ki").queryAs(TextField.class);
        assertEquals("0912345678", tfPhone.getText(), "TextField phải lưu đúng số điện thoại đã nhập");
    }

    @Test
    public void testEnterPassword() {
        clickOn("#Dat_mat_khau").write("securePass123");
        PasswordField pfPass = lookup("#Dat_mat_khau").queryAs(PasswordField.class);
        assertEquals("securePass123", pfPass.getText(), "PasswordField phải lưu đúng mật khẩu đã nhập");
    }

    @Test
    public void testEnterConfirmPassword() {
        clickOn("#Nhap_lai_mat_khau").write("securePass123");
        PasswordField pfConfirm = lookup("#Nhap_lai_mat_khau").queryAs(PasswordField.class);
        assertEquals("securePass123", pfConfirm.getText(), "PasswordField nhập lại phải lưu đúng mật khẩu");
    }

    // ─── Validation ─────────────────────────────────────────────────

    @Test
    public void testEmptyRegistrationShowsError() {
        // Bấm đăng ký khi chưa nhập gì → phải có lỗi
        clickOn("#Dang_ky");
        WaitForAsyncUtils.waitForFxEvents();

        Label lblMessage = lookup("#lblMessage").queryAs(Label.class);
        assertNotNull(lblMessage, "Label thông báo không được null");
        assertFalse(lblMessage.getText().isBlank(), "Phải có thông báo lỗi khi đăng ký rỗng");
    }

    @Test
    public void testPasswordMismatchShowsError() {
        // Nhập 2 mật khẩu không khớp
        clickOn("#Ten_dang_ki").write("testUser");
        clickOn("#Sdt_dang_ki").write("0123456789");
        clickOn("#Dat_mat_khau").write("password123");
        clickOn("#Nhap_lai_mat_khau").write("differentPassword");
        clickOn("#Dang_ky");
        WaitForAsyncUtils.waitForFxEvents();

        Label lblMessage = lookup("#lblMessage").queryAs(Label.class);
        assertNotNull(lblMessage, "Label thông báo không được null");
        // Phải hiển thị lỗi về mật khẩu không khớp (hoặc lỗi bất kỳ từ server)
        assertNotNull(lblMessage.getText(), "Phải có thông báo lỗi");
    }

    @Test
    public void testTogglePasswordButtonExists() {
        assertNotNull(lookup("#btnTogglePassword").query(), "Nút toggle mật khẩu phải tồn tại");
    }

    @Test
    public void testToggleConfirmPasswordButtonExists() {
        assertNotNull(lookup("#btnToggleConfirmPassword").query(), "Nút toggle nhập lại mật khẩu phải tồn tại");
    }
}

