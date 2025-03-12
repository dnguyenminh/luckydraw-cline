package vn.com.fecredit.app.exception;

/**
 * Exception thrown when an operation is attempted that is not valid in the current context.
 * Used for business logic validation failures.
 */
public class InvalidOperationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new InvalidOperationException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidOperationException with a formatted message for the specified operation and reason.
     *
     * @param operation the operation that was attempted
     * @param reason the reason why the operation is invalid
     * @return a new InvalidOperationException with a formatted message
     */
    public static InvalidOperationException forOperation(String operation, String reason) {
        return new InvalidOperationException("Cannot perform operation '" + operation + "': " + reason);
    }

    /**
     * Constructs a new InvalidOperationException with a formatted message for the specified entity and operation.
     *
     * @param entityType the type of entity on which the operation was attempted
     * @param entityId the identifier of the entity
     * @param operation the operation that was attempted
     * @param reason the reason why the operation is invalid
     * @return a new InvalidOperationException with a formatted message
     */
    public static InvalidOperationException forEntityOperation(String entityType, Object entityId, String operation, String reason) {
        return new InvalidOperationException("Cannot perform '" + operation + "' on " + entityType + " with id: " + entityId + ". Reason: " + reason);
    }
}