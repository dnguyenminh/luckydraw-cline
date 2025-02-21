package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldenHourDTO {
    private Long id;
    private Long eventId;
    private Long rewardId;
    private String name;
    private Integer startHour;
    private Integer endHour;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double multiplier;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (startTime != null) {
            this.startHour = startTime.getHour();
        }
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            this.endHour = endTime.getHour();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String name;
        private Integer startHour;
        private Integer endHour;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double multiplier;
        private Boolean isActive;

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            if (startTime != null) {
                this.startHour = startTime.getHour();
            }
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            if (endTime != null) {
                this.endHour = endTime.getHour();
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private Integer startHour;
        private Integer endHour;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double multiplier;
        private Boolean isActive;

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            if (startTime != null) {
                this.startHour = startTime.getHour();
            }
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            if (endTime != null) {
                this.endHour = endTime.getHour();
            }
        }
    }
}