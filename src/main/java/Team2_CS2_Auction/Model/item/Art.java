package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;   // Tác giả
    private String material; // Chất liệu / Năm sáng tác

    // Constructor cập nhật: Thêm tham số imagePath
    public Art(String id, String ten, String loai, String moTa, double gia, double buoc,
               LocalDateTime batDau, LocalDateTime ketThuc, String imagePath, // <--- THÊM Ở ĐÂY
               String artist, String material) {

        /**
         * super(...) bây giờ nhận 9 tham số để khớp với Item.java mới.
         * Thứ tự phải chuẩn: id, tên, loại, mô tả, giá, bước, bắt đầu, kết thúc, ảnh.
         */
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath);

        this.artist = artist;
        this.material = material;
    }

    // --- CÁC GETTER/SETTER CŨ GIỮ NGUYÊN (KHÔNG XÓA) ---
    public String getArtist() {
        return artist;
    }

    public String getMaterial() {
        return material;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}