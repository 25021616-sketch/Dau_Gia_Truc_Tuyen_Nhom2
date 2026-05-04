package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class ArtFactory {
    public static Art create(String id, String ten, String loai, String moTa, double gia, double buoc,
                             LocalDateTime batDau, LocalDateTime ketThuc, String imagePath,
                             String artist, String material) {

        return new Art(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath, artist, material);
    }
}