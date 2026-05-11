package Team2_CS2_Auction.Model.item;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class Item implements Serializable {
    private String id;
    private String tenSanPham;
    private String loaiSanPham;
    private String moTa;
    private String imagePath; // Đã đổi từ List thành String duy nhất

    private double giaKhoiDiem;
    private double buocGia;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    // Constructor cập nhật lại tham số imagePath
    public Item(String id, String tenSanPham, String loaiSanPham, String moTa, String imagePath) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.moTa = moTa;
        this.imagePath = imagePath;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getGiaKhoiDiem() { return giaKhoiDiem; }
    public void setGiaKhoiDiem(double gia) { this.giaKhoiDiem = gia; }

    public double getBuocGia() { return buocGia; }
    public void setBuocGia(double buoc) { this.buocGia = buoc; }

    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDateTime start) { this.ngayBatDau = start; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime end) { this.ngayKetThuc = end; }
}