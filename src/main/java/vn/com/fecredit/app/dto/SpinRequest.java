package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Participant ID is required")
    private Long participantId;

    private String customerLocation;

    // Optional fields for eligibility checks
    private Boolean isGoldenHourEligible;
    private Boolean hasActiveParticipation;
    private Long remainingSpinsForParticipant;
    private String participantStatus;
}