package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private String power;

    // Constructor cải tiến: Nhận thêm 'loai' để Controller có thể truyền vào linh hoạt
    public Electronics(String id, String ten, String loai, String moTa, double gia, double buoc,
                       LocalDateTime batDau, LocalDateTime ketThuc, String brand, String power) {

        // Gọi Constructor của lớp cha Item
        // Đảm bảo truyền đúng 8 tham số mà class Item đang yêu cầu
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc);

        this.brand = brand;
        this.power = power;
    }

    // --- GETTERS ---
    public String getBrand() {
        return brand;
    }

    public String getPower() {
        return power;
    }

    // --- SETTERS ---
    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPower(String power) {
        this.power = power;
    }
}