package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;

public class VehicleFactory {
    public static Vehicle create(String id, String ten, String loai, String moTa, double gia, double buoc,
                                 LocalDateTime batDau, LocalDateTime ketThuc, String imagePath,
                                 String brand, String year) {

        return new Vehicle(id, ten, loai, moTa, gia, buoc, batDau, ketThuc, imagePath, brand, year);
    }
}