package Team2_CS2_Auction.util;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgBBUploader {

    // API Key của ImgBB mà bạn vừa tạo
    private static final String API_KEY = "c9f2fa5e2e9858fcca405f77d8e3d4b1";
    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";

    /**
     * Upload ảnh lên ImgBB và trả về một đường link public.
     * @param file File ảnh ở dưới máy tính
     * @return Link URL của ảnh (vd: https://i.ibb.co/xyz/image.png)
     */
    public static String uploadImage(File file) {
        try {
            // 1. Đọc toàn bộ nội dung file ảnh thành mảng byte
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // 2. Mã hóa sang Base64
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            // 3. Mã hóa URL-Encode để ghép vào chuỗi HTTP
            String encodedImage = java.net.URLEncoder.encode(base64Image, "UTF-8");

            // 4. Cấu hình gói dữ liệu gửi đi
            String formData = "key=" + API_KEY + "&image=" + encodedImage;

            // 5. Mở kết nối HttpClient (Chuẩn của Java 11+)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IMGBB_API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            // 6. Gửi request và nhận kết quả JSON
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Thành công, lấy chuỗi JSON trả về
                String json = response.body();

                // 7. Dùng Regex để tách lấy cái link ảnh trong JSON mà không cần xài thư viện ngoài
                Pattern pattern = Pattern.compile("\"url\":\"(https?:\\\\?/\\\\?/[^\"]+)\"");
                Matcher matcher = pattern.matcher(json);
                if (matcher.find()) {
                    // Sửa lại các dấu gạch chéo cho đúng chuẩn
                    String imageUrl = matcher.group(1).replace("\\/", "/");
                    return imageUrl;
                }
            } else {
                System.err.println("Lỗi từ ImgBB (Status " + response.statusCode() + "): " + response.body());
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối đến mạng để upload: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Trả về null nếu lỗi
    }
}
