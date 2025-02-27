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

/**
 * Container class for Event-related DTOs
 */
final class EventDTO {
    private EventDTO() {
        // Prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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
        private EventStatistics statistics;
        private List<EventLocationDTO.EventLocationResponse> locations;
        private List<RewardDTO.RewardResponse> rewards;
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStatistics {
        private int totalParticipants;
        private int totalRewards;
        private int activeRewards;
        private LocalDateTime lastSpinDate;
        private LocalDateTime lastWinDate;
        private SpinStats todayStats;
        private SpinStats weeklyStats;
        private SpinStats monthlyStats;
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStatus {
        private boolean available;
        private boolean active;
        private boolean deleted;
        private boolean withinValidPeriod;
        private boolean hasSpinsRemaining;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long remainingDays;
        private String unavailabilityReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSpinValidation {
        private boolean canSpin;
        private boolean withinValidPeriod;
        private boolean hasSpinsRemaining;
        private boolean hasLocations;
        private boolean hasRewards;
        private String validationMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUpdateResult {
        private int totalProcessed;
        private int successfulUpdates;
        private int failedUpdates;
        private String errorMessage;
        private LocalDateTime processedAt;
    }
}
