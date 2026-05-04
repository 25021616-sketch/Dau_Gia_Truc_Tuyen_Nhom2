package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class ElectronicsFactory {
    public static Electronics create(String id, String ten, String loai, String moTa, double gia, double buoc,
                                     LocalDateTime batDau, LocalDateTime ketThuc, String imagePath,
                                     String brand, String power) {

        return new Electronics(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath, brand, power);
    }
}