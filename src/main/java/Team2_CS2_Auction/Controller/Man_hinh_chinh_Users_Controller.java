package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
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

    // Khởi tạo repo để dùng chung
    private final ProductRepository repo = new ProductRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    private void loadData() {
        // Lấy danh sách Auction từ Database
        List<Auction> listFromDB = repo.getAllProducts();
        ObservableList<Auction> observableList = FXCollections.observableArrayList(listFromDB);
        hienThiDanhSach(observableList);
    }

    private void hienThiDanhSach(ObservableList<Auction> auctions) {
        if (pnlItems == null) return;

        // Dừng timeline cũ để tránh rò rỉ bộ nhớ
        for (Item_Card_Controller ctrl : activeControllers) {
            if (ctrl != null) ctrl.stopTimeline();
        }

        activeControllers.clear();
        pnlItems.getChildren().clear();

        for (Auction auction : auctions) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Team2_CS2_Auction/example/myauctionapp/ItemCard.fxml")
                );

                Parent card = loader.load();
                Item_Card_Controller cardController = loader.getController();

                // ✅ Đảm bảo Item_Card_Controller đã đổi thành setData(Auction a)
                cardController.setData(auction);

                activeControllers.add(cardController);
                pnlItems.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Lỗi nạp card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFilterAll(ActionEvent event) {
        // ✅ Fix: Gọi lại loadData hoặc repo thay vì truy cập biến static không tồn tại
        loadData();
    }

    // ================== NAVIGATION (Giữ nguyên) ==================
    @FXML public void handleGoTothemsanpham(ActionEvent event) { switchScene(event, "them_san_pham.fxml", "Thêm sản phẩm"); }
    @FXML public void chuyenSangdanhsachtheodoi(ActionEvent event) { switchScene(event, "danh_sach_theo_doi_San_Pham.fxml", "Danh sách theo dõi"); }
    @FXML public void handleGoToLichSu(ActionEvent event) { switchScene(event, "Lich_su_giao_dich.fxml", "Lịch sử giao dịch"); }
    @FXML public void handleGoToDangNhap(ActionEvent event) { switchScene(event, "dang_nhap.fxml", "Đăng nhập"); }
}