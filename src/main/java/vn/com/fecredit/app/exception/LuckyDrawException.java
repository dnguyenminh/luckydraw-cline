package vn.com.fecredit.app.exception;

public class LuckyDrawException extends RuntimeException {
    private final String errorCode;

    public LuckyDrawException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public LuckyDrawException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LuckyDrawException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class EventNotFoundException extends LuckyDrawException {
        public EventNotFoundException(String message) {
            super(message, "EVENT_NOT_FOUND");
        }
    }

    public static class RewardNotFoundException extends LuckyDrawException {
        public RewardNotFoundException(String message) {
            super(message, "REWARD_NOT_FOUND");
        }
    }

    public static class ParticipantNotFoundException extends LuckyDrawException {
        public ParticipantNotFoundException(String message) {
            super(message, "PARTICIPANT_NOT_FOUND");
        }
    }

    public static class InsufficientSpinsException extends LuckyDrawException {
        public InsufficientSpinsException(String message) {
            super(message, "INSUFFICIENT_SPINS");
        }
    }

    public static class NoRewardsAvailableException extends LuckyDrawException {
        public NoRewardsAvailableException(String message) {
            super(message, "NO_REWARDS_AVAILABLE");
        }
    }

    public static class InvalidDataException extends LuckyDrawException {
        public InvalidDataException(String message) {
            super(message, "INVALID_DATA");
        }
    }

    public static class DuplicateResourceException extends LuckyDrawException {
        public DuplicateResourceException(String message) {
            super(message, "DUPLICATE_RESOURCE");
        }
    }

    public static class ResourceNotFoundException extends LuckyDrawException {
        public ResourceNotFoundException(String message) {
            super(message, "RESOURCE_NOT_FOUND");
        }
    }

    public static class InvalidFileFormatException extends LuckyDrawException {
        public InvalidFileFormatException(String message) {
            super(message, "INVALID_FILE_FORMAT");
        }
    }

    public static class InvalidDateRangeException extends LuckyDrawException {
        public InvalidDateRangeException(String message) {
            super(message, "INVALID_DATE_RANGE");
        }
    }

    public static class EventNotActiveException extends LuckyDrawException {
        public EventNotActiveException(String message) {
            super(message, "EVENT_NOT_ACTIVE");
        }
    }

    public static class UnauthorizedAccessException extends LuckyDrawException {
        public UnauthorizedAccessException(String message) {
            super(message, "UNAUTHORIZED_ACCESS");
        }
    }
}