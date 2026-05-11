package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction; // ✅ Import Auction
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public abstract class Base_Admin_Controller {

    private final String BASE_PATH = "/Team2_CS2_Auction/example/myauctionapp/";

    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        navigate(event, fxmlFileName, title, null);
    }

    /**
     * ✅ Sửa parameter từ Item sang Object hoặc Auction để đồng bộ
     */
    public void switchSceneWithData(ActionEvent event, String fxmlFileName, String title, Object data) {
        navigate(event, fxmlFileName, title, data);
    }

    private void navigate(ActionEvent event, String fxmlFileName, String title, Object data) {
        try {
            URL fxmlLocation = getClass().getResource(BASE_PATH + fxmlFileName);
            if (fxmlLocation == null) {
                System.err.println("❌ Không tìm thấy file FXML: " + BASE_PATH + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // --- PHẦN QUAN TRỌNG: TRUYỀN DỮ LIỆU ---
            if (data != null) {
                Object controller = loader.getController();

                // ✅ Sửa logic kiểm tra: Nếu là trang Phiên Đấu Giá, truyền Auction
                if (controller instanceof Phien_Dau_Gia_Controller) {
                    // Gọi hàm setAuctionData (hàm chúng ta vừa đổi tên ở bước trước)
                    ((Phien_Dau_Gia_Controller) controller).setAuctionData((Auction) data);
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            stage.setTitle(title);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Lỗi nạp giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }
}