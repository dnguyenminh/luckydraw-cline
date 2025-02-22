package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@Service
@RequiredArgsConstructor
public class GoldenHourService {

    private final GoldenHourRepository goldenHourRepository;
    private final RewardRepository rewardRepository;
    private final GoldenHourMapper goldenHourMapper;

    public List<GoldenHour> findByEventIdAndIsActiveTrue(Long eventId) {
        return goldenHourRepository.findByEventIdAndIsActiveTrue(eventId);
    }

    public List<GoldenHour> findActiveByRewardIdWithDetails(Long rewardId) {
        return goldenHourRepository.findActiveByRewardIdWithDetails(rewardId);
    }

    public Optional<GoldenHour> findByIdWithDetails(Long id) {
        return goldenHourRepository.findByIdWithDetails(id);
    }

    public Double getGoldenHourMultiplier(Long rewardId, Long goldenHourId, LocalDateTime currentTime) {
        // If a specific golden hour is requested, check it first
        if (goldenHourId != null) {
            return goldenHourRepository.findByIdWithDetails(goldenHourId)
                .filter(GoldenHour::isActive)
                .filter(gh -> gh.getReward().getId().equals(rewardId))
                .filter(gh -> !currentTime.isBefore(gh.getStartTime()))
                .filter(gh -> !currentTime.isAfter(gh.getEndTime()))
                .map(GoldenHour::getMultiplier)
                .orElse(1.0);
        }
        
        // Otherwise find any active golden hour for the reward
        return goldenHourRepository.findActiveGoldenHourByRewardId(rewardId, currentTime)
            .map(GoldenHour::getMultiplier)
            .orElse(1.0);
    }

    public boolean isGoldenHourActive(Long rewardId, LocalDateTime dateTime) {
        return goldenHourRepository.isGoldenHourActive(rewardId, dateTime);
    }

    @Transactional
    public boolean updateStatus(Long id, boolean status) {
        int updated = goldenHourRepository.updateStatus(id, status);
        return updated > 0;
    }

    @Transactional
    public GoldenHourDTO save(GoldenHourDTO dto) {
        GoldenHour goldenHour = goldenHourMapper.toEntity(dto);
        GoldenHour saved = goldenHourRepository.save(goldenHour);
        return goldenHourMapper.toDTO(saved);
    }

    @Transactional
    public GoldenHourDTO createGoldenHour(long rewardId, GoldenHourDTO.CreateRequest request) {
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + rewardId));
            
        GoldenHour goldenHour = goldenHourMapper.toEntity(request);
        goldenHour.setReward(reward);
        
        GoldenHour saved = goldenHourRepository.save(goldenHour);
        return goldenHourMapper.toDTO(saved);
    }

    @Transactional
    public GoldenHourDTO updateGoldenHour(Long id, GoldenHourDTO.UpdateRequest request) {
        GoldenHour goldenHour = goldenHourRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GoldenHour not found with id: " + id));

        goldenHourMapper.updateEntity(goldenHour, request);
        GoldenHour saved = goldenHourRepository.save(goldenHour);
        return goldenHourMapper.toDTO(saved);
    }

    public List<GoldenHour> findActiveGoldenHoursOrdered(Long rewardId) {
        return goldenHourRepository.findActiveGoldenHoursOrdered(rewardId);
    }

    public Optional<GoldenHour> findActiveGoldenHour(Long rewardId, LocalDateTime currentTime) {
        return goldenHourRepository.findActiveGoldenHour(rewardId, currentTime);
    }

    public boolean existsByEventIdAndName(Long eventId, String name) {
        return goldenHourRepository.existsByEventIdAndName(eventId, name);
    }
}