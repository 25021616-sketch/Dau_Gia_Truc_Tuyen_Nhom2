package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction; // ✅ Sử dụng Auction
import Team2_CS2_Auction.Repository.ProductRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Admin_quan_li_dau_gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private TableView<Auction> tableItems; // ✅ Chuyển sang Auction
    @FXML private TableColumn<Auction, String> colID;
    @FXML private TableColumn<Auction, String> colTen;
    @FXML private TableColumn<Auction, String> colLoai;
    @FXML private TableColumn<Auction, Double> colGia;
    @FXML private TableColumn<Auction, LocalDateTime> colKetThuc;
    @FXML private TableColumn<Auction, LocalDateTime> colBatDau;
    @FXML private TableColumn<Auction, Double> colBuocGia;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Ánh xạ dữ liệu thông qua Auction.getItem()
        // Vì dữ liệu nằm trong lớp Item bên trong Auction, ta dùng lambda để lấy
        colID.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getId()));
        colTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getTenSanPham()));
        colLoai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getLoaiSanPham()));

        // 2. Định dạng cột GIÁ (Lấy giá khởi điểm từ Auction)
        colGia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCurrentPrice()));
        formatPriceColumn(colGia);

        // 3. Định dạng cột BƯỚC GIÁ
        colBuocGia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStepPrice()));
        formatPriceColumn(colBuocGia);

        // 4. Định dạng cột THỜI GIAN BẮT ĐẦU
        colBatDau.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStartTime()));
        formatDateColumn(colBatDau);

        // 5. Định dạng cột THỜI GIAN KẾT THÚC
        colKetThuc.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndTime()));
        formatDateColumn(colKetThuc);

        // 6. TẢI DỮ LIỆU TỪ DATABASE (PRODUCT REPOSITORY)
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        ProductRepository repo = new ProductRepository();
        // Lấy danh sách từ DB
        ObservableList<Auction> list = FXCollections.observableArrayList(repo.getAllProducts());

        if (list != null) {
            tableItems.setItems(list);
            tableItems.refresh();
            System.out.println(">>> [ADMIN] Đã tải: " + list.size() + " phiên đấu giá từ Database.");
        }
    }

    private void formatPriceColumn(TableColumn<Auction, Double> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", price));
                }
            }
        });
    }

    private void formatDateColumn(TableColumn<Auction, LocalDateTime> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---
    @FXML
    public void handleGoToDashboard(ActionEvent event) {
        switchScene(event, "Trang_chu_Admin.fxml", "Bảng điều khiển");
    }

    @FXML
    public void handleGoToUsers(ActionEvent event) {
        switchScene(event, "Admin_quan_li_User.fxml", "Quản lý người dùng");
    }

    @FXML
    public void handleGoToHistory(ActionEvent event) {
        switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá");
    }

    @FXML
    public void handleAddNewListing(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "Niêm yết sản phẩm mới");
    }
}