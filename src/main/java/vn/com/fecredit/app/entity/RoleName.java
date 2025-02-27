package vn.com.fecredit.app.entity;

public enum RoleName {
    ROLE_ADMIN,           // System administrator with full access
    ROLE_MANAGER,         // Event/location manager 
    ROLE_STAFF,          // Staff member who can manage participants
    ROLE_PARTICIPANT,     // Regular participant in events
    ROLE_API,            // API access role
    ROLE_AUDIT,          // Audit/monitoring role
    ROLE_GUEST;          // Limited access guest role

    public static boolean isValid(String name) {
        try {
            RoleName.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static RoleName fromString(String name) {
        try {
            return RoleName.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role name: " + name);
        }
    }

    public boolean isAdminRole() {
        return this == ROLE_ADMIN;
    }

    public boolean isStaffRole() {
        return this == ROLE_STAFF || this == ROLE_MANAGER || isAdminRole();
    }

    public boolean isParticipantRole() {
        return this == ROLE_PARTICIPANT;
    }

    public boolean isSystemRole() {
        return this == ROLE_API || this == ROLE_AUDIT;
    }

    public boolean hasHigherPrivilegeThan(RoleName other) {
        if (this == ROLE_ADMIN) return true;
        if (this == ROLE_MANAGER && other != ROLE_ADMIN) return true;
        if (this == ROLE_STAFF && (other == ROLE_PARTICIPANT || other == ROLE_GUEST)) return true;
        return false;
    }

    @Override
    public String toString() {
        return name().substring(5).toLowerCase(); // Remove "ROLE_" prefix
    }
}
