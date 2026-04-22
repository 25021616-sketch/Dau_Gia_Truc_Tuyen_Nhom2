package Team2_CS2_Auction.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class Man_hinh_chinh_Users_Controller extends Base_Admin_Controller {

    // Ví dụ dùng với MouseEvent (như hình 1 của bạn)
    @FXML
    public void chuyenSangdanhsachtheodoi(ActionEvent event) {
        switchScene(event, "danh_sach_theo_doi_San_Pham.fxml", "Danh sách theo dõi");
    }

    // Ví dụ chuyển sang lịch sử giao dịch bằng Button (ActionEvent)
    @FXML
    public void handleGoToLichSu(ActionEvent event) {
        switchScene(event, "Lich_su_giao_dich.fxml", "Lịch sử giao dịch");
    }

    @FXML
    public void handleGoToDangNhap(ActionEvent event) {
        switchScene(event, "dang_nhap.fxml", "DANG NHAP");
    }

    @FXML
    public void handleGoTothemsanpham(ActionEvent event) {
        switchScene(event, "them_san_pham.fxml", "them san pham");
    }


}
