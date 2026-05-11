package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction; // ✅ Import Auction
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
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
    protected void showAlert(Alert.AlertType type,
                             String title,
                             String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        // Làm đẹp Dialog
        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.setPrefWidth(420);

        dialogPane.setStyle("""
        -fx-background-color: #ffffff;
        -fx-font-size: 14px;
        -fx-font-family: "Segoe UI";
        -fx-border-color: #dcdcdc;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        // Style nút OK
        Button okButton =
                (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.setStyle("""
        -fx-background-color: #4CAF50;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
        -fx-cursor: hand;
    """);

        // Đổi màu theo loại alert
        switch (type) {

            case ERROR -> okButton.setStyle("""
            -fx-background-color: #e53935;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

            case WARNING -> okButton.setStyle("""
            -fx-background-color: #fb8c00;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

            case INFORMATION -> okButton.setStyle("""
            -fx-background-color: #1e88e5;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);
        }

        alert.showAndWait();
    }
}