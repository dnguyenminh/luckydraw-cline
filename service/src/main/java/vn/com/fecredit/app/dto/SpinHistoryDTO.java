package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotNull;
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
        @NotNull
        private Long participantEventId;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long rewardId;
        private Integer pointsEarned;
        private Boolean finalized;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long participantEventId;
        private String participantName;
        private String eventName;
        private String locationName;
        
        private Long rewardId;
        private String rewardName;
        private Boolean win;
        private Integer pointsEarned;
        private Boolean finalized;
        
        private LocalDateTime spinTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer status;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String eventName;
        private String locationName;
        private String participantName;
        private Boolean win;
        private Integer pointsEarned;
        private LocalDateTime spinTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long participantEventId;
        private String participantName;
        private String eventName;
        private String locationName;
        
        private Integer totalSpins;
        private Integer winningSpins;
        private Integer totalPoints;
        private Double winRate;
        
        private LocalDateTime firstSpinTime;
        private LocalDateTime lastSpinTime;
    }
}
