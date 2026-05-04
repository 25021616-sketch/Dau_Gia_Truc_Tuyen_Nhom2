package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class RealEstate extends Item {
    private String address;
    private double area;
    private String legal;

    // Constructor cập nhật: Nhận thêm tham số imagePath
    public RealEstate(String id, String ten, String loai, String moTa, double gia, double buoc,
                      LocalDateTime batDau, LocalDateTime ketThuc, String imagePath, // <-- THÊM Ở ĐÂY
                      String address, double area, String legal) {

        /**
         * super(...) phải truyền đủ 9 tham số để khớp với Item.java mới:
         * id, tên, loại, mô tả, giá khởi điểm, bước giá, bắt đầu, kết thúc, ảnh.
         */
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath);

        this.address = address;
        this.area = area;
        this.legal = legal;
    }

    // --- CÁC GETTER/SETTER CỦA BẠN (GIỮ NGUYÊN 100%) ---
    public String getAddress() {
        return address;
    }

    public double getArea() {
        return area;
    }

    public String getLegal() {
        return legal;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public void setLegal(String legal) {
        this.legal = legal;
    }
}