package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.item.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public abstract class Base_Admin_Controller {

    // Đường dẫn gốc đến thư mục chứa FXML của bạn
    private final String BASE_PATH = "/Team2_CS2_Auction/example/myauctionapp/";

    /**
     * Hàm chuyển trang cơ bản (không truyền dữ liệu)
     */
    public void switchScene(ActionEvent event, String fxmlFileName, String title) {
        navigate(event, fxmlFileName, title, null);
    }

    /**
     * Hàm chuyển trang CÓ TRUYỀN DỮ LIỆU (Dùng để vào trang chi tiết đấu giá)
     * @param data: Đối tượng Item cần truyền đi
     */
    public void switchSceneWithData(ActionEvent event, String fxmlFileName, String title, Object data) {
        navigate(event, fxmlFileName, title, data);
    }

    /**
     * Hàm điều hướng lõi xử lý mọi trường hợp
     */
    private void navigate(ActionEvent event, String fxmlFileName, String title, Object data) {
        try {
            URL fxmlLocation = getClass().getResource(BASE_PATH + fxmlFileName);
            if (fxmlLocation == null) {
                System.err.println("❌ Không tìm thấy file FXML: " + BASE_PATH + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // --- PHẦN QUAN TRỌNG: TRUYỀN DỮ LIỆU SANG CONTROLLER MỚI ---
            if (data != null) {
                Object controller = loader.getController();

                // Nếu trang đích là Phiên Đấu Giá, hãy gọi hàm nạp dữ liệu của nó
                if (controller instanceof Phien_Dau_Gia_Controller) {
                    ((Phien_Dau_Gia_Controller) controller).setItemData((Item) data);
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