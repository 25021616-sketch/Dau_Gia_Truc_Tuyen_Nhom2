package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;
import java.util.List;

public class Art extends Item {
    private String tacGia;
    private String namSangTac;

    public Art(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        super(id, ten, loai, moTa, imagePaths);
    }

    public Art(String id, String ten, String loai, String moTa,
               double giaKhoiDiem, double buocGia,
               LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
               String imagePath, String tacGia, String namSangTac) {
        super(id, ten, loai, moTa, giaKhoiDiem, buocGia, thoiGianBatDau, thoiGianKetThuc, imagePath);
        this.tacGia = tacGia;
        this.namSangTac = namSangTac;
    }
}