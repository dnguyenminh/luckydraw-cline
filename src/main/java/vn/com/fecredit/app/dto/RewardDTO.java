package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private Long eventRegionId;
    private String code;
    private String name;
    private String description;
    private String applicableProvinces;
    private Integer quantity;
    private Integer remainingQuantity;
    private Double probability;
    private Integer maxQuantityInPeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    @Builder.Default
    private List<GoldenHourDTO> goldenHours = new ArrayList<>();
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRewardRequest {
        @NotNull(message = "Event ID is required")
        private Long eventId;

        private Long eventRegionId;

        private String code;

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        private String applicableProvinces;

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be greater than or equal to 0")
        private Integer quantity;

        @NotNull(message = "Probability is required")
        @Min(value = 0, message = "Probability must be greater than or equal to 0")
        private Double probability;

        private Integer maxQuantityInPeriod;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
        @Builder.Default
        private List<GoldenHourDTO> goldenHours = new ArrayList<>();

        public boolean isActive() {
            return Boolean.TRUE.equals(isActive);
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRewardRequest {
        private String code;
        private String name;
        private String description;
        private String applicableProvinces;
        private Integer quantity;
        private Double probability;
        private Integer maxQuantityInPeriod;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
        @Builder.Default
        private List<GoldenHourDTO> goldenHours = new ArrayList<>();

        public boolean isActive() {
            return Boolean.TRUE.equals(isActive);
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }
    }
}
