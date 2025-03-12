package vn.com.fecredit.app.exception;

/**
 * Exception thrown when an entity cannot be found in the database.
 * Similar to ResourceNotFoundException but specifically for entity lookup failures.
 */
public class EntityNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new EntityNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new EntityNotFoundException with a formatted message for the specified entity type and identifier.
     *
     * @param entityType the type of entity that was not found (e.g., "User", "Event")
     * @param entityId the identifier of the entity that was not found
     * @return a new EntityNotFoundException with a formatted message
     */
    public static EntityNotFoundException forEntity(String entityType, Object entityId) {
        return new EntityNotFoundException(entityType + " not found with id: " + entityId);
    }

    /**
     * Constructs a new EntityNotFoundException with a formatted message for the specified entity type and field.
     *
     * @param entityType the type of entity that was not found (e.g., "User", "Event")
     * @param fieldName the name of the field used to look up the entity (e.g., "email", "code")
     * @param fieldValue the value of the field used to look up the entity
     * @return a new EntityNotFoundException with a formatted message
     */
    public static EntityNotFoundException forEntityWithField(String entityType, String fieldName, Object fieldValue) {
        return new EntityNotFoundException(entityType + " not found with " + fieldName + ": " + fieldValue);
    }
}