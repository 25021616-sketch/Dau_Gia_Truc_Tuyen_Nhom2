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
    // --- BỔ SUNG: Thuộc tính lưu đường dẫn ảnh ---
    private String imagePath;

    // Constructor đầy đủ (Đã cập nhật để nhận imagePath)
    public Item(String id, String tenSanPham, String loaiSanPham, String moTa,
                double giaKhoiDiem, double buocGia,
                LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.moTa = moTa;
        this.giaKhoiDiem = giaKhoiDiem;
        this.buocGia = buocGia;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.imagePath = ""; // Khởi tạo mặc định là chuỗi rỗng
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getTenSanPham() { return tenSanPham; }
    public String getLoaiSanPham() { return loaiSanPham; }
    public String getMoTa() { return moTa; }
    public double getGiaKhoiDiem() { return giaKhoiDiem; }
    public double getBuocGia() { return buocGia; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    // --- BỔ SUNG: Getter cho ảnh ---
    public String getImagePath() { return imagePath; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public void setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setGiaKhoiDiem(double giaKhoiDiem) { this.giaKhoiDiem = giaKhoiDiem; }
    public void setBuocGia(double buocGia) { this.buocGia = buocGia; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    // --- BỔ SUNG: Setter cho ảnh ---
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // --- PHƯƠNG THỨC BỔ TRỢ (UTILITIES) ---

    /**
     * Kiểm tra xem phiên đấu giá hiện tại có đang diễn ra hay không
     */
    public boolean dangDauGia() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(thoiGianBatDau) && now.isBefore(thoiGianKetThuc);
    }

    /**
     * Kiểm tra xem phiên đấu giá đã kết thúc chưa
     */
    public boolean daKetThuc() {
        return LocalDateTime.now().isAfter(thoiGianKetThuc);
    }

    @Override
    public String toString() {
        return "Sản phẩm: " + tenSanPham + " [" + loaiSanPham + "]" +
                "\nGiá khởi điểm: " + giaKhoiDiem +
                "\nBắt đầu: " + thoiGianBatDau +
                "\nKết thúc: " + thoiGianKetThuc +
                "\nĐường dẫn ảnh: " + imagePath;
    }
    /**
     * Cho nó bắt thời gian để chuyển sang trạng thái đúng của nó
     */
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
}
