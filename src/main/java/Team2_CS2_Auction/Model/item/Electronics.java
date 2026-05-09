package Team2_CS2_Auction.Model.item;

import java.time.LocalDateTime;
import java.util.List;

public class Electronics extends Item {

    public Electronics(String id, String ten, String loai, String moTa, List<String> imagePaths) {
        super(id, ten, loai, moTa, imagePaths);
    }

}