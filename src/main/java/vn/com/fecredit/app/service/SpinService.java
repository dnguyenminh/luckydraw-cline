package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.spin.SpinCheckResponse;
import vn.com.fecredit.app.dto.spin.SpinRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.model.LuckyDrawResult;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.repository.LuckyDrawResultRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SpinService {
    private final RewardRepository rewardRepository;
    private final ParticipantRepository participantRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final LuckyDrawResultRepository luckyDrawResultRepository;
    private final Random random = new Random();

    public SpinService(
            RewardRepository rewardRepository,
            ParticipantRepository participantRepository,
            SpinHistoryRepository spinHistoryRepository,
            LuckyDrawResultRepository luckyDrawResultRepository) {
        this.rewardRepository = rewardRepository;
        this.participantRepository = participantRepository;
        this.spinHistoryRepository = spinHistoryRepository;
        this.luckyDrawResultRepository = luckyDrawResultRepository;
    }

    @Transactional(readOnly = true)
    public SpinHistory getLatestSpinHistory(Long participantId) {
        return spinHistoryRepository.findFirstByParticipantIdOrderBySpinTimeDesc(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("SpinHistory", "participantId", participantId));
    }

    @Transactional(readOnly = true)
    public SpinCheckResponse checkSpinEligibility(SpinRequest request) {
        Participant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "id", request.getParticipantId()));

        if (participant.getSpinsRemaining() <= 0) {
            return SpinCheckResponse.builder()
                    .eligible(false)
                    .message("No spins remaining")
                    .build();
        }

        if (!participant.getEvent().getCode().equals(request.getEventCode())) {
            return SpinCheckResponse.builder()
                    .eligible(false)
                    .message("Invalid event code")
                    .build();
        }

        if (!participant.getEvent().getIsActive()) {
            return SpinCheckResponse.builder()
                    .eligible(false)
                    .message("Event is not active")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(participant.getEvent().getStartDate()) || 
            now.isAfter(participant.getEvent().getEndDate())) {
            return SpinCheckResponse.builder()
                    .eligible(false)
                    .message("Event is not within valid time period")
                    .build();
        }

        return SpinCheckResponse.builder()
                .eligible(true)
                .message("Eligible to spin")
                .build();
    }

    @Transactional
    public LuckyDrawResult spin(SpinRequest request) {
        Participant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "id", request.getParticipantId()));

        if (participant.getSpinsRemaining() <= 0) {
            throw new IllegalStateException("No spins remaining");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reward> availableRewards = participant.getEvent().getRewards().stream()
                .filter(reward -> reward.isAvailable(now, participant.getProvince()))
                .toList();

        Optional<Reward> selectedReward = selectReward(availableRewards);

        participant.decrementSpinsRemaining();
        participantRepository.save(participant);

        SpinHistory spinHistory = SpinHistory.builder()
                .participant(participant)
                .spinTime(now)
                .reward(selectedReward.orElse(null))
                .build();
        spinHistoryRepository.save(spinHistory);

        if (selectedReward.isPresent()) {
            Reward reward = selectedReward.get();
            reward.decrementRemainingQuantity();
            rewardRepository.save(reward);

            return LuckyDrawResult.builder()
                    .participant(participant)
                    .reward(reward)
                    .spinHistory(spinHistory)
                    .winTime(now)
                    .isClaimed(false)
                    .build();
        }

        return null;
    }

    private Optional<Reward> selectReward(List<Reward> availableRewards) {
        double totalProbability = availableRewards.stream()
                .mapToDouble(Reward::getProbability)
                .sum();

        if (totalProbability <= 0) {
            return Optional.empty();
        }

        double randomValue = random.nextDouble() * totalProbability;
        double cumulativeProbability = 0;

        for (Reward reward : availableRewards) {
            cumulativeProbability += reward.getProbability();
            if (randomValue <= cumulativeProbability) {
                return Optional.of(reward);
            }
        }

        return Optional.empty();
    }
}