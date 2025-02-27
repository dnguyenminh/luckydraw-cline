package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.dto.SpinHistoryDTO.CreateRequest;
import vn.com.fecredit.app.dto.SpinHistoryDTO.SpinResponse;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.mapper.common.BaseMapper;

@Component 
public class SpinHistoryMapper implements BaseMapper<SpinHistory, SpinResponse, CreateRequest, CreateRequest> {

    @Override
    public SpinHistory toEntity(CreateRequest request) {
        if (request == null) {
            return null;
        }

        return SpinHistory.builder()
            .spinTime(request.getSpinTime())
            .winProbability(request.getWinProbability())
            .probabilityMultiplier(request.getProbabilityMultiplier())
            .goldenHourActive(request.isGoldenHourActive())
            .goldenHourMultiplier(request.getGoldenHourMultiplier())
            .notes(request.getNotes())
            .build();
    }

    @Override
    public void updateEntity(CreateRequest request, SpinHistory entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setSpinTime(request.getSpinTime());
        entity.setWinProbability(request.getWinProbability());
        entity.setProbabilityMultiplier(request.getProbabilityMultiplier());
        entity.setGoldenHourDetails(request.isGoldenHourActive(), request.getGoldenHourMultiplier());
        entity.setNotes(request.getNotes());
    }

    @Override
    public SpinResponse toResponse(SpinHistory entity) {
        if (entity == null) {
            return null;
        }

        return SpinResponse.builder()
            .id(entity.getId())
            .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
            .eventName(entity.getEvent() != null ? entity.getEvent().getName() : null)
            .participantId(entity.getParticipant() != null ? entity.getParticipant().getId() : null)
            .participantName(entity.getParticipant() != null ? entity.getParticipant().getFullName() : null)
            .eventLocationId(entity.getEventLocation() != null ? entity.getEventLocation().getId() : null)
            .eventLocationName(entity.getEventLocation() != null ? entity.getEventLocation().getName() : null)
            .eventLocationProvince(entity.getEventLocation() != null ? entity.getEventLocation().getProvince() : null)
            .rewardId(entity.getReward() != null ? entity.getReward().getId() : null)
            .rewardName(entity.getReward() != null ? entity.getReward().getName() : null)
            .spinTime(entity.getSpinTime())
            .spinDate(entity.getSpinDate())
            .win(entity.isWin())
            .winProbability(entity.getWinProbability())
            .finalProbability(entity.getFinalProbability())
            .goldenHourActive(entity.isGoldenHourActive())
            .goldenHourMultiplier(entity.getGoldenHourMultiplier())
            .probabilityMultiplier(entity.getProbabilityMultiplier())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    @Override
    public Class<SpinHistory> getEntityClass() {
        return SpinHistory.class;
    }

    @Override
    public Class<SpinResponse> getResponseClass() {
        return SpinResponse.class;
    }

    @Override
    public Class<CreateRequest> getCreateRequestClass() {
        return CreateRequest.class;
    }

    @Override
    public Class<CreateRequest> getUpdateRequestClass() {
        return CreateRequest.class;
    }

    public SpinHistoryDTO.SpinSummary toSummary(SpinHistory entity) {
        if (entity == null) {
            return null;
        }

        return SpinHistoryDTO.SpinSummary.builder()
            .id(entity.getId())
            .eventName(entity.getEvent() != null ? entity.getEvent().getName() : null)
            .participantName(entity.getParticipant() != null ? entity.getParticipant().getFullName() : null)
            .eventLocationName(entity.getEventLocation() != null ? entity.getEventLocation().getName() : null)
            .rewardName(entity.getReward() != null ? entity.getReward().getName() : null)
            .spinTime(entity.getSpinTime())
            .win(entity.isWin())
            .finalProbability(entity.getFinalProbability())
            .goldenHourActive(entity.isGoldenHourActive())
            .build();
    }

    public List<SpinHistoryDTO.SpinSummary> toSummaryList(List<SpinHistory> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    public SpinHistoryDTO.SpinStats toStats(List<SpinHistory> spins) {
        if (spins == null || spins.isEmpty()) {
            return null;
        }

        SpinHistory first = spins.get(0);
        Long eventId = first.getEvent() != null ? first.getEvent().getId() : null;
        Long participantId = first.getParticipant() != null ? first.getParticipant().getId() : null;

        int totalSpins = spins.size();
        int totalWins = (int) spins.stream().filter(SpinHistory::isWin).count();
        double winRate = totalSpins > 0 ? (double) totalWins / totalSpins : 0.0;

        Double avgProbability = spins.stream()
            .map(SpinHistory::getWinProbability)
            .filter(p -> p != null)
            .mapToDouble(p -> p)
            .average()
            .orElse(0.0);

        Double avgFinalProbability = spins.stream()
            .map(SpinHistory::getFinalProbability)
            .filter(p -> p != null)
            .mapToDouble(p -> p)
            .average()
            .orElse(0.0);

        int goldenHourSpins = (int) spins.stream()
            .filter(SpinHistory::isGoldenHourActive)
            .count();

        Double avgGoldenHourMultiplier = spins.stream()
            .filter(SpinHistory::isGoldenHourActive)
            .map(SpinHistory::getGoldenHourMultiplier)
            .filter(m -> m != null)
            .mapToDouble(m -> m)
            .average()
            .orElse(0.0);

        LocalDateTime firstSpinTime = spins.stream()
            .map(SpinHistory::getSpinTime)
            .min(LocalDateTime::compareTo)
            .orElse(null);

        LocalDateTime lastSpinTime = spins.stream()
            .map(SpinHistory::getSpinTime)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        LocalDateTime lastWinTime = spins.stream()
            .filter(SpinHistory::isWin)
            .map(SpinHistory::getSpinTime)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        return SpinHistoryDTO.SpinStats.builder()
            .eventId(eventId)
            .participantId(participantId)
            .totalSpins(totalSpins)
            .totalWins(totalWins)
            .winRate(winRate)
            .averageProbability(avgProbability)
            .averageFinalProbability(avgFinalProbability)
            .goldenHourSpins(goldenHourSpins)
            .averageGoldenHourMultiplier(avgGoldenHourMultiplier)
            .firstSpinTime(firstSpinTime)
            .lastSpinTime(lastSpinTime)
            .lastWinTime(lastWinTime)
            .build();
    }
}
