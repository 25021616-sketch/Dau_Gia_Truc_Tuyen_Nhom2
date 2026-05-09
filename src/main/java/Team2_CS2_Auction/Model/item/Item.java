package Team2_CS2_Auction.Model.item;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Item implements Serializable {
    private String id;
    private String tenSanPham;
    private String loaiSanPham;
    private String moTa;
    private List<String> imagePaths;
    private double giaKhoiDiem;
    private double buocGia;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;

    public Item(String id, String tenSanPham, String loaiSanPham, String moTa, List<String> imagePaths) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.moTa = moTa;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    public Item(String id, String tenSanPham, String loaiSanPham, String moTa,
                double giaKhoiDiem, double buocGia,
                LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                String imagePath) {
        this(id, tenSanPham, loaiSanPham, moTa,
                imagePath == null || imagePath.isBlank()
                        ? new ArrayList<>()
                        : new ArrayList<>(List.of(imagePath)));
        this.giaKhoiDiem = giaKhoiDiem;
        this.buocGia = buocGia;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public List<String> getImagePaths() { return new ArrayList<>(imagePaths); }
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    public String getImagePath() {
        return imagePaths != null && !imagePaths.isEmpty() ? imagePaths.get(0) : "";
    }

    public void setImagePath(String imagePath) {
        this.imagePaths = new ArrayList<>();
        if (imagePath != null && !imagePath.isBlank()) {
            this.imagePaths.add(imagePath);
        }
    }

    public double getGiaKhoiDiem() { return giaKhoiDiem; }
    public void setGiaKhoiDiem(double giaKhoiDiem) { this.giaKhoiDiem = giaKhoiDiem; }

    public double getBuocGia() { return buocGia; }
    public void setBuocGia(double buocGia) { this.buocGia = buocGia; }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
}