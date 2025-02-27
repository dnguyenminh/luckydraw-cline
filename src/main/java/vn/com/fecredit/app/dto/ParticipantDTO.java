package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ParticipantDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long eventId;
        
        @NotNull
        private Long eventLocationId;

        @NotBlank
        @Size(max = 50)
        private String customerId;

        @NotBlank
        @Size(max = 50)
        private String cardNumber;

        @NotBlank
        @Size(max = 100)
        private String fullName;

        @Email
        @Size(max = 100)
        private String email;

        @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        @Size(max = 15)
        private String phoneNumber;

        @Size(max = 100)
        private String province;

        private Integer totalSpins;
        private Integer dailySpinLimit;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 100)
        private String fullName;

        @Email
        @Size(max = 100)
        private String email;

        @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        @Size(max = 15)
        private String phoneNumber;

        @Size(max = 100)
        private String province;

        private Integer totalSpins;
        private Integer dailySpinLimit;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResponse {
        private Long id;
        private Long eventId;
        private String eventName;
        private Long eventLocationId;
        private String eventLocationName;
        private String eventLocationProvince;
        private String customerId;
        private String cardNumber;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String province;
        private Integer totalSpins;
        private Integer remainingSpins;
        private Integer dailySpinLimit;
        private Integer dailySpinsUsed;
        private LocalDateTime lastSpinDate;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private ParticipantSpinStats spinStats;
        private DailySpinStatus dailyStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantSummary {
        private Long id;
        private String customerId;
        private String fullName;
        private String province;
        private Integer remainingSpins;
        private boolean active;
        private ParticipantSpinStats spinStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantSpinStats {
        private int totalSpins;
        private int spinsWon;
        private int spinsToday;
        private int spinsThisWeek;
        private int spinsThisMonth;
        private double winRate;
        private LocalDateTime lastSpinDate;
        private LocalDateTime lastWinDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySpinStatus {
        private int dailySpinLimit;
        private int dailySpinsUsed;
        private int spinsRemaining;
        private LocalDateTime lastSpinDate;
        private LocalDateTime nextResetDate;
        private boolean eligibleForSpin;
        private String ineligibilityReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantEligibility {
        private boolean eligible;
        private boolean active;
        private boolean hasSpinsRemaining;
        private boolean withinDailyLimit;
        private Integer remainingSpins;
        private Integer dailySpinsRemaining;
        private LocalDateTime lastSpinDate;
        private String ineligibilityReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportResult {
        private int totalProcessed;
        private int successfulImports;
        private int failedImports;
        private String errorMessage;
        private LocalDateTime processedAt;
    }
}
