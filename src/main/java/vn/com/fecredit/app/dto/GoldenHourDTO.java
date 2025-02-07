package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldenHourDTO {
    private Long id;
    private String name;
    private Double multiplier;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private Long rewardId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Multiplier is required")
        @Positive(message = "Multiplier must be greater than 0")
        private Double multiplier;

        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;

        @NotNull(message = "End time is required")
        private LocalDateTime endTime;

        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private Double multiplier;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean isActive;
    }
}