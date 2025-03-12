package vn.com.fecredit.app.enums;

/**
 * UserType represents the fundamental classification of users in the system.
 * Unlike Roles which define specific permissions, UserType determines the user's
 * basic category and affects core system behavior and UI presentation.
 *
 * Key differences from Role:
 * 1. A user has exactly one UserType but can have multiple Roles
 * 2. UserType is more static and rarely changes
 * 3. UserType affects system-wide behavior and UI presentation
 * 4. Roles handle specific permission-based access control
 */
public enum UserType {
    /**
     * System administrators with full system access
     */
    ADMIN,

    /**
     * Internal staff members who manage events, rewards, etc.
     */
    STAFF,

    /**
     * External users who participate in events
     */
    CUSTOMER,

    /**
     * Temporary users with limited access
     */
    GUEST
}
