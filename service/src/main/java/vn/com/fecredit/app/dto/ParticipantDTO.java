package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class ParticipantDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long eventId;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Integer spinsRemaining;
        private Integer dailySpinLimit;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long eventId;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Boolean isEligibleForSpin;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String eventName;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Integer spinsRemaining;
        private Integer dailySpinLimit;
        private Boolean isEligibleForSpin;
        private Integer status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String eventName;
        private Long eventId;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Integer spinsRemaining;
        private Integer dailySpinLimit;
        private LocalDateTime lastSpinTime;
        private LocalDateTime lastSyncTime;
        private Boolean isEligibleForSpin;
        private Integer status;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
    }
}
