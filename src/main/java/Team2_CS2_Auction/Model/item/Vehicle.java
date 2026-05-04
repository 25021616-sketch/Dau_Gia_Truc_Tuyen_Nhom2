package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String manufacturer; // Hãng sản xuất / Thương hiệu
    private String modelYear;    // Đời xe / Năm sản xuất

    // Constructor cải tiến: Nhận tham số 'loai' để phân biệt Xe hơi/Đồng hồ/Trang sức
    public Vehicle(String id, String ten, String loai, String moTa, double gia, double buoc,
                   LocalDateTime batDau, LocalDateTime ketThuc,
                   String manufacturer, String modelYear) {

        // Gọi Constructor cha (Item) - Thứ tự chuẩn 8 tham số: id, ten, loai, moTa, gia, buoc, batDau, ketThuc
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc);

        this.manufacturer = manufacturer;
        this.modelYear = modelYear;
    }

    // --- GETTERS ---
    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelYear() {
        return modelYear;
    }

    // --- SETTERS ---
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }
}