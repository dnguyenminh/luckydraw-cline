package vn.com.fecredit.app.enums;

/**
 * Enumeration of system role names
 */
public enum RoleName {

    /**
     * System administrator with full access
     */
    ADMIN,

    /**
     * Regular user with standard permissions
     */ 
    USER,

    /**
     * Manager role with elevated permissions
     */
    MANAGER,

    /**
     * Event organizer role
     */
    EVENT_ORGANIZER,

    /**
     * Location manager role
     */
    LOCATION_MANAGER,

    /**
     * Event participant role
     */
    PARTICIPANT;

    /**
     * Check if role has administration privileges
     * @param role Role to check
     * @return True if role has admin privileges
     */
    public static boolean isAdmin(RoleName role) {
        return ADMIN.equals(role);
    }

    /**
     * Check if role has manager privileges
     * @param role Role to check  
     * @return True if role has manager privileges
     */
    public static boolean isManager(RoleName role) {
        return MANAGER.equals(role) || isAdmin(role);
    }

    /**
     * Check if role has organizer privileges
     * @param role Role to check
     * @return True if role has organizer privileges 
     */
    public static boolean isOrganizer(RoleName role) {
        return EVENT_ORGANIZER.equals(role) || isManager(role);
    }
    
    /**
     * Check if role has location manager privileges
     * @param role Role to check
     * @return True if role has location manager privileges
     */
    public static boolean isLocationManager(RoleName role) {
        return LOCATION_MANAGER.equals(role) || isManager(role);
    }
    
    /**
     * Check if role is a participant role
     * @param role Role to check
     * @return True if role is participant
     */
    public static boolean isParticipant(RoleName role) {
        return PARTICIPANT.equals(role);
    }

}
