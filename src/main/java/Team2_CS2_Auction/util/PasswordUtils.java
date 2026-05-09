package Team2_CS2_Auction.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String hashSha256(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Mật khẩu không được null");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không thể hash mật khẩu", e);
        }
    }
}
