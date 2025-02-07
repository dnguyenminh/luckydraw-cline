package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardDTO {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private Integer remainingQuantity;
    private Integer maxQuantityInPeriod;
    private Double probability;
    private String applicableProvinces;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Long eventId;
    private List<GoldenHourDTO> goldenHours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRewardRequest {
        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        private Integer quantity;

        private Integer maxQuantityInPeriod;

        @NotNull(message = "Probability is required")
        @Positive(message = "Probability must be greater than 0")
        private Double probability;

        private String applicableProvinces;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;

        @NotNull(message = "Event ID is required")
        private Long eventId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRewardRequest {
        private String name;
        private String description;
        private Integer quantity;
        private Integer maxQuantityInPeriod;
        private Double probability;
        private String applicableProvinces;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
    }
}
