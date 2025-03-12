package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

public class Summary {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummary {
        private Long id;
        private String name;
        private EntityStatus status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long locationCount;
        private Long participantCount;
        private String code;
        private String description;
        private Integer dailySpinLimit;
        private Double defaultWinProbability;
        private String metadata;
        private Integer version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private Long rewardCount;
        private Long spinCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStats {
        private Long totalEvents;
        private Long activeEvents;
        private Long totalLocations;
        private Long totalRewards;
        private Long totalParticipants;
        private Long totalSpins;
        private Long totalWins;
        private Double winRate;
        private Double averageRewardsPerEvent;
        private Double averageLocationsPerEvent;
        private Double averageParticipantsPerEvent;
        private Set<String> topLocations;
        private Set<String> topRewards;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String name;
        private String code;
        private String description;
        private LocalDateTime startDate; 
        private LocalDateTime endDate;
        private Integer dailySpinLimit;
        private Double defaultWinProbability;
        private String metadata;
        private EntityStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer dailySpinLimit;
        private Double defaultWinProbability;
        private String metadata;
        private EntityStatus status;
    }
}
