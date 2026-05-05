package Team2_CS2_Auction.Service;

import Team2_CS2_Auction.Repository.AuctionRepository;

public class AdminServiceImpl implements AdminService {

    private final AuctionRepository auctionRepository;

    public AdminServiceImpl(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Override
    public void approveAuction(String auctionId) throws Exception {
        auctionRepository.updateStatus(auctionId, "APPROVED");
    }

    @Override
    public void rejectAuction(String auctionId, String reason) throws Exception {
        auctionRepository.updateStatus(auctionId, "REJECTED");
    }
}