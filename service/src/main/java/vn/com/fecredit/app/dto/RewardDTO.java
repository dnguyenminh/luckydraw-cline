package vn.com.fecredit.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RewardDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^[A-Z0-9_]{3,20}$", message = "Code must be 3-20 uppercase letters, numbers or underscores")
        private String code;

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        private String name;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        @NotNull(message = "Initial quantity is required")
        @Min(value = 1, message = "Initial quantity must be at least 1")
        private Integer initialQuantity;

        @NotNull(message = "Win probability is required")
        @DecimalMin(value = "0.0", message = "Win probability must be between 0 and 1")
        @DecimalMax(value = "1.0", message = "Win probability must be between 0 and 1")
        private Double winProbability;

        private String imageUrl;
        private Long eventLocationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        private String name;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        @Min(value = 0, message = "Remaining quantity cannot be negative")
        private Integer remainingQuantity;

        @DecimalMin(value = "0.0", message = "Win probability must be between 0 and 1")
        @DecimalMax(value = "1.0", message = "Win probability must be between 0 and 1")
        private Double winProbability;

        private String imageUrl;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String code;
        private String name;
        private String description;
        private Integer initialQuantity;
        private Integer remainingQuantity;
        private Double winProbability;
        private String imageUrl;
        private Boolean active;
        private Long eventLocationId;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String code;
        private String name;
        private Integer remainingQuantity;
        private Double winProbability;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long id;
        private String code;
        private String name;
        private Integer totalQuantity;
        private Integer remainingQuantity;
        private Integer claimedQuantity;
        private Double winProbability;
        private Double effectiveProbability;
        private Integer uniqueWinners;
        private Boolean active;
    }
}
