package Team2_CS2_Auction.Model.user;

import Team2_CS2_Auction.Service.AdminService;

public class Admin extends User {

    private transient AdminService adminService;

    public Admin(int id,
                 String username,
                 String password,
                 String phone) {

        super(id, username, password, phone, UserRole.ADMIN);
    }

    public Admin(String username, String password) {
        super(username, password, UserRole.ADMIN);
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void approveAuction(String auctionId) throws Exception {
        if (adminService == null) {
            throw new Exception("Chưa kết nối AdminService");
        }

        adminService.approveAuction(auctionId);
    }

    public void rejectAuction(String auctionId, String reason) throws Exception {
        if (adminService == null) {
            throw new Exception("Chưa kết nối AdminService");
        }

        adminService.rejectAuction(auctionId, reason);
    }

    @Override
    public String getInfo() {
        return String.format("Admin{id=%d, username='%s'}", getId(), getUsername());
    }
}