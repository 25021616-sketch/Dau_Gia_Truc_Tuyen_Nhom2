package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public abstract class Item {
    private String id;
    private String tenSanPham;
    private String loaiSanPham;
    private String moTa;
    private double giaKhoiDiem;
    private double buocGia;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private String imagePath;

    // SỬA: Thêm tham số imagePath vào Constructor
    public Item(String id, String tenSanPham, String loaiSanPham, String moTa,
                double giaKhoiDiem, double buocGia,
                LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                String imagePath) { // Nhận imagePath từ Factory truyền vào
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.moTa = moTa;
        this.giaKhoiDiem = giaKhoiDiem;
        this.buocGia = buocGia;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.imagePath = imagePath; // Gán giá trị thực tế thay vì để rỗng
    }

    // --- GIỮ NGUYÊN TẤT CẢ GETTER/SETTER CỦA BẠN ---
    public String getId() { return id; }
    public String getTenSanPham() { return tenSanPham; }
    public String getLoaiSanPham() { return loaiSanPham; }
    public String getMoTa() { return moTa; }
    public double getGiaKhoiDiem() { return giaKhoiDiem; }
    public double getBuocGia() { return buocGia; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public String getImagePath() { return imagePath; }

    public void setId(String id) { this.id = id; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public void setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setGiaKhoiDiem(double giaKhoiDiem) { this.giaKhoiDiem = giaKhoiDiem; }
    public void setBuocGia(double buocGia) { this.buocGia = buocGia; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // --- GIỮ NGUYÊN CÁC PHƯƠNG THỨC LOGIC CỦA BẠN ---
    public boolean dangDauGia() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(thoiGianBatDau) && now.isBefore(thoiGianKetThuc);
    }

    public boolean daKetThuc() {
        return LocalDateTime.now().isAfter(thoiGianKetThuc);
    }

    public String getTrangThai() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(thoiGianBatDau)) {
            return "SẮP DIỄN RA";
        } else if (now.isAfter(thoiGianBatDau) && now.isBefore(thoiGianKetThuc)) {
            return "ĐANG DIỄN RA";
        } else {
            return "ĐÃ KẾT THÚC";
        }
    }

    @Override
    public String toString() {
        return "Sản phẩm: " + tenSanPham + " [" + loaiSanPham + "]" +
                "\nGiá hiện tại: " + giaKhoiDiem +
                "\nTrạng thái: " + getTrangThai();
    }
}