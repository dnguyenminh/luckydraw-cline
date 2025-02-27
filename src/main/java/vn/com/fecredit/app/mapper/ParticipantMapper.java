package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.SpinHistory;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    public Participant createEntityFromRequest(ParticipantDTO.CreateRequest request) {
        return Participant.builder()
                .eventId(request.getEventId())
                .eventLocationId(request.getEventLocationId())
                .customerId(request.getCustomerId())
                .cardNumber(request.getCardNumber())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .province(request.getProvince())
                .totalSpins(request.getTotalSpins())
                .remainingSpins(request.getTotalSpins())
                .dailySpinLimit(request.getDailySpinLimit())
                .dailySpinsUsed(0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public void updateEntityFromRequest(Participant participant, ParticipantDTO.UpdateRequest request) {
        if (request.getFullName() != null) {
            participant.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            participant.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            participant.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProvince() != null) {
            participant.setProvince(request.getProvince());
        }
        if (request.getActive() != null) {
            participant.setActive(request.getActive());
        }
        if (request.getTotalSpins() != null || request.getDailySpinLimit() != null) {
            participant.updateSpinLimits(request.getTotalSpins(), request.getDailySpinLimit());
        }
    }

    public ParticipantDTO.ParticipantResponse toResponse(Participant participant) {
        Event event = participant.getEvent();
        EventLocation location = participant.getEventLocation();
        
        return ParticipantDTO.ParticipantResponse.builder()
                .id(participant.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .eventLocationId(location.getId())
                .eventLocationName(location.getName())
                .eventLocationProvince(location.getProvince())
                .customerId(participant.getCustomerId())
                .cardNumber(participant.getCardNumber())
                .fullName(participant.getFullName())
                .email(participant.getEmail())
                .phoneNumber(participant.getPhoneNumber())
                .province(participant.getProvince())
                .totalSpins(participant.getTotalSpins())
                .remainingSpins(participant.getRemainingSpins())
                .dailySpinLimit(participant.getDailySpinLimit())
                .dailySpinsUsed(participant.getDailySpinsUsed())
                .lastSpinDate(participant.getLastSpinDate())
                .active(participant.isActive())
                .createdAt(participant.getCreatedAt())
                .updatedAt(participant.getUpdatedAt())
                .spinStats(calculateSpinStats(participant.getSpinHistories()))
                .dailyStatus(calculateDailyStatus(participant))
                .build();
    }

    public ParticipantDTO.ParticipantSummary toSummary(Participant participant) {
        return ParticipantDTO.ParticipantSummary.builder()
                .id(participant.getId())
                .customerId(participant.getCustomerId())
                .fullName(participant.getFullName())
                .province(participant.getProvince())
                .remainingSpins(participant.getRemainingSpins())
                .active(participant.isActive())
                .spinStats(calculateSpinStats(participant.getSpinHistories()))
                .build();
    }

    public ParticipantDTO.ParticipantSpinStats calculateSpinStats(Set<SpinHistory> spinHistories) {
        if (spinHistories == null || spinHistories.isEmpty()) {
            return ParticipantDTO.ParticipantSpinStats.builder()
                    .totalSpins(0)
                    .spinsWon(0)
                    .spinsToday(0)
                    .spinsThisWeek(0)
                    .spinsThisMonth(0)
                    .winRate(0.0)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        long totalSpins = spinHistories.size();
        long spinsWon = spinHistories.stream().filter(SpinHistory::isWin).count();
        
        long spinsToday = spinHistories.stream()
                .filter(s -> s.getSpinDate().toLocalDate().equals(now.toLocalDate()))
                .count();
                
        long spinsThisWeek = spinHistories.stream()
                .filter(s -> s.getSpinDate().isAfter(now.minusWeeks(1)))
                .count();
                
        long spinsThisMonth = spinHistories.stream()
                .filter(s -> s.getSpinDate().isAfter(now.minusMonths(1)))
                .count();

        double winRate = totalSpins > 0 ? (double) spinsWon / totalSpins : 0.0;

        LocalDateTime lastSpinDate = spinHistories.stream()
                .map(SpinHistory::getSpinDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastWinDate = spinHistories.stream()
                .filter(SpinHistory::isWin)
                .map(SpinHistory::getSpinDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return ParticipantDTO.ParticipantSpinStats.builder()
                .totalSpins((int) totalSpins)
                .spinsWon((int) spinsWon)
                .spinsToday((int) spinsToday)
                .spinsThisWeek((int) spinsThisWeek)
                .spinsThisMonth((int) spinsThisMonth)
                .winRate(winRate)
                .lastSpinDate(lastSpinDate)
                .lastWinDate(lastWinDate)
                .build();
    }

    public ParticipantDTO.DailySpinStatus calculateDailyStatus(Participant participant) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSpinDate = participant.getLastSpinDate();
        boolean isSameDay = lastSpinDate != null && 
                           lastSpinDate.toLocalDate().equals(now.toLocalDate());
        
        int spinsUsed = isSameDay ? participant.getDailySpinsUsed() : 0;
        int spinsRemaining = participant.getDailySpinLimit() - spinsUsed;
        
        LocalDateTime nextResetDate = now.toLocalDate().plusDays(1).atStartOfDay();
        
        boolean eligibleForSpin = participant.isEligibleForSpin();
        String reason = null;
        if (!eligibleForSpin) {
            if (!participant.isActive()) {
                reason = "Participant is inactive";
            } else if (!participant.hasSpinsRemaining()) {
                reason = "No spins remaining";
            } else if (!participant.hasDailySpinsRemaining()) {
                reason = "Daily spin limit reached";
            }
        }

        return ParticipantDTO.DailySpinStatus.builder()
                .dailySpinLimit(participant.getDailySpinLimit())
                .dailySpinsUsed(spinsUsed)
                .spinsRemaining(spinsRemaining)
                .lastSpinDate(lastSpinDate)
                .nextResetDate(nextResetDate)
                .eligibleForSpin(eligibleForSpin)
                .ineligibilityReason(reason)
                .build();
    }

    public List<ParticipantDTO.ParticipantResponse> toResponseList(List<Participant> participants) {
        return participants.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ParticipantDTO.ParticipantSummary> toSummaryList(List<Participant> participants) {
        return participants.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
