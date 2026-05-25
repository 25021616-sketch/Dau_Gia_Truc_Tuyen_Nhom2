package Team2_CS2_Auction.Model.auction;

public class AutoBid {
    private int id;
    private int userId;
    private int productId;
    private int stepMultiplier;
    private double maxLimit;
    private boolean active;
    
    // Thuộc tính phụ lấy từ JOIN bảng user để kiểm tra số dư
    private double balance;

    // Constructors
    public AutoBid() {
    }

    public AutoBid(int userId, int productId, int stepMultiplier, double maxLimit, boolean active) {
        this.userId = userId;
        this.productId = productId;
        this.stepMultiplier = stepMultiplier;
        this.maxLimit = maxLimit;
        this.active = active;
    }

    public AutoBid(int id, int userId, int productId, int stepMultiplier, double maxLimit, boolean active, double balance) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.stepMultiplier = stepMultiplier;
        this.maxLimit = maxLimit;
        this.active = active;
        this.balance = balance;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getStepMultiplier() {
        return stepMultiplier;
    }

    public void setStepMultiplier(int stepMultiplier) {
        this.stepMultiplier = stepMultiplier;
    }

    public double getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(double maxLimit) {
        this.maxLimit = maxLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AutoBid{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", stepMultiplier=" + stepMultiplier +
                ", maxLimit=" + maxLimit +
                ", active=" + active +
                ", balance=" + balance +
                '}';
    }
}
