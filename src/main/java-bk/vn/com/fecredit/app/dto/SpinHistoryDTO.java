package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class SpinHistoryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long eventId;
        private Long participantId;
        private Long rewardId;
        private Long locationId;
        private Long goldenHourId;
        private LocalDateTime spinTime;
        private Double winProbability;
        private Double probabilityMultiplier;
        private Double goldenHourMultiplier;
        private String deviceId;
        private String sessionId;
        private String metadata;
        private String notes;
        private Double latitude;
        private Double longitude;
        private String ipAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long id;
        private Boolean winning;
        private Double winProbability;
        private Double finalProbability;
        private String spinResult;
        private String notes;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String eventName;
        private String participantName;
        private String rewardName;
        private String locationName;
        private LocalDateTime spinTime;
        private boolean winning;
        private Double winProbability;
        private Double finalProbability;
        private String spinResult;
        private String deviceId;
        private String sessionId;
        private String metadata;
        private String notes;
        private Double latitude;
        private Double longitude;
        private boolean goldenHourActive;
        private String ipAddress;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String locationName;
        private String rewardName;
        private String participantName;
        private LocalDateTime spinTime;
        private boolean winning;
        private String spinResult;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalSpins;
        private Long winningSpins;
        private Double winRate;
        private LocalDateTime firstSpinTime;
        private LocalDateTime lastSpinTime;
        private Long uniqueParticipants;
        private Long uniqueLocations;
        private Long uniqueRewards;
        private Long goldenHourSpins;
        private Double averageWinProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinResponse {
        private Long id;
        private boolean winning;
        private String rewardName;
        private String spinResult;
        private LocalDateTime spinTime;
        private Double winProbability;
        private Double finalProbability;
        private boolean goldenHourActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResult {
        private Long id;
        private boolean winning;
        private String spinResult;
        private LocalDateTime updatedAt;
    }
}
