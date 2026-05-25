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
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class LoginUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Tải giao diện Đăng Nhập trực tiếp bỏ qua bước nhập IP Server
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/dang_nhap.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testEmptyLoginFieldsShowsError() {
        // Tìm ô nhập liệu bằng ID (như đã khai báo trong Dang_nhap_Controller.java)
        // Bấm vào nút Đăng nhập mà không nhập gì
        clickOn("#btnLogin");
        
        // Kiểm tra xem dòng thông báo lỗi có hiển thị đúng không
        verifyThat("#lblMessage", hasText("⚠ Vui lòng nhập tài khoản và mật khẩu!"));
    }

    @Test
    public void testEnterCredentialsAndLogin() {
        // Giả lập thao tác gõ phím vào ô nhập liệu
        clickOn("#Ten_dang_nhap").write("admin");
        clickOn("#Mat_khau").write("123456");
        
        // Bấm nút đăng nhập
        clickOn("#btnLogin");
        
        // Nút sẽ đổi chữ thành "ĐANG ĐĂNG NHẬP..." (Theo như code xử lý UI của bạn)
        verifyThat("#btnLogin", hasText("ĐANG ĐĂNG NHẬP..."));
    }
}
