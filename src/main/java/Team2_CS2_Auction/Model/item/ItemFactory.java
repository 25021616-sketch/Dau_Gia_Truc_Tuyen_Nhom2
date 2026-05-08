package Team2_CS2_Auction.Model.item;

import java.util.List;

public class ItemFactory {
    public static Item createItem(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        return switch (loai) {
            case "Đồ điện tử" -> new Electronics(id, ten, loai, moTa, imagePaths);
            case "Sách", "Tác phẩm nghệ thuật" -> new Art(id, ten, loai, moTa, imagePaths);
            case "Bất động sản" -> new RealEstate(id, ten, loai, moTa, imagePaths);
            case "Xe hơi" -> new Vehicle(id, ten, loai, moTa, imagePaths);
            default -> new Other(id, ten, loai, moTa, imagePaths); // Mặc định là Other
        };
    }
}