package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.SpinHistoryMapper;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.service.SpinHistoryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpinHistoryServiceImpl implements SpinHistoryService {

    private final SpinHistoryRepository spinHistoryRepository;
    private final ParticipantEventRepository participantEventRepository;
    private final RewardRepository rewardRepository;
    private final SpinHistoryMapper spinHistoryMapper;

    @Override
    @Transactional
    public SpinHistoryDTO.Response createSpin(SpinHistoryDTO.CreateRequest request) {
        SpinHistory spinHistory = spinHistoryMapper.toEntity(request);
        validateNewSpin(spinHistory);
        spinHistory = spinHistoryRepository.save(spinHistory);
        return spinHistoryMapper.toResponse(spinHistory);
    }

    @Override
    @Transactional
    public SpinHistoryDTO.Response recordWin(Long spinId, Long rewardId, Integer pointsEarned) {
        SpinHistory spinHistory = getSpinHistoryById(spinId);
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new EntityNotFoundException("Reward", rewardId));

        validateSpinUpdate(spinHistory);
        spinHistory.markAsWin(reward, pointsEarned);
        spinHistory = spinHistoryRepository.save(spinHistory);
        return spinHistoryMapper.toResponse(spinHistory);
    }

    @Override
    @Transactional
    public SpinHistoryDTO.Response recordLoss(Long spinId) {
        SpinHistory spinHistory = getSpinHistoryById(spinId);
        validateSpinUpdate(spinHistory);
        spinHistory.markAsLoss();
        spinHistory = spinHistoryRepository.save(spinHistory);
        return spinHistoryMapper.toResponse(spinHistory);
    }

    @Override
    @Transactional
    public SpinHistoryDTO.Response finalizeSpin(Long spinId) {
        SpinHistory spinHistory = getSpinHistoryById(spinId);
        if (spinHistory.isFinalized()) {
            throw new InvalidOperationException("Spin already finalized");
        }
        spinHistory.finalize();
        spinHistory = spinHistoryRepository.save(spinHistory);
        return spinHistoryMapper.toResponse(spinHistory);
    }

    @Override
    public SpinHistoryDTO.Response getById(Long id) {
        return spinHistoryMapper.toResponse(getSpinHistoryById(id));
    }

    @Override
    public Page<SpinHistoryDTO.Response> findAll(Pageable pageable) {
        return spinHistoryRepository.findAll(pageable)
            .map(spinHistoryMapper::toResponse);
    }

    @Override
    public Page<SpinHistoryDTO.Response> findAllByParticipantEvent(Long participantEventId, Pageable pageable) {
        return spinHistoryRepository.findAllByParticipantEventId(participantEventId, pageable)
            .map(spinHistoryMapper::toResponse);
    }

    @Override
    public List<SpinHistoryDTO.Summary> findAllByEventId(Long eventId) {
        return spinHistoryMapper.toSummaryList(
            spinHistoryRepository.findAllByEventId(eventId)
        );
    }

    @Override
    public List<SpinHistoryDTO.Summary> findAllByEventLocation(Long locationId) {
        return spinHistoryMapper.toSummaryList(
            spinHistoryRepository.findAllByEventLocationId(locationId)
        );
    }

    @Override
    public Optional<SpinHistoryDTO.Response> findLatestSpin(Long participantEventId) {
        return spinHistoryRepository
            .findFirstByParticipantEventIdOrderBySpinTimeDesc(participantEventId)
            .map(spinHistoryMapper::toResponse);
    }

    @Override
    public SpinHistoryDTO.Statistics getParticipantEventStatistics(Long participantEventId) {
        ParticipantEvent participantEvent = participantEventRepository.findById(participantEventId)
            .orElseThrow(() -> new EntityNotFoundException("ParticipantEvent", participantEventId));
        return spinHistoryMapper.toStatistics(participantEvent);
    }

    @Override
    public long countTotalSpins(Long participantEventId) {
        return spinHistoryRepository.countSpinsInTimeRange(
            participantEventId,
            LocalDateTime.MIN,
            LocalDateTime.MAX
        );
    }

    @Override
    public long countWinningSpins(Long participantEventId) {
        return spinHistoryRepository.countWinningSpins(participantEventId);
    }

    @Override
    public double calculateWinRate(Long participantEventId) {
        long total = countTotalSpins(participantEventId);
        return total > 0 ? (double) countWinningSpins(participantEventId) / total : 0.0;
    }

    @Override
    public Integer getTotalPointsEarned(Long participantEventId) {
        return spinHistoryRepository.sumPointsEarned(participantEventId);
    }

    @Override
    public List<SpinHistoryDTO.Summary> findSpinsByTimeRange(Long participantEventId, LocalDateTime startTime, LocalDateTime endTime) {
        return spinHistoryMapper.toSummaryList(
            spinHistoryRepository.findAllByParticipantEventIdAndSpinTimeBetween(
                participantEventId, startTime, endTime
            )
        );
    }

    @Override
    public List<SpinHistoryDTO.Summary> findTodaysSpins(Long participantEventId) {
        return spinHistoryMapper.toSummaryList(
            spinHistoryRepository.findTodaySpins(participantEventId)
        );
    }

    @Override
    public List<SpinHistoryDTO.Summary> findRecentSpins(Long participantEventId, int limit) {
        return spinHistoryMapper.toSummaryList(
            spinHistoryRepository.findByParticipantEventIdOrderBySpinTimeDesc(
                participantEventId, 
                Pageable.ofSize(limit)
            )
        );
    }

    @Override
    public boolean hasReachedDailyLimit(Long participantEventId) {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return spinHistoryRepository.countSpinsInTimeRange(participantEventId, start, end) >= 
               getParticipantEvent(participantEventId).getMaxSpinsPerDay();
    }

    @Override
    public boolean hasUnfinalizedSpins(Long participantEventId) {
        return spinHistoryRepository.existsByParticipantEventIdAndFinalizedFalse(participantEventId);
    }

    @Override
    public boolean canSpin(Long participantEventId) {
        return !hasReachedDailyLimit(participantEventId) && 
               !hasUnfinalizedSpins(participantEventId) &&
               getParticipantEvent(participantEventId).hasRemainingSpins();
    }

    @Override
    @Transactional
    public void validateSpinState(Long spinId) {
        SpinHistory spinHistory = getSpinHistoryById(spinId);
        if (spinHistory.isFinalized()) {
            throw new InvalidOperationException("Cannot modify finalized spin");
        }
    }

    @Override
    @Transactional
    public void resetDailySpins(Long participantEventId) {
        // Reset is handled at ParticipantEvent level
        getParticipantEvent(participantEventId).resetDailySpins();
    }

    @Override
    @Transactional
    public void cancelSpin(Long spinId) {
        SpinHistory spinHistory = getSpinHistoryById(spinId);
        if (spinHistory.isFinalized()) {
            throw new InvalidOperationException("Cannot cancel finalized spin");
        }
        spinHistory.setStatus(0);
        spinHistoryRepository.save(spinHistory);
    }

    private SpinHistory getSpinHistoryById(Long id) {
        return spinHistoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("SpinHistory", id));
    }

    private ParticipantEvent getParticipantEvent(Long id) {
        return participantEventRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ParticipantEvent", id));
    }

    private void validateNewSpin(SpinHistory spinHistory) {
        ParticipantEvent participantEvent = spinHistory.getParticipantEvent();
        if (!canSpin(participantEvent.getId())) {
            throw new InvalidOperationException("Cannot create new spin: daily limit reached or unfinalized spins exist");
        }
    }

    private void validateSpinUpdate(SpinHistory spinHistory) {
        if (spinHistory.isFinalized()) {
            throw new InvalidOperationException("Cannot update finalized spin");
        }
    }
}
