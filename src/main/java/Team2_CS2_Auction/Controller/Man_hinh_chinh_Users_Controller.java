package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Repository.AuctionData;
import Team2_CS2_Auction.Model.item.Item;
import Team2_CS2_Auction.Repository.ProductRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Man_hinh_chinh_Users_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private FlowPane pnlItems;

    private List<Item_Card_Controller> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ProductRepository repo = new ProductRepository();

        List<Item> listFromDB = repo.getAllProducts();

        ObservableList<Item> observableList =
                FXCollections.observableArrayList(listFromDB);

        hienThiDanhSach(observableList);
    }

    // ✅ Đổi sang ObservableList
    private void hienThiDanhSach(ObservableList<Item> items) {
        if (pnlItems == null) return;

        // Dừng timeline cũ
        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }

        activeControllers.clear();
        pnlItems.getChildren().clear();

        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml")
                );

                Parent card = loader.load();

                Item_Card_Controller cardController = loader.getController();
                cardController.setData(item);

                activeControllers.add(cardController);
                pnlItems.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Lỗi nạp card: " + e.getMessage());
            }
        }
    }

    // ================== NAVIGATION ==================

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm");
    }

    @FXML
    public void chuyenSangdanhsachtheodoi(ActionEvent event) {
        switchScene(event, "danh_sach_theo_doi_San_Pham.fxml", "Danh sách theo dõi");
    }

    @FXML
    public void handleGoToLichSu(ActionEvent event) {
        switchScene(event, "Lich_su_giao_dich.fxml", "Lịch sử giao dịch");
    }

    @FXML
    public void handleGoToDangNhap(ActionEvent event) {
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
    }

    @FXML
    private void handleFilterAll(ActionEvent event) {
        ObservableList<Item> observableList =
                FXCollections.observableArrayList(AuctionData.listSanPham);

        hienThiDanhSach(observableList);
    }
}