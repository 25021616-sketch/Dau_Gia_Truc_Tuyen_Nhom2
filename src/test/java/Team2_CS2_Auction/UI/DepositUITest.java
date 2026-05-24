package Team2_CS2_Auction.UI;

import Team2_CS2_Auction.Controller.Nap_Tien_Controller;
import Team2_CS2_Auction.Model.user.Member;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class DepositUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/nap_tien.fxml"));
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
    }

    @Test
    public void testEmptyDeposit() {
        // Không điền gì và bấm Xác Nhận Nạp
        clickOn("XÁC NHẬN NẠP");
        verifyThat("#lblMessage", hasText("Vui lòng nhập số tiền!"));
    }

    @Test
    public void testNegativeDeposit() {
        // Nạp số tiền âm
        clickOn("#txtAmount").write("-100");
        clickOn("XÁC NHẬN NẠP");
        // Kiểm tra xem message hiển thị lỗi số âm
        verifyThat("#lblMessage", hasText("Số tiền nạp phải > 0"));
    }

    @Test
    public void testInvalidNumberFormat() {
        // Nạp chữ cái thay vì số
        clickOn("#txtAmount").write("abc");
        clickOn("XÁC NHẬN NẠP");
        verifyThat("#lblMessage", hasText("Số tiền phải là con số!"));
    }
}
