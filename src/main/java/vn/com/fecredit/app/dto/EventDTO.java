package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer participantCount;
    private Integer rewardCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateEventRequest {
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^\\S+$", message = "Code must not contain spaces")
        private String code;

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @NotNull(message = "Start date is required")
        private LocalDateTime startDate;

        @NotNull(message = "End date is required")
        private LocalDateTime endDate;

        @NotNull(message = "Active status is required")
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEventRequest {
        private String name;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStatistics {
        private Long id;
        private String name;
        private Integer totalParticipants;
        private Integer totalSpins;
        private Integer totalRewardsGiven;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummary {
        private Long id;
        private String name;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
        private Integer participantCount;
        private Integer totalSpins;
        private Integer remainingRewards;
    }
}