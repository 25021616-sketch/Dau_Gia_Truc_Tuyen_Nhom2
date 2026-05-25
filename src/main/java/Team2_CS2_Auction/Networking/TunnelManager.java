package Team2_CS2_Auction.Networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Quản lý tự động tạo đường truyền Internet (Tunnel) bằng SSH + Pinggy ngay trong code Java.
 * Giảng viên và trợ giảng chỉ cần chạy ServerMain.java, hệ thống sẽ tự thiết lập kết nối internet
 * và in ra địa chỉ kết nối cho các máy khách mà không cần cài đặt hay chạy phần mềm bên ngoài.
 */
public class TunnelManager {

    private static Process sshProcess;

    public static void startTunnelAsync() {
        Thread tunnelThread = new Thread(() -> {
            try {
                // 1. Tự động kiểm tra và tạo SSH Key nếu chưa có
                ensureSshKeyExists();

                // 2. Thiết lập lệnh SSH kết nối tới Pinggy
                String userHome = System.getProperty("user.home");
                Path keyPath = Paths.get(userHome, ".ssh", "id_rsa");
                
                String keyArg = keyPath.toFile().exists() ? keyPath.toString() : null;

                ProcessBuilder pb;
                if (keyArg != null) {
                    pb = new ProcessBuilder("ssh", 
                            "-o", "StrictHostKeyChecking=no", 
                            "-p", "443", 
                            "-i", keyArg, 
                            "-R0:localhost:8080", 
                            "tcp@a.pinggy.io");
                } else {
                    pb = new ProcessBuilder("ssh", 
                            "-o", "StrictHostKeyChecking=no", 
                            "-p", "443", 
                            "-R0:localhost:8080", 
                            "tcp@a.pinggy.io");
                }

                pb.redirectErrorStream(true);
                sshProcess = pb.start();

                // 3. Đọc dữ liệu đầu ra từ tiến trình SSH để tìm địa chỉ IP công cộng
                BufferedReader reader = new BufferedReader(new InputStreamReader(sshProcess.getInputStream()));
                String line;
                boolean foundUrl = false;

                while ((line = reader.readLine()) != null) {
                    // Dò tìm chuỗi chứa link pinggy
                    if (line.contains("pinggy") && line.contains(".link:") && !foundUrl) {
                        // Tách lấy địa chỉ URL sạch
                        String url = extractPinggyUrl(line);
                        if (url != null) {
                            foundUrl = true;
                            printTunnelBanner(url);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("[Tunnel] Không thể tự động tạo đường truyền SSH: " + e.getMessage());
                System.out.println("[Tunnel] Gợi ý: Giảng viên và trợ giảng kết nối chung mạng Wi-Fi để tự động nhận diện IP LAN.");
            }
        });

        tunnelThread.setDaemon(true);
        tunnelThread.start();
    }

    /** Đảm bảo khóa SSH tồn tại để tránh hỏi mật khẩu */
    private static void ensureSshKeyExists() {
        try {
            String userHome = System.getProperty("user.home");
            Path sshDir = Paths.get(userHome, ".ssh");
            Path keyFile = sshDir.resolve("id_rsa");

            if (!Files.exists(keyFile)) {
                System.out.println("[Tunnel] Đang khởi tạo khóa bảo mật SSH trên hệ thống...");
                if (!Files.exists(sshDir)) {
                    Files.createDirectories(sshDir);
                }

                ProcessBuilder pb = new ProcessBuilder(
                        "ssh-keygen", "-t", "rsa", "-b", "2048", 
                        "-f", keyFile.toString(), "-N", ""
                );
                Process p = pb.start();
                p.waitFor();
                System.out.println("[Tunnel] Khởi tạo khóa bảo mật thành công!");
            }
        } catch (Exception ignored) {}
    }

    /** Trích xuất địa chỉ URL Pinggy từ dòng output */
    private static String extractPinggyUrl(String line) {
        try {
            // Định dạng dòng có thể chứa màu hoặc chuỗi thừa, lọc lấy đoạn chứa pinggy
            int index = line.indexOf(".link:");
            if (index == -1) return null;
            
            // Tìm điểm bắt đầu của host
            int start = index;
            while (start > 0 && !Character.isWhitespace(line.charAt(start)) && line.charAt(start) != '\u001b') {
                start--;
            }
            if (line.charAt(start) == '\u001b' || Character.isWhitespace(line.charAt(start))) {
                start++;
            }
            
            // Tìm điểm kết thúc của port
            int end = index + 6;
            while (end < line.length() && Character.isDigit(line.charAt(end))) {
                end++;
            }
            
            String rawUrl = line.substring(start, end).trim();
            // Lọc bỏ ký tự đặc biệt của màu ANSI nếu có
            return rawUrl.replaceAll("\u001B\\[[;\\d]*m", "");
        } catch (Exception e) {
            return null;
        }
    }

    /** In ra banner thông báo đẹp đẽ trên Console Server */
    private static void printTunnelBanner(String url) {
        System.out.println("\n");
        System.out.println("==========================================================================");
        System.out.println("  🌐 ĐƯỜNG TRUYỀN INTERNET TỰ ĐỘNG CHO TRỢ GIẢNG & GIẢNG VIÊN");
        System.out.println("==========================================================================");
        System.out.println("  Các máy khách (Client) khác chỉ cần nhập địa chỉ sau để kết nối:");
        System.out.println("  ");
        System.out.println("  👉  " + url);
        System.out.println("  ");
        System.out.println("  (Không cần cấu hình mạng, không cần chung Wi-Fi, không cần quyền Admin)");
        System.out.println("==========================================================================\n");
    }

    /** Đóng kết nối khi Server tắt */
    public static void stopTunnel() {
        if (sshProcess != null && sshProcess.isAlive()) {
            sshProcess.destroy();
        }
    }
}
