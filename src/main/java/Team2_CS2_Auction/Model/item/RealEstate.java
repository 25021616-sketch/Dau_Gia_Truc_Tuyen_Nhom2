package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class RealEstate extends Item {
    private String address;
    private double area;
    private String legal;

    // Constructor cải tiến: Nhận tham số 'loai' để truyền từ Controller vào
    public RealEstate(String id, String ten, String loai, String moTa, double gia, double buoc,
                      LocalDateTime batDau, LocalDateTime ketThuc,
                      String address, double area, String legal) {

        // Gọi Constructor cha (Item) - Đảm bảo khớp 100% với 8 tham số của Item.java
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc);

        this.address = address;
        this.area = area;
        this.legal = legal;
    }

    // --- GETTERS ---
    public String getAddress() {
        return address;
    }

    public double getArea() {
        return area;
    }

    public String getLegal() {
        return legal;
    }

    // --- SETTERS ---
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