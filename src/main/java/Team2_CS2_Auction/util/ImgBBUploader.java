package Team2_CS2_Auction.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class ImgBBUploader {

    private static final String API_KEY = "cff2a80229e9ad1ae215817e29ab9346";
    private static final String API_URL = "https://api.imgbb.com/1/upload";

    public static String uploadImage(File file) {

        try {

            byte[] imageBytes = Files.readAllBytes(file.toPath());

            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            String body = "key=" + API_KEY
                    + "&image=" + URLEncoder.encode(base64, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("STATUS: " + response.statusCode());
            System.out.println("BODY: " + response.body());

            JsonObject jsonObject =
                    JsonParser.parseString(response.body()).getAsJsonObject();

            boolean success = jsonObject.get("success").getAsBoolean();

            if (!success) {
                return null;
            }

            return jsonObject
                    .getAsJsonObject("data")
                    .get("url")
                    .getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}