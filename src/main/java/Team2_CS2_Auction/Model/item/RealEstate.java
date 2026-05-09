package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;
import java.util.List;

public class RealEstate extends Item {
    private String diaChi;
    private double dienTich;
    private String loaiBatDongSan;

    public RealEstate(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        super(id, ten, loai, moTa, imagePaths);
    }

    public RealEstate(String id, String ten, String loai, String moTa,
                      double giaKhoiDiem, double buocGia,
                      LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                      String imagePath, String diaChi, double dienTich, String loaiBatDongSan) {
        super(id, ten, loai, moTa, giaKhoiDiem, buocGia, thoiGianBatDau, thoiGianKetThuc, imagePath);
        this.diaChi = diaChi;
        this.dienTich = dienTich;
        this.loaiBatDongSan = loaiBatDongSan;
    }
}