package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
public class SpinRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Participant ID is required")
    private Long participantId;

    private Long eventLocationId;
    private String location;

    @Builder.Default
    private Boolean hasActiveParticipation = false;
    
    @Builder.Default
    private Boolean isGoldenHourEligible = false;

    @Builder.Default
    private SpinRequestType type = SpinRequestType.STANDARD;

    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String channel;
    private String source;

    public enum SpinRequestType {
        STANDARD,
        GOLDEN_HOUR,
        BONUS,
        PROMOTIONAL,
        VIP
    }

    // Validation methods
    public boolean isLocationRequired() {
        return eventLocationId == null;
    }

    public boolean isLocationValid() {
        if (!isLocationRequired()) {
            return true;
        }
        return location != null && !location.trim().isEmpty();
    }

    public boolean isEligibleForGoldenHour() {
        return isGoldenHourEligible && hasActiveParticipation;
    }

    public boolean isVIPSpin() {
        return SpinRequestType.VIP.equals(type);
    }

    public boolean isPromotionalSpin() {
        return SpinRequestType.PROMOTIONAL.equals(type);
    }

    public boolean isBonusSpin() {
        return SpinRequestType.BONUS.equals(type);
    }

    public boolean isStandardSpin() {
        return SpinRequestType.STANDARD.equals(type);
    }

    // Builder customization
    public static class SpinRequestBuilder {
        private String location;
        private Long eventLocationId;

        public SpinRequestBuilder location(String location) {
            this.location = location;
            return this;
        }

        public SpinRequestBuilder eventLocationId(Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }
    }
}
