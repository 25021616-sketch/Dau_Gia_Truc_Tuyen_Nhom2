package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;
import java.util.List;

public class Electronics extends Item {
    private String hangSanXuat;
    private String congSuat;

    public Electronics(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        super(id, ten, loai, moTa, imagePaths);
    }

    public Electronics(String id, String ten, String loai, String moTa,
                       double giaKhoiDiem, double buocGia,
                       LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                       String imagePath, String hangSanXuat, String congSuat) {
        super(id, ten, loai, moTa, giaKhoiDiem, buocGia, thoiGianBatDau, thoiGianKetThuc, imagePath);
        this.hangSanXuat = hangSanXuat;
        this.congSuat = congSuat;
    }
}