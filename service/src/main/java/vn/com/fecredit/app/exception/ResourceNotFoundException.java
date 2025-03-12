package vn.com.fecredit.app.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 * Used for handling 404-like scenarios in the application.
 */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with a formatted message for the specified resource type and identifier.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Event")
     * @param resourceId the identifier of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with id: " + resourceId);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message for the specified resource type and field.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Event")
     * @param fieldName the name of the field used to look up the resource (e.g., "email", "code")
     * @param fieldValue the value of the field used to look up the resource
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(resourceType + " not found with " + fieldName + ": " + fieldValue);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message for the specified resource type and identifier.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Event")
     * @param resourceId the identifier of the resource that was not found
     * @return a new ResourceNotFoundException with a formatted message
     */
    public static ResourceNotFoundException forResource(String resourceType, Object resourceId) {
        return new ResourceNotFoundException(resourceType + " not found with id: " + resourceId);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message for the specified resource type and field.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Event")
     * @param fieldName the name of the field used to look up the resource (e.g., "email", "code")
     * @param fieldValue the value of the field used to look up the resource
     * @return a new ResourceNotFoundException with a formatted message
     */
    public static ResourceNotFoundException forResourceWithField(String resourceType, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(resourceType + " not found with " + fieldName + ": " + fieldValue);
    }
}