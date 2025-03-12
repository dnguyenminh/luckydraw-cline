package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO classes for spin-related operations.
 */
public class SpinDTO {

    /**
     * Response DTO for spin result operations.
     * Contains information about the result of a spin, including whether it was a win
     * and any reward information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinResultResponse {
        private Long id;
        private Long participantId;
        private Long eventId;
        private Long locationId;
        private LocalDateTime spinTime;
        private boolean win;
        private Long rewardId;
        private String rewardName;
        private String rewardCode;
        private Integer points;
        private String message;
    }
    
    /**
     * Request DTO for spin operations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinRequest {
        private Long participantId;
        private Long eventId;
        private Long locationId;
    }
}