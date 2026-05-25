package Team2_CS2_Auction.Controller;
import Team2_CS2_Auction.Model.auction.Auction;
import Team2_CS2_Auction.Service.AuctionService;
import Team2_CS2_Auction.Service.AuctionServiceImpl;
import Team2_CS2_Auction.Repository.AuctionRepositoryImpl;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Trang_chu_Admin_Controller
        extends Base_Admin_Controller
        implements Initializable {
    @FXML
    private Button Dashboard;

    @FXML
    private Button history;

    @FXML
    private Button inventory;

    @FXML
    private Button users;

    // Nút mới thêm vào
    @FXML
    private Button btnLogout;

    @FXML
    private Label lblRevenue;

    @FXML
    private Label lblTotalSessions;

    @FXML
    private Label lblTotalUsers;

    @FXML
    private VBox vboxFeaturedProducts;

    private final AuctionRepositoryImpl repo =
            new AuctionRepositoryImpl();
            
    private final AuctionService auctionService = new AuctionServiceImpl();

    @FXML
    public void handleGoToUser(ActionEvent event) {
        // Gõ đúng tên file trong ảnh: Admin_quan_li_User.fxml
        switchScene(event, "Admin_quan_li_User.fxml", "Quản lý người dùng");
    }

    @FXML
    public void handleGoToInventory(ActionEvent event) {
        // Gõ đúng tên file trong ảnh: Admin_quan_li_dau_gia.fxml
        switchScene(event, "Admin_quan_li_dau_gia.fxml", "Quản lý đấu giá");
    }

    @FXML
    public void handleGoToHistory(ActionEvent event) {
        // Mở trang Lịch sử đấu giá
        switchScene(event, "Admin_quan_li_lich_su.fxml", "Lịch sử đấu giá");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Quay lại trang đăng nhập (Tên file: dang_nhap.fxml)
        switchScene(event, "dang_nhap.fxml", "Đăng nhập");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loadDashboardData();
        loadFeaturedProducts();
    }

    private void loadDashboardData() {

        try {

            double revenue =
                    repo.getTotalRevenue();

            int totalSessions =
                    repo.getTotalSessionsOrganized();

            int totalUsers =
                    repo.getTotalUsers();

            // Tổng doanh thu
            lblRevenue.setText(
                    "$" + String.format("%,.0f", revenue)
            );

            // Tổng phiên
            lblTotalSessions.setText(
                    String.format("%,d", totalSessions)
            );

            // Tổng user
            lblTotalUsers.setText(
                    String.format("%,d", totalUsers)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadFeaturedProducts() {
        if (vboxFeaturedProducts == null) return;
        vboxFeaturedProducts.getChildren().clear();
        
        try {
            List<Auction> activeAuctions = auctionService.getActiveAuctions();
            if (activeAuctions == null || activeAuctions.isEmpty()) {
                Label lblEmpty = new Label("Chưa có sản phẩm đấu giá nào đang diễn ra.");
                lblEmpty.setStyle("-fx-font-size: 14; -fx-text-fill: #64748B;");
                vboxFeaturedProducts.getChildren().add(lblEmpty);
                return;
            }
            
            // Lấy tối đa 3 sản phẩm nổi bật
            int count = Math.min(3, activeAuctions.size());
            for (int i = 0; i < count; i++) {
                Auction auction = activeAuctions.get(i);
                
                HBox hbox = new HBox();
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setSpacing(20.0);
                hbox.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 4); -fx-border-color: #E2E8F0; -fx-border-radius: 14;");
                
                Rectangle rect = new Rectangle(75.0, 75.0);
                rect.setArcWidth(12);
                rect.setArcHeight(12);
                rect.setFill(Color.web("#E2E8F0")); // Giữ nguyên placeholder
                
                VBox centerBox = new VBox();
                centerBox.setSpacing(5);
                HBox.setHgrow(centerBox, Priority.ALWAYS);
                
                Label lblCategory = new Label(auction.getItem() != null && auction.getItem().getLoaiSanPham() != null ? auction.getItem().getLoaiSanPham().toUpperCase() : "DANH MỤC");
                lblCategory.setStyle("-fx-text-fill: #B45309; -fx-font-size: 10; -fx-font-weight: bold; -fx-text-transform: uppercase;");
                
                Label lblName = new Label(auction.getItem() != null && auction.getItem().getTenSanPham() != null ? auction.getItem().getTenSanPham() : "Tên sản phẩm");
                lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #0F172A;");
                
                Label lblDesc = new Label(auction.getItem() != null && auction.getItem().getMoTa() != null ? auction.getItem().getMoTa() : "Mô tả sản phẩm");
                lblDesc.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
                
                centerBox.getChildren().addAll(lblCategory, lblName, lblDesc);
                
                VBox rightBox = new VBox();
                rightBox.setAlignment(Pos.CENTER_RIGHT);
                rightBox.setSpacing(5);
                
                Label lblPriceTitle = new Label("MỨC GIÁ THẦU HIỆN TẠI");
                lblPriceTitle.setStyle("-fx-font-size: 11; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
                
                Label lblPrice = new Label(String.format("$%,.0f", auction.getCurrentPrice()));
                lblPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 22; -fx-text-fill: #1E3A8A;");
                
                rightBox.getChildren().addAll(lblPriceTitle, lblPrice);
                
                hbox.getChildren().addAll(rect, centerBox, rightBox);
                vboxFeaturedProducts.getChildren().add(hbox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}