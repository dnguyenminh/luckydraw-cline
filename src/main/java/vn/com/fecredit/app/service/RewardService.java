package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@Service
@RequiredArgsConstructor
public class RewardService {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 100L; // 100ms
    
    private final RewardRepository rewardRepository;
    private final EventRepository eventRepository;
    private final GoldenHourRepository goldenHourRepository;
    private final RewardMapper rewardMapper;
    private final GoldenHourMapper goldenHourMapper;

    @Transactional(readOnly = true)
    public List<RewardDTO> getAllRewards() {
        return rewardRepository.findAll()
                .stream()
                .map(rewardMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RewardDTO getRewardById(Long id) {
        return rewardRepository.findById(id)
                .map(rewardMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", id));
    }

    @Transactional
    public RewardDTO createReward(RewardDTO.CreateRewardRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));
        
        Reward reward = rewardMapper.toEntity(request);
        reward.setEvent(event);
        reward.setRemainingQuantity(reward.getQuantity());
        
        return rewardMapper.toDTO(rewardRepository.save(reward));
    }

    @Transactional
    public RewardDTO updateReward(Long id, RewardDTO.UpdateRewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", id));
        
        rewardMapper.updateRewardFromRequest(request, reward);
        return rewardMapper.toDTO(rewardRepository.save(reward));
    }

    @Transactional
    public RewardDTO updateQuantity(Long id, int quantity) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                Reward reward = rewardRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", id));
                
                // Validate quantity
                if (quantity < 0) {
                    throw new IllegalArgumentException("Quantity cannot be negative");
                }

                // Calculate new remaining quantity proportionally
                double ratio = quantity / (double) reward.getQuantity();
                int newRemainingQuantity = (int) Math.ceil(reward.getRemainingQuantity() * ratio);
                
                reward.setQuantity(quantity);
                reward.setRemainingQuantity(newRemainingQuantity);
                
                Reward savedReward = rewardRepository.save(reward);
                return rewardMapper.toDTO(savedReward);
                
            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new OptimisticLockingFailureException(
                        "Failed to update reward quantity after " + MAX_RETRIES + " attempts", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while retrying update", ie);
                }
            }
        }
        throw new OptimisticLockingFailureException("Failed to update reward quantity");
    }

    @Transactional
    public void deleteReward(Long id) {
        if (!rewardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reward", "id", id);
        }
        rewardRepository.deleteById(id);
    }

    @Transactional
    public RewardDTO addGoldenHour(Long rewardId, GoldenHourDTO.CreateRequest createRequest) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", rewardId));

        GoldenHour goldenHour = goldenHourMapper.createEntity(createRequest);
        goldenHour.setReward(reward);
        goldenHour = goldenHourRepository.save(goldenHour);
        reward.addGoldenHour(goldenHour);
        return rewardMapper.toDTO(reward);
    }

    @Transactional
    public RewardDTO removeGoldenHour(Long rewardId, Long goldenHourId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", rewardId));
        
        GoldenHour goldenHour = goldenHourRepository.findById(goldenHourId)
                .orElseThrow(() -> new ResourceNotFoundException("GoldenHour", "id", goldenHourId));

        reward.removeGoldenHour(goldenHour);
        return rewardMapper.toDTO(rewardRepository.save(reward));
    }

    @Transactional
    public Optional<Reward> decrementRemainingQuantity(Long id) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                Optional<Reward> rewardOpt = rewardRepository.findById(id);
                if (rewardOpt.isEmpty()) {
                    return Optional.empty();
                }

                Reward reward = rewardOpt.get();
                if (reward.getRemainingQuantity() <= 0) {
                    return Optional.empty();
                }

                reward.decrementRemainingQuantity();
                return Optional.of(rewardRepository.save(reward));

            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    return Optional.empty();
                }
                try {
                    Thread.sleep(RETRY_DELAY * (long) attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while retrying decrement", ie);
                }
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public boolean isRewardAvailable(Reward reward, LocalDateTime dateTime, String province) {
        return reward != null && reward.isAvailable(dateTime, province);
    }
}