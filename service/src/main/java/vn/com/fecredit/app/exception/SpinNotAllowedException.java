package vn.com.fecredit.app.exception;

/**
 * Exception thrown when a spin operation is not allowed due to business rules.
 * Used for enforcing spin limits and other spin-related validations.
 */
public class SpinNotAllowedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new SpinNotAllowedException with the specified detail message.
     *
     * @param message the detail message
     */
    public SpinNotAllowedException(String message) {
        super(message);
    }

    /**
     * Constructs a new SpinNotAllowedException with a formatted message for the specified reason.
     *
     * @param reason the reason why the spin is not allowed
     * @return a new SpinNotAllowedException with a formatted message
     */
    public static SpinNotAllowedException forReason(String reason) {
        return new SpinNotAllowedException("Spin not allowed: " + reason);
    }

    /**
     * Constructs a new SpinNotAllowedException with a formatted message for the specified participant and reason.
     *
     * @param participantId the identifier of the participant attempting to spin
     * @param reason the reason why the spin is not allowed
     * @return a new SpinNotAllowedException with a formatted message
     */
    public static SpinNotAllowedException forParticipant(Long participantId, String reason) {
        return new SpinNotAllowedException("Participant with id: " + participantId + " cannot spin. Reason: " + reason);
    }

    /**
     * Constructs a new SpinNotAllowedException with a formatted message for daily limit reached.
     *
     * @param participantId the identifier of the participant attempting to spin
     * @param dailyLimit the daily spin limit
     * @return a new SpinNotAllowedException with a formatted message
     */
    public static SpinNotAllowedException forDailyLimitReached(Long participantId, int dailyLimit) {
        return new SpinNotAllowedException("Participant with id: " + participantId + " has reached the daily spin limit of " + dailyLimit);
    }

    /**
     * Constructs a new SpinNotAllowedException with a formatted message for no remaining spins.
     *
     * @param participantId the identifier of the participant attempting to spin
     * @return a new SpinNotAllowedException with a formatted message
     */
    public static SpinNotAllowedException forNoRemainingSpins(Long participantId) {
        return new SpinNotAllowedException("Participant with id: " + participantId + " has no remaining spins");
    }
}