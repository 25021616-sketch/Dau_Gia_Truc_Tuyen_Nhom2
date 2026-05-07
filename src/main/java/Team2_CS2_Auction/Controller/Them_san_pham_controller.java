package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Model.item.ItemFactory;
import Team2_CS2_Auction.Repository.AuctionData;
// import Team2_CS2_Auction.Repository.ProductRepository;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Them_san_pham_controller extends Base_Admin_Controller implements Initializable {

    @FXML private TextField txtTenSanPham, txtGiaKhoiDiem, txtBuocGia;
    @FXML private TextField gioBatDau, phutBatDau, gioKetThuc, phutKetThuc;
    @FXML private ComboBox<String> loaiSanPhamCombo;
    @FXML private TextArea txtMoTa;
    @FXML private DatePicker ngayBatDauPicker, ngayKetThucPicker;
    @FXML private FlowPane imageGalleryPane;

    private List<String> listImagePaths = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loaiSanPhamCombo.setItems(FXCollections.observableArrayList(
                "Đồ điện tử",           // Mapped to Electronics
                "Tác phẩm nghệ thuật",  // Mapped to Art
                "Bất động sản",         // Mapped to RealEstate
                "Xe hơi",               // Mapped to Vehicle
                "Khác"                  // Mapped to Other
        ));
    }

    @FXML
    private void handleDangSanPham(ActionEvent event) {
        try {
            String loai = loaiSanPhamCombo.getValue();
            if (loai == null) throw new Exception("Vui lòng chọn loại sản phẩm!");

            LocalDateTime start = ngayBatDauPicker.getValue().atTime(
                    Integer.parseInt(gioBatDau.getText()),
                    Integer.parseInt(phutBatDau.getText())
            );

            LocalDateTime end = ngayKetThucPicker.getValue().atTime(
                    Integer.parseInt(gioKetThuc.getText()),
                    Integer.parseInt(phutKetThuc.getText())
            );

            if(start.isAfter(end)) throw new Exception("Thời gian bắt đầu không được lớn hơn kết thúc!");

            // TẠO ITEM
            String itemId = "ITEM_" + System.currentTimeMillis();
            Item newItem = ItemFactory.createItem(
                    itemId,
                    txtTenSanPham.getText(),
                    loai,
                    txtMoTa.getText(),
                    new ArrayList<>(listImagePaths)
            );

            // TẠO AUCTION
            String auctionId = "AUC_" + System.currentTimeMillis();
            double startPrice = Double.parseDouble(txtGiaKhoiDiem.getText());
            double stepPrice = Double.parseDouble(txtBuocGia.getText());

            // TODO: Ở đây lấy Seller từ session đang login. Tạm thời truyền null hoặc mockup.
            Auction newAuction = new Auction(
                    auctionId,
                    newItem,
                    null, // currentUser hoặc seller
                    startPrice,
                    stepPrice,
                    start,
                    end
            );

            // LƯU VÀO CƠ SỞ DỮ LIỆU
            /* TODO: Cập nhật lại Repository của bạn để lưu cả Item và Auction nhé.
               Vì bạn đã sửa Model nên bảng DB cũng cần thay đổi để tách bảng Items và Auctions.

               ProductRepository repo = new ProductRepository();
               boolean success = repo.insertAuctionAndItem(newAuction);
            */
            boolean success = true; // Giả lập lưu thành công

            if (success) {
                // Nếu List này nằm trong ram dùng cho test thì lưu Auction thay vì Item
                // AuctionData.listAuction.add(newAuction);

                resetForm();
                new Alert(Alert.AlertType.INFORMATION, "Tạo phiên đấu giá thành công!").showAndWait();
                handleBackToHome(event);
            } else {
                throw new Exception("Không thể lưu dữ liệu vào DataBase!");
            }

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: Giá và Giờ phải là con số hợp lệ!").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Hỗ trợ chọn nhiều ảnh cùng lúc
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                String path = file.toURI().toString();
                listImagePaths.add(path);

                // Tạo ImageView thumbnail và nhét vào giao diện
                ImageView thumbnail = new ImageView(new Image(path));
                thumbnail.setFitWidth(80);
                thumbnail.setFitHeight(80);
                thumbnail.setPreserveRatio(false); // Cắt vuông ảnh

                // Gắn CSS để đẹp hơn (Nếu có class 'thumbnail-img' trong css)
                thumbnail.getStyleClass().add("thumbnail-img");

                imageGalleryPane.getChildren().add(thumbnail);
            }
        }
    }

    private void resetForm() {
        txtTenSanPham.clear();
        txtMoTa.clear();
        txtGiaKhoiDiem.clear();
        txtBuocGia.clear();
        gioBatDau.clear(); phutBatDau.clear();
        gioKetThuc.clear(); phutKetThuc.clear();
        ngayBatDauPicker.setValue(null);
        ngayKetThucPicker.setValue(null);
        loaiSanPhamCombo.getSelectionModel().clearSelection();

        listImagePaths.clear();
        imageGalleryPane.getChildren().clear();
    }

    @FXML
    public void handleBackToHome(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Trang chủ");
    }
}