package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Repository.AuctionData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import Team2_CS2_Auction.Model.item.Item;

public class Admin_quan_li_dau_gia_Controller extends Base_Admin_Controller implements Initializable {
    // Các cột thành phần
    @FXML private TableView<Item> tableItems;
    @FXML private TableColumn<Item, String> colID;
    @FXML private TableColumn<Item, String> colTen;
    @FXML private TableColumn<Item, String> colLoai;
    @FXML private TableColumn<Item, Double> colGia;
    @FXML private TableColumn<Item, LocalDateTime> colKetThuc;
    @FXML private TableColumn<Item, LocalDateTime> colBatDau;
    @FXML private TableColumn<Item, Double> colBuocGia;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Ánh xạ dữ liệu cơ bản
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loaiSanPham"));

        // 2. Định dạng cột GIÁ KHỞI ĐIỂM (colGia)
        colGia.setCellValueFactory(new PropertyValueFactory<>("giaKhoiDiem"));
        formatPriceColumn(colGia);

        // 3. Định dạng cột BƯỚC GIÁ (colBuocGia) - MỚI
        colBuocGia.setCellValueFactory(new PropertyValueFactory<>("buocGia"));
        formatPriceColumn(colBuocGia);

        // 4. Định dạng cột THỜI GIAN BẮT ĐẦU (colBatDau) - MỚI
        colBatDau.setCellValueFactory(new PropertyValueFactory<>("thoiGianBatDau"));
        formatDateColumn(colBatDau);

        // 5. Định dạng cột THỜI GIAN KẾT THÚC (colKetThuc)
        colKetThuc.setCellValueFactory(new PropertyValueFactory<>("thoiGianKetThuc"));
        formatDateColumn(colKetThuc);

        // 6. Đổ dữ liệu vào bảng
        if (AuctionData.listSanPham != null) {
            tableItems.setItems(AuctionData.listSanPham);
            tableItems.refresh();
            System.out.println(">>> [HỆ THỐNG ADMIN] Đã tải: " + AuctionData.listSanPham.size() + " vật phẩm.");
        }
    }

    /**
     * Hàm dùng chung để định dạng hiển thị tiền tệ ($1,234.00)
     */
    private void formatPriceColumn(TableColumn<Item, Double> column) {
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

    /**
     * Hàm dùng chung để định dạng hiển thị ngày tháng (dd/MM/yyyy HH:mm)
     */
    private void formatDateColumn(TableColumn<Item, LocalDateTime> column) {
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