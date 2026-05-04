package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class RealEstateFactory {
    public static RealEstate create(String id, String ten, String loai, String moTa, double gia, double buoc,
                                    LocalDateTime batDau, LocalDateTime ketThuc, String imagePath,
                                    String address, double area, String legal) {

        return new RealEstate(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath, address, area, legal);
    }
}