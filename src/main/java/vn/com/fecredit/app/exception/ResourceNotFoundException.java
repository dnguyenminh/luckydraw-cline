package vn.com.fecredit.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public static ResourceNotFoundException event(Long id) {
        return new ResourceNotFoundException("Event", "id", id);
    }

    public static ResourceNotFoundException eventLocation(Long id) {
        return new ResourceNotFoundException("EventLocation", "id", id);
    }

    public static ResourceNotFoundException participant(Long id) {
        return new ResourceNotFoundException("Participant", "id", id);
    }

    public static ResourceNotFoundException reward(Long id) {
        return new ResourceNotFoundException("Reward", "id", id);
    }

    public static ResourceNotFoundException goldenHour(Long id) {
        return new ResourceNotFoundException("GoldenHour", "id", id);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User", "id", id);
    }

    public static ResourceNotFoundException userByUsername(String username) {
        return new ResourceNotFoundException("User", "username", username);
    }

    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }

    public static ResourceNotFoundException role(Long id) {
        return new ResourceNotFoundException("Role", "id", id);
    }

    public static ResourceNotFoundException roleByName(String name) {
        return new ResourceNotFoundException("Role", "name", name);
    }

    public static ResourceNotFoundException spinHistory(Long id) {
        return new ResourceNotFoundException("SpinHistory", "id", id);
    }

    public static ResourceNotFoundException participantByCustomerId(String customerId) {
        return new ResourceNotFoundException("Participant", "customerId", customerId);
    }

    public static ResourceNotFoundException participantByCardNumber(String cardNumber) {
        return new ResourceNotFoundException("Participant", "cardNumber", cardNumber);
    }

    public static ResourceNotFoundException eventByCode(String code) {
        return new ResourceNotFoundException("Event", "code", code);
    }

    public static ResourceNotFoundException rewardByCode(String code) {
        return new ResourceNotFoundException("Reward", "code", code);
    }
}
