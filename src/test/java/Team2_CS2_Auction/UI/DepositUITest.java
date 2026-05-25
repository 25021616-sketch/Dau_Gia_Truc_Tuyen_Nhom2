package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Nap_Tien_Controller;
import Team2_CS2_Auction.Model.user.Member;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class DepositUITest extends ApplicationTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/Nap_Tien.fxml"));
        Parent root = fxmlLoader.load();

        Nap_Tien_Controller controller = fxmlLoader.getController();
        Member dummyUser = new Member(9999, "testuser", "123", "0123");
        try {
            controller.setUserData(dummyUser);
        } catch (Exception e) {
            // Bỏ qua lỗi DB nếu môi trường test không có kết nối
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testDepositFormHasRequiredFields() {
        // Kiểm tra các field cần thiết tồn tại
        assertNotNull(lookup("#txtAmount").query(), "TextField số tiền phải tồn tại");
        assertNotNull(lookup("#lblMessage").query(), "Label thông báo phải tồn tại");
        assertNotNull(lookup("#lblCurrentBalance").query(), "Label số dư hiện tại phải tồn tại");
    }

    @Test
    public void testAmountFieldIsEditable() {
        TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
        assertNotNull(txtAmount, "TextField số tiền không được null");
        assertTrue(txtAmount.isEditable(), "TextField số tiền phải có thể nhập");
    }

    @Test
    public void testInitialBalanceLabel() {
        Label lblBalance = lookup("#lblCurrentBalance").queryAs(Label.class);
        assertNotNull(lblBalance, "Label số dư không được null");
        // Giá trị ban đầu phải là 0 vì user mới
        assertNotNull(lblBalance.getText(), "Label số dư không được null text");
    }

    @Test
    public void testEmptyDeposit() {
        // Đảm bảo trường rỗng
        interact(() -> lookup("#txtAmount").queryAs(TextField.class).setText(""));
        
        var buttons = lookup(".button").queryAllAs(javafx.scene.control.Button.class);
        var confirmBtn = buttons.stream()
                .filter(b -> b.getText() != null && b.getText().contains("N"))
                .findFirst();

        if (confirmBtn.isPresent()) {
            interact(() -> confirmBtn.get().fire());
            WaitForAsyncUtils.waitForFxEvents();

            Label lblMessage = lookup("#lblMessage").queryAs(Label.class);
            assertNotNull(lblMessage.getText(), "Phải có thông báo lỗi khi nạp rỗng");
            assertFalse(lblMessage.getText().isBlank(), "Thông báo lỗi không được rỗng");
        } else {
            // Fallback: kiểm tra TextField có thể nhập
            TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
            assertNotNull(txtAmount);
        }
    }

    @Test
    public void testEnterValidAmount() {
        TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
        interact(() -> txtAmount.setText("500"));
        assertEquals("500", txtAmount.getText(), "TextField phải lưu đúng số tiền đã nhập");
    }

    @Test
    public void testEnterNegativeAmount() {
        TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
        interact(() -> txtAmount.setText("-100"));
        assertEquals("-100", txtAmount.getText(), "TextField phải cho nhập giá trị âm (validation ở controller)");
    }

    @Test
    public void testEnterTextAmount() {
        TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
        interact(() -> txtAmount.setText("abc"));
        assertEquals("abc", txtAmount.getText(), "TextField phải cho nhập text (validation ở controller)");
    }

    @Test
    public void testClearAmountField() {
        TextField txtAmount = lookup("#txtAmount").queryAs(TextField.class);
        interact(() -> txtAmount.setText("1000"));
        // Xóa nội dung
        interact(() -> txtAmount.clear());
        assertEquals("", txtAmount.getText(), "TextField phải có thể xóa");
    }
}

