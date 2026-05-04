package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String manufacturer; // Hãng sản xuất / Thương hiệu
    private String modelYear;    // Đời xe / Năm sản xuất

    // Constructor cập nhật: Nhận thêm tham số imagePath
    public Vehicle(String id, String ten, String loai, String moTa, double gia, double buoc,
                   LocalDateTime batDau, LocalDateTime ketThuc, String imagePath, // <-- THÊM Ở ĐÂY
                   String manufacturer, String modelYear) {

        /**
         * super(...) phải truyền đủ 9 tham số để khớp với Item.java mới:
         * id, tên, loại, mô tả, giá khởi điểm, bước giá, bắt đầu, kết thúc, ảnh.
         */
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath);

        this.manufacturer = manufacturer;
        this.modelYear = modelYear;
    }

    // --- CÁC GETTER/SETTER CỦA BẠN (GIỮ NGUYÊN 100%) ---
    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelYear() {
        return modelYear;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }
}