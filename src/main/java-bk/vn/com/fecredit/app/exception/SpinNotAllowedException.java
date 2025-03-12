package vn.com.fecredit.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SpinNotAllowedException extends BusinessException {
    
    private final SpinRestrictionReason reason;

    public SpinNotAllowedException(String message, SpinRestrictionReason reason) {
        super(message, reason.getCode());
        this.reason = reason;
        setStatus("SPIN_RESTRICTED");
    }

    public enum SpinRestrictionReason {
        NO_REMAINING_SPINS("SPIN001", "No remaining spins available"),
        DAILY_LIMIT_REACHED("SPIN002", "Daily spin limit reached"),
        COOLDOWN_ACTIVE("SPIN003", "Spin cooldown period is active"),
        INVALID_LOCATION("SPIN004", "Invalid spin location"),
        EVENT_INACTIVE("SPIN005", "Event is not active"),
        EVENT_ENDED("SPIN006", "Event has ended"),
        PARTICIPANT_INELIGIBLE("SPIN007", "Participant is not eligible"),
        REWARD_UNAVAILABLE("SPIN008", "No rewards available"),
        QUOTA_EXCEEDED("SPIN009", "Spin quota exceeded"),
        TIME_RESTRICTED("SPIN010", "Spin not allowed at this time"),
        GOLDEN_HOUR_INACTIVE("SPIN011", "Golden hour is not active"),
        SYSTEM_ERROR("SPIN999", "System error occurred");

        private final String code;
        private final String description;

        SpinRestrictionReason(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public static SpinNotAllowedException noRemainingSpins() {
        return new SpinNotAllowedException(
            SpinRestrictionReason.NO_REMAINING_SPINS.getDescription(),
            SpinRestrictionReason.NO_REMAINING_SPINS
        );
    }

    public static SpinNotAllowedException dailyLimitReached() {
        return new SpinNotAllowedException(
            SpinRestrictionReason.DAILY_LIMIT_REACHED.getDescription(),
            SpinRestrictionReason.DAILY_LIMIT_REACHED
        );
    }

    public static SpinNotAllowedException cooldownActive(long remainingSeconds) {
        return new SpinNotAllowedException(
            String.format("Please wait %d seconds before next spin", remainingSeconds),
            SpinRestrictionReason.COOLDOWN_ACTIVE
        );
    }

    public static SpinNotAllowedException invalidLocation(String location) {
        return new SpinNotAllowedException(
            String.format("Location '%s' is not valid for this spin", location),
            SpinRestrictionReason.INVALID_LOCATION
        );
    }

    public static SpinNotAllowedException eventInactive(Long eventId) {
        return new SpinNotAllowedException(
            String.format("Event %d is not currently active", eventId),
            SpinRestrictionReason.EVENT_INACTIVE
        );
    }

    public static SpinNotAllowedException eventEnded(Long eventId) {
        return new SpinNotAllowedException(
            String.format("Event %d has ended", eventId),
            SpinRestrictionReason.EVENT_ENDED
        );
    }

    public static SpinNotAllowedException participantIneligible(Long participantId) {
        return new SpinNotAllowedException(
            String.format("Participant %d is not eligible for spin", participantId),
            SpinRestrictionReason.PARTICIPANT_INELIGIBLE
        );
    }

    public static SpinNotAllowedException rewardUnavailable() {
        return new SpinNotAllowedException(
            SpinRestrictionReason.REWARD_UNAVAILABLE.getDescription(),
            SpinRestrictionReason.REWARD_UNAVAILABLE
        );
    }

    public static SpinNotAllowedException quotaExceeded() {
        return new SpinNotAllowedException(
            SpinRestrictionReason.QUOTA_EXCEEDED.getDescription(),
            SpinRestrictionReason.QUOTA_EXCEEDED
        );
    }

    public static SpinNotAllowedException timeRestricted(String timeWindow) {
        return new SpinNotAllowedException(
            String.format("Spins are only allowed during %s", timeWindow),
            SpinRestrictionReason.TIME_RESTRICTED
        );
    }

    public static SpinNotAllowedException goldenHourInactive() {
        return new SpinNotAllowedException(
            SpinRestrictionReason.GOLDEN_HOUR_INACTIVE.getDescription(),
            SpinRestrictionReason.GOLDEN_HOUR_INACTIVE
        );
    }

    public static SpinNotAllowedException systemError(String details) {
        return new SpinNotAllowedException(
            String.format("System error: %s", details),
            SpinRestrictionReason.SYSTEM_ERROR
        );
    }
}
