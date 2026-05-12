package Team2_CS2_Auction.Model.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String password;
    private String phone;
    private final UserRole role;
    private LocalDateTime createdAt; // Bỏ final để có thể gán từ DB
    private String status = "ACTIVE"; // THÊM DÒNG NÀY (Mặc định là ACTIVE)

    // ─── Constructors ────────────────────────────────────────────

    protected User(int id, String username, String password,
                   String phone, UserRole role) {
        this.id        = id;
        this.username  = validateNotBlank(username, "username");
        this.password  = validateNotBlank(password, "password");
        this.phone     = phone;
        this.role      = role;
        this.createdAt = LocalDateTime.now();
    }

    protected User(String username, String password, UserRole role) {
        this(0, username, password, null, role);
    }

    // ─── Getters / Setters ───────────────────────────────────────

    // THÊM GETTER VÀ SETTER CHO STATUS TẠI ĐÂY
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() { return id; }

    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("ID không được âm");
        this.id = id;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = validateNotBlank(username, "username");
    }

    public String getPassword() { return password; }

    public void setPassword(String rawPassword) {
        this.password = validateNotBlank(rawPassword, "password");
    }

    public boolean checkPassword(String rawPassword) {
        return this.password.equals(rawPassword);
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // THÊM SETTER CHO CREATEDAT (để UserRepository có thể gán dữ liệu từ DB)
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ─── Abstract method ─────────────────────────────────────────

    public abstract String getInfo();

    private static String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " không được để trống");
        return value.trim();
    }

    // ─── Object overrides ────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != 0 && id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s, createdAt=%s, status=%s}",
                id, username, role, createdAt, status);
    }
}