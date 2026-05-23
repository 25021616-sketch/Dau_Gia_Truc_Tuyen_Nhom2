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
        colID.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAuctionId()
                )
        );

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
        colGia.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getCurrentPrice()
                )
        );
        formatPriceColumn(colGia);

        // 7. Bước giá (Lấy từ biến stepPrice trong Auction)
        colBuocGia.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getStepPrice()
                )
        );
        formatPriceColumn(colBuocGia);

        // 8. Ngày bắt đầu (Lấy từ Auction)
        colBatDau.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getStartTime()
                )
        );

        formatDateColumn(colBatDau);

        // 9. Ngày kết thúc (Lấy từ Auction)
        colKetThuc.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getEndTime()
                )
        );

        formatDateColumn(colKetThuc);
    }

    private void setupActionButtons() {

        colThaoTac.setCellFactory(param -> new TableCell<>() {

            // Bỏ text, để nút trống để nhét Icon vào
            private final Button btnApprove = new Button();
            private final Button btnReject = new Button();
            private final HBox container = new HBox(10, btnApprove, btnReject);

            {
                // ===== TẠO ICON TÍCH XANH =====
                javafx.scene.shape.SVGPath checkIcon = new javafx.scene.shape.SVGPath();
                checkIcon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
                checkIcon.setFill(javafx.scene.paint.Color.WHITE);
                btnApprove.setGraphic(checkIcon);

                // ===== TẠO ICON X ĐỎ =====
                javafx.scene.shape.SVGPath crossIcon = new javafx.scene.shape.SVGPath();
                crossIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
                crossIcon.setFill(javafx.scene.paint.Color.WHITE);
                btnReject.setGraphic(crossIcon);

                // ===== STYLE NÚT DUYỆT (Giữ nguyên màu nền, ép khung vuông) =====
                btnApprove.setStyle(
                        "-fx-background-color: #68D391;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 8;" +
                                "-fx-min-width: 32px;" +
                                "-fx-min-height: 32px;"
                );

                // ===== STYLE NÚT TỪ CHỐI (Giữ nguyên màu nền, ép khung vuông) =====
                btnReject.setStyle(
                        "-fx-background-color: #FC8181;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 8;" +
                                "-fx-min-width: 32px;" +
                                "-fx-min-height: 32px;"
                );

                // ===== ACTION DUYỆT =====
                btnApprove.setOnAction(e -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    handleApproveAction(auction);
                });

                // ===== ACTION TỪ CHỐI =====
                btnReject.setOnAction(e -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    handleRejectAction(auction);
                });

                container.setAlignment(javafx.geometry.Pos.CENTER);
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
            auctionService.rejectAuction(auction.getAuctionId());
            new Alert(Alert.AlertType.INFORMATION, "Đã từ chối sản phẩm!").show();
            loadData();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).show();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Quay lại trang đăng nhập (Tên file: dang_nhap.fxml)
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
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