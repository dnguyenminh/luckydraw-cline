package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ProvinceDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long regionId;

        @NotBlank
        @Size(max = 100)
        private String name;

        @NotBlank
        @Size(max = 20)
        private String code;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 100)
        private String name;
        
        @Size(max = 20)
        private String code;
        
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Integer status;
        
        private Long regionId;
        private String regionName;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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
        private String regionName;
        private Integer totalParticipants;
        private Integer activeParticipants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long id;
        private String name;
        private String code;
        private Long regionId;
        private String regionName;
        
        private Integer totalParticipants;
        private Integer activeParticipants;
        private Integer totalSpins;
        private Integer totalWins;
        private Double winRate;
        
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
