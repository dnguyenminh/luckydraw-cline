package vn.com.fecredit.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class ParticipantDTO {

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Customer ID is required")
        private String customerId;
        
        private String cardNumber;
        private String phoneNumber;
        
        @NotNull(message = "Event ID is required")
        private Long eventId;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Long eventId;
        private Boolean isEligibleForSpin;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        
        private Long eventId;
        private String eventName;
        private Long currentEventId;
        private String currentEventName;
        
        private Integer spinsRemaining;
        private Integer dailySpinLimit;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastSpinTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastSyncTime;
        
        private Boolean isEligibleForSpin;
        private EntityStatus entityStatus;
        private Set<SpinHistoryDTO.Summary> spinHistories;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        private String createdBy;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        private String updatedBy;
    }

    @Data
    @Builder
    public static class Summary {
        private Long id;
        private String customerId;
        private String cardNumber;
        private String phoneNumber;
        private Long eventId;
        private String eventName;
        private Integer spinsRemaining;
        private Integer dailySpinLimit;
        private Boolean isEligibleForSpin;
        private EntityStatus entityStatus;
    }

    @Data
    @Builder
    public static class Statistics {
        private Long id;
        private String customerId;
        private Integer totalSpins;
        private Integer totalRewards;
        private Double rewardRate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastSpinTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime firstSpinTime;
        
        private Integer consecutiveDays;
        private Map<String, Integer> rewardDistribution;
        private Map<String, Integer> hourlySpinDistribution;
    }
}
