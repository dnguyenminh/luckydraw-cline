package vn.com.fecredit.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts EntityStatus enum to/from database column
 */
@Converter(autoApply = true)
public class EntityStatusConverter implements AttributeConverter<EntityStatus, String> {

    @Override
    public String convertToDatabaseColumn(EntityStatus status) {
        if (status == null) {
            return EntityStatus.ACTIVE.getCode();
        }
        return status.getCode();
    }

    @Override
    public EntityStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return EntityStatus.ACTIVE;
        }

        try {
            return EntityStatus.fromCode(code);
        } catch (IllegalArgumentException e) {
            // Log warning about unknown status code
            return EntityStatus.ACTIVE;
        }
    }

    /**
     * Helper method to safely convert status string to enum
     */
    public static EntityStatus parseStatusSafely(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return EntityStatus.ACTIVE;
        }

        try {
            return EntityStatus.fromCode(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Log warning about invalid status
            return EntityStatus.ACTIVE;
        }
    }

    /**
     * Helper method to determine if a status string is valid
     */
    public static boolean isValidStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return false;
        }

        try {
            EntityStatus.fromCode(statusStr.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Helper method to determine if transition is allowed
     */
    public static boolean canTransition(String fromStatus, String toStatus) {
        try {
            EntityStatus current = parseStatusSafely(fromStatus);
            EntityStatus target = parseStatusSafely(toStatus);
            return current.canTransitionTo(target);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to get default status when value is invalid
     */
    public static EntityStatus getDefaultStatus() {
        return EntityStatus.ACTIVE;
    }
}
