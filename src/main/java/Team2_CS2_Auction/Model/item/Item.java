package Team2_CS2_Auction.Model.item;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Item implements Serializable {
    private String id;
    private String tenSanPham;
    private String loaiSanPham;
    private String moTa;
    private List<String> imagePaths;


    public Item(String id, String tenSanPham, String loaiSanPham, String moTa, List<String> imagePaths) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.moTa = moTa;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }



    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public List<String> getImagePaths() { return new ArrayList<>(imagePaths); }
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }
}