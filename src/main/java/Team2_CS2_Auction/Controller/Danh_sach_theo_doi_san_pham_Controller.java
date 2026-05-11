package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class Danh_sach_theo_doi_san_pham_Controller extends Base_Admin_Controller {

    @FXML
    public void handleQuayLaiTrangChu(ActionEvent event) {
        switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính");
    }

    @FXML
    public void handleGoToLichSu(ActionEvent event) {
        switchScene(event, "Lich_su_giao_dich.fxml", "Lịch sử giao dịch");
    }
    @FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) {
        switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi");
    }
    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "them san pham");
    }
}