package vn.com.fecredit.app.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoldenHourRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Reward ID is required")
    private Long rewardId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @NotNull(message = "Probability multiplier is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Probability multiplier must be at least 1.0")
    @DecimalMax(value = "10.0", inclusive = true, message = "Probability multiplier cannot exceed 10.0")
    private Double probabilityMultiplier;

    @Pattern(
        regexp = "^(MON|TUE|WED|THU|FRI|SAT|SUN)(,(MON|TUE|WED|THU|FRI|SAT|SUN))*$",
        message = "Recurring days must be comma-separated three-letter day codes (e.g., MON,WED,FRI)"
    )
    private String recurringDays;

    @Builder.Default
    private Boolean isActive = true;

    @AssertTrue(message = "End time must be after start time")
    private boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // Let @NotNull handle this
        }
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Golden hour duration must be between 1 and 24 hours")
    private boolean isValidDuration() {
        if (startTime == null || endTime == null) {
            return true; // Let @NotNull handle this
        }
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        return hours >= 1 && hours <= 24;
    }

    @AssertTrue(message = "Start time must be at the beginning of an hour")
    private boolean isStartTimeOnHour() {
        if (startTime == null) {
            return true; // Let @NotNull handle this
        }
        return startTime.getMinute() == 0 && startTime.getSecond() == 0;
    }

    @AssertTrue(message = "End time must be at the beginning of an hour")
    private boolean isEndTimeOnHour() {
        if (endTime == null) {
            return true; // Let @NotNull handle this
        }
        return endTime.getMinute() == 0 && endTime.getSecond() == 0;
    }

    public void normalizeRecurringDays() {
        if (recurringDays != null) {
            // Convert to uppercase and sort days
            String[] days = recurringDays.split(",");
            java.util.Arrays.sort(days);
            recurringDays = String.join(",", days);
        }
    }

    public boolean hasRecurringDays() {
        return recurringDays != null && !recurringDays.isEmpty();
    }

    public boolean isValidForDay(java.time.DayOfWeek dayOfWeek) {
        if (!hasRecurringDays()) {
            return true;
        }
        String day = dayOfWeek.toString().substring(0, 3);
        return recurringDays.contains(day);
    }

    /**
     * Checks if this golden hour would overlap with another one
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return !startTime.isAfter(otherEnd) && !endTime.isBefore(otherStart);
    }
}
