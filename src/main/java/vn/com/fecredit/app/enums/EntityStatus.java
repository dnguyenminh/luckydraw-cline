package vn.com.fecredit.app.enums;

import lombok.Getter;

/**
 * Enum representing possible entity statuses with database mappings
 */
@Getter
public enum EntityStatus {

    /**
     * Entity is pending approval/activation
     */
    PENDING(0),
    
    /**
     * Entity is active and available for use
     */
    ACTIVE(1),

    /**
     * Entity is inactive but can be reactivated
     */
    INACTIVE(2),

    /**
     * Entity has been soft deleted
     */
    DELETED(3);

    private final Integer value;

    EntityStatus(Integer value) {
        this.value = value;
    }

    /**
     * Get EntityStatus from integer value 
     * @param value Integer status value
     * @return Matching EntityStatus or null if not found
     */
    public static EntityStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }

        for (EntityStatus status : EntityStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        
        return null;
    }
}
