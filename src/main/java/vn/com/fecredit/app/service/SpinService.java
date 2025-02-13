package vn.com.fecredit.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.*;
import vn.com.fecredit.app.repository.*;
import vn.com.fecredit.app.exception.BusinessException;
import vn.com.fecredit.app.dto.SpinRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpinService {
    
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final RewardRepository rewardRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final LuckyDrawResultRepository luckyDrawResultRepository;
    private final GoldenHourRepository goldenHourRepository;
    private final RewardSelectionService rewardSelectionService;

    // Cache for remaining spins by event
    private final ConcurrentHashMap<Long, AtomicLong> remainingSpinsCache = new ConcurrentHashMap<>();

    @Transactional
    public SpinHistory spin(SpinRequest request) {
        checkSpinEligibility(request);

        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new BusinessException("Event not found"));

        Participant participant = participantRepository.findById(request.getParticipantId())
            .orElseThrow(() -> new BusinessException("Participant not found"));

        // Get or initialize remaining spins
        AtomicLong remainingSpins = remainingSpinsCache.computeIfAbsent(
            event.getId(),
            id -> new AtomicLong(event.getTotalSpins())
        );

        // Check if spins are available
        long currentSpins = remainingSpins.get();
        if (currentSpins <= 0) {
            throw new BusinessException("No remaining spins available");
        }

        // Get active rewards and golden hours
        List<Reward> activeRewards = rewardRepository.findActiveRewardsByEventId(event.getId());
        if (activeRewards.isEmpty()) {
            throw new BusinessException("No active rewards available");
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<GoldenHour> goldenHour = goldenHourRepository.findActiveGoldenHour(
            event.getId(), now);

        // Select reward using the selection service
        Optional<Reward> selectedReward = rewardSelectionService.selectReward(
            event,
            activeRewards,
            currentSpins,
            goldenHour,
            request.getCustomerLocation()
        );

        // Update remaining spins atomically
        long updatedSpins = remainingSpins.decrementAndGet();

        // Create and save spin history
        final SpinHistory spinHistory = spinHistoryRepository.save(
            SpinHistory.builder()
                .event(event)
                .participant(participant)
                .spinTime(now)
                .won(selectedReward.isPresent())
                .result(selectedReward.isPresent() ? "WIN" : "LOSE")
                .reward(selectedReward.orElse(null))
                .isGoldenHour(goldenHour.isPresent())
                .currentMultiplier(calculateGoldenHourMultiplier(goldenHour))
                .remainingSpins(updatedSpins)
                .build()
        );

        // If won, create lucky draw result
        selectedReward.ifPresent(reward -> {
            LuckyDrawResult result = LuckyDrawResult.builder()
                .participant(participant)
                .reward(reward)
                .spinHistory(spinHistory)
                .winTime(now)
                .isClaimed(false)
                .build();
            luckyDrawResultRepository.save(result);

            log.info("Participant {} won reward {} in event {}", 
                participant.getId(), reward.getId(), event.getId());
        });

        return spinHistory;
    }

    @Transactional(readOnly = true)
    public void checkSpinEligibility(SpinRequest request) {
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new BusinessException("Event not found"));

        if (!event.getIsActive()) {
            throw new BusinessException("Event is not active");
        }

        if (!event.isInProgress()) {
            throw new BusinessException("Event is not in progress");
        }

        if (!event.hasSpinsAvailable()) {
            throw new BusinessException("No spins available for this event");
        }

        Participant participant = participantRepository.findById(request.getParticipantId())
            .orElseThrow(() -> new BusinessException("Participant not found"));

        if (!participant.getIsActive()) {
            throw new BusinessException("Participant is not active");
        }

        if (!participant.getEvent().getId().equals(event.getId())) {
            throw new BusinessException("Participant does not belong to this event");
        }
    }

    @Transactional(readOnly = true)
    public SpinHistory getLatestSpinHistory(Long participantId) {
        return spinHistoryRepository.findFirstByParticipantIdOrderBySpinTimeDesc(participantId)
            .orElse(null);
    }

    private double calculateGoldenHourMultiplier(Optional<GoldenHour> goldenHour) {
        return goldenHour.map(GoldenHour::getMultiplier).orElse(1.0);
    }

    @Transactional(readOnly = true)
    public long getRemainingSpins(Long eventId) {
        return remainingSpinsCache.computeIfAbsent(eventId,
            id -> new AtomicLong(eventRepository.findById(id)
                .map(Event::getTotalSpins)
                .orElse(0L))).get();
    }

    // For testing
    public void resetSpinCount(Long eventId) {
        remainingSpinsCache.remove(eventId);
    }
}
