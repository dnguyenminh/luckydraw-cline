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
public class ParticipantDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private Long eventLocationId;
    private String eventLocationName;
    private String location;
    private Long locationTotalSpins;
    private Long locationRemainingSpins;
    private Long userId;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String province;
    private String customerId;
    private String employeeId;
    private String cardNumber;
    private Long spinsRemaining;
    private Long dailySpinLimit;
    private Boolean isActive;
    private Boolean isEligibleForSpin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isEligibleForSpin() {
        return Boolean.TRUE.equals(isEligibleForSpin);
    }

    public void setEligibleForSpin(boolean eligible) {
        this.isEligibleForSpin = eligible;
    }
}