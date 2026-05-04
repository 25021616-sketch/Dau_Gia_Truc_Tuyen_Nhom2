package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private String power;

    // Constructor cập nhật: Thêm tham số imagePath vào danh sách nhận vào
    public Electronics(String id, String ten, String loai, String moTa, double gia, double buoc,
                       LocalDateTime batDau, LocalDateTime ketThuc, String imagePath, // <-- THÊM Ở ĐÂY
                       String brand, String power) {

        /**
         * super(...) phải truyền đủ 9 tham số để khớp với Item.java mới nhất:
         * id, tên, loại, mô tả, giá khởi điểm, bước giá, bắt đầu, kết thúc, đường dẫn ảnh.
         */
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath);

        this.brand = brand;
        this.power = power;
    }

    // --- CÁC GETTER/SETTER CỦA BẠN (GIỮ NGUYÊN) ---
    public String getBrand() {
        return brand;
    }

    public String getPower() {
        return power;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPower(String power) {
        this.power = power;
    }
}