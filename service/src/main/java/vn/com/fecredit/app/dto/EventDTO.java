package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

public class EventDTO {

    public static final int STATUS_ACTIVE = 1;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String code;
        private String name;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalSpinsAllowed;
        private Integer initialSpins;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor  
    public static class UpdateRequest {
        private String name;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalSpinsAllowed;
        private Integer initialSpins;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String code;
        private String name;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalSpinsAllowed;
        private Integer initialSpins;
        private Integer remainingSpins;
        private Integer status;
        private String createdBy;
        private String updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int locationCount;
        private int rewardCount;
        private int participantCount; 
        private int spinCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private long totalEvents;
        private long activeEvents;
        private long totalLocations;
        private long totalRewards;
        private long totalParticipants;
        private long totalSpins;
        private long totalWins;
        private double winRate;
        private double averageRewardsPerEvent;
        private double averageLocationsPerEvent;
        private double averageParticipantsPerEvent;
        private Set<String> topLocations;
        private Set<String> topRewards;
    }
}
