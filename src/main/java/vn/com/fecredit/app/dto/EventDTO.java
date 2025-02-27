package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import vn.com.fecredit.app.dto.event.*;

/**
 * Legacy DTO wrapper for event-related operations.
 * Uses composition to delegate to new DTO classes.
 */
@Data
@NoArgsConstructor
public class EventDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        @Size(max = 50)
        private String code;

        @NotBlank
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        @NotNull
        private LocalDateTime startDate;

        @NotNull
        private LocalDateTime endDate;

        private Integer dailySpinLimit;
        private Integer totalSpins;
        private Boolean active;

        public CreateEventRequest toNew() {
            return CreateEventRequest.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailySpinLimit(dailySpinLimit)
                    .totalSpins(totalSpins)
                    .active(active)
                    .build();
        }

        public static CreateRequest fromNew(CreateEventRequest newRequest) {
            return CreateRequest.builder()
                    .code(newRequest.getCode())
                    .name(newRequest.getName())
                    .description(newRequest.getDescription())
                    .startDate(newRequest.getStartDate())
                    .endDate(newRequest.getEndDate())
                    .dailySpinLimit(newRequest.getDailySpinLimit())
                    .totalSpins(newRequest.getTotalSpins())
                    .active(newRequest.getActive())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer dailySpinLimit;
        private Integer totalSpins;
        private Boolean active;

        public UpdateEventRequest toNew() {
            return UpdateEventRequest.builder()
                    .name(name)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailySpinLimit(dailySpinLimit)
                    .totalSpins(totalSpins)
                    .active(active)
                    .build();
        }

        public static UpdateRequest fromNew(UpdateEventRequest newRequest) {
            return UpdateRequest.builder()
                    .name(newRequest.getName())
                    .description(newRequest.getDescription())
                    .startDate(newRequest.getStartDate())
                    .endDate(newRequest.getEndDate())
                    .dailySpinLimit(newRequest.getDailySpinLimit())
                    .totalSpins(newRequest.getTotalSpins())
                    .active(newRequest.getActive())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventResponse {
        private Long id;
        private String code;
        private String name;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer dailySpinLimit;
        private Integer totalSpins;
        private Integer remainingSpins;
        private boolean active;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private EventDTO.EventStatistics statistics;
        private List<EventLocationDTO.EventLocationResponse> locations;
        private List<RewardDTO.RewardResponse> rewards;

        public static EventResponse fromNew(vn.com.fecredit.app.dto.event.EventResponse newResponse) {
            return EventResponse.builder()
                    .id(newResponse.getId())
                    .code(newResponse.getCode())
                    .name(newResponse.getName())
                    .description(newResponse.getDescription())
                    .startDate(newResponse.getStartDate())
                    .endDate(newResponse.getEndDate())
                    .dailySpinLimit(newResponse.getDailySpinLimit())
                    .totalSpins(newResponse.getTotalSpins())
                    .remainingSpins(newResponse.getRemainingSpins())
                    .active(newResponse.isActive())
                    .deleted(newResponse.isDeleted())
                    .createdAt(newResponse.getCreatedAt())
                    .updatedAt(newResponse.getUpdatedAt())
                    .statistics(EventStatistics.fromNew(newResponse.getStatistics()))
                    .locations(newResponse.getLocations())
                    .rewards(newResponse.getRewards())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummary {
        private Long id;
        private String code;
        private String name;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer remainingSpins;
        private boolean active;
        private EventStatistics statistics;

        public static EventSummary fromNew(vn.com.fecredit.app.dto.event.EventSummary newSummary) {
            return EventSummary.builder()
                    .id(newSummary.getId())
                    .code(newSummary.getCode())
                    .name(newSummary.getName())
                    .startDate(newSummary.getStartDate())
                    .endDate(newSummary.getEndDate())
                    .remainingSpins(newSummary.getRemainingSpins())
                    .active(newSummary.isActive())
                    .statistics(EventStatistics.fromNew(newSummary.getStatistics()))
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStatistics {
        private int totalParticipants;
        private int activeParticipants;
        private int totalSpins;
        private int remainingSpins;
        private double participationRate;
        private double completionRate;
        private int totalLocations;
        private int activeLocations;
        private int totalRewards;
        private int activeRewards;
        private LocalDateTime lastSpinDate;
        private LocalDateTime lastWinDate;
        private SpinStats todayStats;
        private SpinStats weeklyStats;
        private SpinStats monthlyStats;

        public static EventStatistics fromNew(vn.com.fecredit.app.dto.event.EventStatistics newStats) {
            if (newStats == null) return null;
            return EventStatistics.builder()
                    .totalParticipants(newStats.getTotalParticipants())
                    .activeParticipants(newStats.getActiveParticipants())
                    .totalSpins(newStats.getTotalSpins())
                    .remainingSpins(newStats.getRemainingSpins())
                    .participationRate(newStats.getParticipationRate())
                    .completionRate(newStats.getCompletionRate())
                    .totalLocations(newStats.getTotalLocations())
                    .activeLocations(newStats.getActiveLocations())
                    .totalRewards(newStats.getTotalRewards())
                    .activeRewards(newStats.getActiveRewards())
                    .lastSpinDate(newStats.getLastSpinDate())
                    .lastWinDate(newStats.getLastWinDate())
                    .todayStats(SpinStats.fromNew(newStats.getTodayStats()))
                    .weeklyStats(SpinStats.fromNew(newStats.getWeeklyStats()))
                    .monthlyStats(SpinStats.fromNew(newStats.getMonthlyStats()))
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinStats {
        private int totalSpins;
        private int totalWins;
        private double winRate;
        private int uniqueParticipants;
        private int newParticipants;
        private double averageSpinsPerParticipant;

        public static SpinStats fromNew(vn.com.fecredit.app.dto.event.SpinStats newStats) {
            if (newStats == null) return null;
            return SpinStats.builder()
                    .totalSpins(newStats.getTotalSpins())
                    .totalWins(newStats.getTotalWins())
                    .winRate(newStats.getWinRate())
                    .uniqueParticipants(newStats.getUniqueParticipants())
                    .newParticipants(newStats.getNewParticipants())
                    .averageSpinsPerParticipant(newStats.getAverageSpinsPerParticipant())
                    .build();
        }
    }
}
