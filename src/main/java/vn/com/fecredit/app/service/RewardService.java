package vn.com.fecredit.app.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardRepository rewardRepository;
    private final EventRepository eventRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final GoldenHourService goldenHourService;
    private final GoldenHourRepository goldenHourRepository;
    private final RewardMapper rewardMapper;
    private final GoldenHourMapper goldenHourMapper;

//    public RewardService(
//            RewardRepository rewardRepository,
//            EventRepository eventRepository,
//            SpinHistoryRepository spinHistoryRepository,
//            GoldenHourService goldenHourService,
//            GoldenHourRepository goldenHourRepository,
//            RewardMapper rewardMapper,
//            GoldenHourMapper goldenHourMapper
//    ) {
//        this.rewardRepository = rewardRepository;
//        this.eventRepository = eventRepository;
//        this.spinHistoryRepository = spinHistoryRepository;
//        this.goldenHourService = goldenHourService;
//        this.goldenHourRepository = goldenHourRepository;
//        this.rewardMapper = rewardMapper;
//        this.goldenHourMapper = goldenHourMapper;
//    }

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
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", id));
        
        reward.setQuantity(quantity);
        reward.setRemainingQuantity(quantity);
        return rewardMapper.toDTO(rewardRepository.save(reward));
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
        reward.addGoldenHour(goldenHour);

        return rewardMapper.toDTO(rewardRepository.save(reward));
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
        try {
            return rewardRepository.findById(id)
                    .map(reward -> {
                        if (reward.getRemainingQuantity() > 0) {
                            reward.decrementRemainingQuantity();
                            return rewardRepository.save(reward);
                        }
                        return null;
                    });
        } catch (OptimisticLockException e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public boolean isRewardAvailable(Reward reward, LocalDateTime dateTime, String province) {
        return reward.isAvailable(dateTime, province);
    }
}