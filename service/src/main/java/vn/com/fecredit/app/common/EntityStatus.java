package vn.com.fecredit.app.common;

/**
 * Enum representing the status of entities in the system.
 * Used for filtering and status management across the application.
 */
public enum EntityStatus {
    ACTIVE(1),
    INACTIVE(0);
    
    private final int value;
    
    EntityStatus(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Convert an integer status value to the corresponding EntityStatus enum.
     * 
     * @param value The integer status value (typically 0 or 1)
     * @return The corresponding EntityStatus enum value
     */
    public static EntityStatus fromValue(int value) {
        for (EntityStatus status : EntityStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
    
    /**
     * Convert a boolean active flag to the corresponding EntityStatus enum.
     * 
     * @param active True for ACTIVE status, false for INACTIVE
     * @return The corresponding EntityStatus enum value
     */
    public static EntityStatus fromBoolean(boolean active) {
        return active ? ACTIVE : INACTIVE;
    }
}