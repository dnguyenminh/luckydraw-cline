package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegionDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^[A-Z0-9_]{2,20}$", message = "Code must be uppercase alphanumeric and underscore, between 2-20 characters")
        private String code;

        @Min(value = 0, message = "Default win probability must be non-negative")
        private Double defaultWinProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @Min(value = 0, message = "Default win probability must be non-negative")
        private Double defaultWinProbability;

        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private Double defaultWinProbability;
        private Integer status;
        private Integer locationCount;
        private Integer provinceCount;
        private Integer activeLocationCount;
        private Integer activeProvinceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private String code;
        private Integer status;
        private Integer locationCount;
        private Integer provinceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long id;
        private String name;
        private String code;
        private Integer totalLocations;
        private Integer activeLocations;
        private Integer totalProvinces;
        private Integer activeProvinces;
        private Double locationActivationRate;
        private Double provinceActivationRate;
    }
}
