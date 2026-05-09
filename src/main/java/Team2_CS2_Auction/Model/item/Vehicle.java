package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;
import java.util.List;

public class Vehicle extends Item {
    private String thuongHieu;
    private String namSanXuat;

    public Vehicle(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        super(id, ten, loai, moTa, imagePaths);
    }

    public Vehicle(String id, String ten, String loai, String moTa,
                   double giaKhoiDiem, double buocGia,
                   LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                   String imagePath, String thuongHieu, String namSanXuat) {
        super(id, ten, loai, moTa, giaKhoiDiem, buocGia, thoiGianBatDau, thoiGianKetThuc, imagePath);
        this.thuongHieu = thuongHieu;
        this.namSanXuat = namSanXuat;
    }
}