# Hệ thống Đấu Giá Trực Tuyến - Nhóm 2

## 1. Mô tả bài toán và phạm vi hệ thống
Hệ thống Đấu Giá Trực Tuyến (Online Auction System) là một ứng dụng Client-Server cho phép người dùng tham gia đấu giá các sản phẩm qua mạng. Hệ thống bao gồm hai thành phần chính:
- **Server**: Quản lý phiên đấu giá, thời gian, kết nối mạng và xử lý logic kết thúc phiên.
- **Client (Người dùng & Quản trị viên)**: Người dùng có thể đăng ký, đăng nhập, nạp tiền, đăng sản phẩm mới, xem danh sách sản phẩm và tham gia đấu giá (đặt giá realtime). Quản trị viên có quyền quản lý người dùng, quản lý đấu giá và xem lịch sử.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
- **Ngôn ngữ**: Java 21
- **Giao diện**: JavaFX (Client)
- **Quản lý project**: Maven
- **Cơ sở dữ liệu**: MySQL (Sử dụng JDBC & Flyway cho Database Migration)
- **Giao tiếp mạng**: Socket Programming & Gson (JSON)
- **Yêu cầu cài đặt**:
  - **Java JDK 21** trở lên.
  - **MySQL Server** đang chạy (Database sẽ được Flyway tự động khởi tạo/migrate).

## 3. Cấu trúc thư mục chính
Dự án được xây dựng theo kiến trúc MVC với các package chính trong thư mục `src/main/java/Team2_CS2_Auction/`:
- `Controller`: Chứa các bộ điều khiển xử lý sự kiện giao diện (JavaFX Controllers).
- `Model`: Định nghĩa các thực thể dữ liệu (User, Product, Auction, ...).
- `Networking`: Logic giao tiếp mạng Client-Server (Socket, JSON payload).
- `Repository`: Lớp truy xuất cơ sở dữ liệu (JDBC).
- `Service`: Chứa logic nghiệp vụ của ứng dụng.
- `Session`: Quản lý phiên đăng nhập hiện tại của người dùng.
- `util`: Các lớp tiện ích (Database connection).
Ngoài ra: `src/main/resources` chứa các file FXML (giao diện), CSS và các file cấu trúc Flyway migration.

## 4. Vị trí các file .jar
Các file thực thi fat JAR (đã bao gồm toàn bộ thư viện như JavaFX, MySQL Connector, Gson...) được build và nằm ở thư mục `target/`:
- **Server**: `target/MyAuctionApp-1.0-SNAPSHOT-server.jar`
- **Client**: `target/MyAuctionApp-1.0-SNAPSHOT-client.jar`

## 5. Hướng dẫn chạy Server/Client theo thứ tự cụ thể
Để hệ thống hoạt động chính xác, **bạn phải chạy Server trước, sau đó mới chạy Client**.

### Bước 1: Khởi động Server
Mở terminal tại thư mục gốc của dự án và chạy lệnh sau:
```bash
java -jar target/MyAuctionApp-1.0-SNAPSHOT-server.jar
```
*Lưu ý: Terminal của Server sẽ in ra địa chỉ IP LAN của máy chủ. Bạn hãy copy hoặc ghi nhớ IP này để Client kết nối.*

### Bước 2: Khởi động Client
Mở một terminal khác (có thể trên cùng một máy hoặc máy tính khác trong mạng) và chạy lệnh sau:
```bash
java -jar target/MyAuctionApp-1.0-SNAPSHOT-client.jar
```
*Lưu ý: Tại màn hình Client, khi được yêu cầu (hoặc trong phần cài đặt kết nối), hãy nhập đúng địa chỉ IP mà Server đã hiển thị.*

### Hướng dẫn tự Build lại file JAR (Dành cho nhà phát triển)
Nếu bạn có thay đổi mã nguồn và muốn tạo lại file JAR, hãy chạy lệnh Maven sau:
```bash
# Trên Windows
.\mvnw.cmd clean package -DskipTests

# Trên Linux/macOS
./mvnw clean package -DskipTests
```

## 6. Danh sách chức năng đã hoàn thành
- [x] Đăng ký, đăng nhập tài khoản (phân quyền Admin / User).
- [x] Nạp tiền vào tài khoản người dùng.
- [x] Giao diện Dashboard cho User và Admin.
- [x] Đăng bán sản phẩm mới.
- [x] Quản lý sản phẩm của tôi.
- [x] Hiển thị danh sách sản phẩm đang đấu giá.
- [x] **Tham gia đấu giá thời gian thực qua mạng (Real-time Bidding)**.
- [x] Xem danh sách các phiên đấu giá đã tham gia.
- [x] (Admin) Quản lý tài khoản người dùng.
- [x] (Admin) Quản lý các phiên đấu giá.
- [x] (Admin) Xem lịch sử toàn hệ thống.

## 7. Link báo cáo PDF và video demo
- **Link báo cáo PDF**: [Gắn link PDF của nhóm bạn tại đây]
- **Link video demo**: [Gắn link Video YouTube/Drive tại đây]
