package vn.com.fecredit.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoldenHourService {

    private final GoldenHourRepository goldenHourRepository;
    private final RewardRepository rewardRepository;
    private final GoldenHourMapper goldenHourMapper;

    @Transactional(readOnly = true)
    public Long getActiveGoldenHourId(LocalDateTime dateTime) {
        Optional<GoldenHour> activeGoldenHour = goldenHourRepository.findActiveGoldenHour(dateTime);
        return activeGoldenHour.map(GoldenHour::getId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Double getGoldenHourMultiplier(Long goldenHourId, LocalDateTime dateTime) {
        if (goldenHourId == null) {
            return 1.0;
        }

        return goldenHourRepository.findById(goldenHourId)
                .filter(gh -> Boolean.TRUE.equals(gh.getIsActive()) && gh.isWithinTimeRange(dateTime))
                .map(GoldenHour::getMultiplier)
                .orElse(1.0);
    }

    @Transactional(readOnly = true)
    public Optional<GoldenHour> getActiveGoldenHour(LocalDateTime dateTime) {
        return goldenHourRepository.findActiveGoldenHour(dateTime);
    }

    @Transactional
    public GoldenHourDTO createGoldenHour(Long rewardId, GoldenHourDTO.CreateRequest request) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", rewardId));

        GoldenHour goldenHour = goldenHourMapper.createEntity(request);
        goldenHour.setReward(reward);
        goldenHour = goldenHourRepository.save(goldenHour);

        return goldenHourMapper.toDTO(goldenHour);
    }

    @Transactional
    public GoldenHourDTO updateGoldenHour(Long id, GoldenHourDTO.UpdateRequest request) {
        GoldenHour goldenHour = goldenHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoldenHour", "id", id));

        goldenHourMapper.updateEntityFromDTO(request, goldenHour);
        goldenHour = goldenHourRepository.save(goldenHour);

        return goldenHourMapper.toDTO(goldenHour);
    }

    @Transactional(readOnly = true)
    public GoldenHour findByRewardIdAndIsActiveTrue(Long rewardId) {
        return goldenHourRepository.findByRewardIdAndIsActiveTrue(rewardId)
                .orElse(null);
    }

    @Transactional
    public void deactivateGoldenHour(Long id) {
        GoldenHour goldenHour = goldenHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoldenHour", "id", id));
        goldenHour.setIsActive(false);
        goldenHourRepository.save(goldenHour);
    }

    @Transactional
    public void deleteGoldenHour(Long id) {
        goldenHourRepository.deleteById(id);
    }
}