package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class Lich_su_giao_dich_Controller extends Base_Admin_Controller {

    @FXML
    public void handleQuayLaiTrangChu(ActionEvent event) {switchScene(event, "Man_hinh_chinh_Users.fxml", "Màn hình chính");}
    @FXML
    public void chuyenSangdanhsachtheodoi(ActionEvent event) {switchScene(event, "danh_sach_theo_doi_San_Pham.fxml", "Danh sách theo dõi");}
    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "them san pham");
    }
    @FXML
    public void handleGoToSanPhamCuaToi(ActionEvent event) {switchScene(event, "san_pham_cua_toi.fxml", "Sản phẩm của tôi");}
}