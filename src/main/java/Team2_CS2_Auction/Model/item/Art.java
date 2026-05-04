package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;   // Họa sĩ / Nghệ nhân / Tác giả
    private String material; // Chất liệu / Năm sáng tác (tùy biến)

    // Constructor cải tiến
    public Art(String id, String ten, String loai, String moTa, double gia, double buoc,
               LocalDateTime batDau, LocalDateTime ketThuc,
               String artist, String material) {

        /**
         * CHÚ Ý: super(...) phải khớp chính xác với số lượng và thứ tự tham số
         * trong Constructor của class Item.
         * Ở đây là 8 tham số (id, ten, loai, moTa, gia, buoc, batDau, ketThuc).
         */
        super(id, ten, loai, moTa, gia, buoc, batDau, ketThuc);

        this.artist = artist;
        this.material = material;
    }

    // --- GETTERS ---
    public String getArtist() {
        return artist;
    }

    public String getMaterial() {
        return material;
    }

    // --- SETTERS ---
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}