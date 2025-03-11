package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RewardService {
    
    Reward getById(Long id);
    
    RewardDTO.Response create(RewardDTO.CreateRequest request);
    
    RewardDTO.Response update(Long id, RewardDTO.UpdateRequest request);
    
    void delete(Long id);
    
    RewardDTO.Response findById(Long id);
    
    List<RewardDTO.Response> findAllByEventId(Long eventId);
    
    Page<RewardDTO.Response> findAll(Pageable pageable);
    
    List<RewardDTO.Summary> findAllActiveByEventId(Long eventId);
    
    void updateRemainingQuantity(Long id, Integer quantity);
    
    void decrementRemainingQuantityById(Long id);
    
    boolean hasAvailableQuantity(Long id);
    
    void validateReward(Long eventId, Long rewardId);
}
