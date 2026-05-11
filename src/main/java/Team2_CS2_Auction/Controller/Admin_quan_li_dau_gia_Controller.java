package Team2_CS2_Auction.Controller;

import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class Admin_quan_li_dau_gia_Controller extends Base_Admin_Controller implements Initializable {

    @FXML private TableView<Auction> tableItems;
    @FXML private TableColumn<Auction, Void> colSTT;
    @FXML private TableColumn<Auction, String> colID;
    @FXML private TableColumn<Auction, String> colTen;
    @FXML private TableColumn<Auction, String> colChuSoHuu;
    @FXML private TableColumn<Auction, String> colLoai;
    @FXML private TableColumn<Auction, Double> colGia;
    @FXML private TableColumn<Auction, Double> colBuocGia;
    @FXML private TableColumn<Auction, LocalDateTime> colBatDau;
    @FXML private TableColumn<Auction, LocalDateTime> colKetThuc;
    @FXML private TableColumn<Auction, Void> colThaoTac;

    private final AuctionService auctionService = new AuctionServiceImpl();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupActionButtons();
        loadData();
    }

    private void setupTableColumns() {
        // 1. STT tự động (Giữ nguyên)
        colSTT.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        // 2. ID Sản phẩm (Lấy từ Auction.getAuctionId())
        colID.setCellValueFactory(new PropertyValueFactory<>("auctionId"));

        // 3. Tên vật phẩm (Lấy từ Item)
        colTen.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getItem().getTenSanPham()));

        // 4. Người đăng (Lấy từ Member seller)
        colChuSoHuu.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeller().getUsername()));

        // 5. Loại (Lấy từ Item)
        colLoai.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getItem().getLoaiSanPham()));

        // 6. Giá khởi điểm (Lấy từ biến currentPrice trong Auction)
        // Vì trong AuctionRepositoryImpl bạn gán: auction.setCurrentPrice(rs.getDouble("start_price"))
        colGia.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        formatPriceColumn(colGia);

        // 7. Bước giá (Lấy từ biến stepPrice trong Auction)
        colBuocGia.setCellValueFactory(new PropertyValueFactory<>("stepPrice"));
        formatPriceColumn(colBuocGia);

        // 8. Ngày bắt đầu (Lấy từ Auction)
        colBatDau.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        formatDateColumn(colBatDau);

        // 9. Ngày kết thúc (Lấy từ Auction)
        colKetThuc.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        formatDateColumn(colKetThuc);
    }
    private void setupActionButtons() {

        colThaoTac.setCellFactory(param -> new TableCell<>() {

            private final Button btnApprove =
                    new Button("Duyệt");

            private final Button btnReject =
                    new Button("Từ chối");

            private final HBox container =
                    new HBox(10, btnApprove, btnReject);

            {

                // ===== STYLE NÚT DUYỆT =====

                btnApprove.setStyle(
                        "-fx-background-color: #68D391;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 8;"
                );

                // ===== STYLE NÚT TỪ CHỐI =====

                btnReject.setStyle(
                        "-fx-background-color: #FC8181;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 8;"
                );

                // ===== ACTION DUYỆT =====

                btnApprove.setOnAction(e -> {

                    Auction auction =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    handleApproveAction(auction);
                });

                // ===== ACTION TỪ CHỐI =====

                btnReject.setOnAction(e -> {

                    Auction auction =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    handleRejectAction(auction);
                });

                container.setAlignment(
                        javafx.geometry.Pos.CENTER
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : container);
            }
        });
    }


    private void loadData() {
        try {
            List<Auction> pendingList = auctionService.getPendingAuctions();
            // Ép kiểu FXCollections.observableList để tránh lỗi Ambiguous
            tableItems.setItems(FXCollections.observableList(pendingList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleApproveAction(Auction auction) {
        try {
            // Đảm bảo Auction Model có hàm getAuctionId()
            auctionService.approveAuction(auction.getAuctionId());
            new Alert(Alert.AlertType.INFORMATION, "Đã duyệt sản phẩm thành công!").show();
            loadData();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
    }

    private void handleRejectAction(Auction auction) {

        try {

            auctionService.rejectAuction(
                    auction.getAuctionId()
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Đã từ chối sản phẩm!"
            ).show();

            loadData();

        } catch (Exception e) {

            new Alert(
                    Alert.AlertType.ERROR,
                    "Lỗi: " + e.getMessage()
            ).show();
        }
    }

    private void formatPriceColumn(TableColumn<Auction, Double> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText((empty || price == null) ? null : String.format("$%,.0f", price));
            }
        });
    }

    private void formatDateColumn(TableColumn<Auction, LocalDateTime> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText((empty || date == null) ? null : formatter.format(date));
            }
        });
    }

    @FXML public void handleGoToDashboard(ActionEvent event) { switchScene(event, "Trang_chu_Admin.fxml", "Bảng điều khiển"); }
    @FXML public void handleGoToUsers(ActionEvent event) { switchScene(event, "Admin_quan_li_User.fxml", "Quản lý người dùng"); }
    @FXML public void handleGoToHistory(ActionEvent event) { switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá"); }
    @FXML public void handleAddNewListing(ActionEvent event) { switchScene(event, "Trang_chu_Admin.fxml", "Bảng điều khiển"); }
}