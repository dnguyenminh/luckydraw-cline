package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EventDTO {

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
        private Integer status;
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
        private Integer status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer dailySpinLimit;
        private Double defaultWinProbability;
        private String metadata;
        private Integer status;
        private Integer version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private Long locationCount;
        private Long rewardCount;
        private Long participantCount;
        private Long spinCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
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

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_DELETED = -1;
}
