package vn.com.fecredit.app.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RoleName defines specific permissions and access rights in the system.
 * Unlike UserType which defines user classification, RoleName determines
 * what actions a user can perform.
 *
 * The "ROLE_" prefix is required by Spring Security for role-based authorization.
 * Use format: hasRole('ADMIN') in @PreAuthorize annotations.
 */
public enum RoleName implements GrantedAuthority {

    // Administrative roles
    ROLE_ADMIN("Full system access and management capabilities"),
    ROLE_EVENT_MANAGER("Create and manage events, locations, and settings"),
    ROLE_REWARD_MANAGER("Manage rewards, prize distributions, and inventory"),
    ROLE_PARTICIPANT_MANAGER("Manage participant registrations and profiles"),
    
    // Operational roles
    ROLE_REPORT_VIEWER("View system reports and statistics"),
    ROLE_OPERATOR("Basic system operations and support"),
    ROLE_MODERATOR("Content moderation and user management"),
    ROLE_MANAGER("Department-level management capabilities"),
    ROLE_VIEWER("View-only access to system content"),
    
    // User roles
    ROLE_USER("Basic authenticated user access"),
    ROLE_PREMIUM_USER("Enhanced user privileges"),
    ROLE_PARTICIPANT("Event participation access"),
    ROLE_PREMIUM_PARTICIPANT("Enhanced participant privileges"),
    
    // Base role
    ROLE_GUEST("Limited access for unauthenticated users");

    // Static role references for tests and constants
    public static final RoleName ADMIN = ROLE_ADMIN;
    public static final RoleName USER = ROLE_USER;
    public static final RoleName GUEST = ROLE_GUEST;
    public static final RoleName MANAGER = ROLE_MANAGER;
    public static final RoleName VIEWER = ROLE_VIEWER;
    public static final RoleName OPERATOR = ROLE_OPERATOR;
    public static final RoleName MODERATOR = ROLE_MODERATOR;
    public static final RoleName PARTICIPANT = ROLE_PARTICIPANT;

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getAuthority() {
        return name();
    }

    public String getDisplayName() {
        return name().substring(5); // Remove "ROLE_" prefix
    }

    public boolean hasHigherPrivilegesThan(RoleName other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isAdminRole() {
        return this == ROLE_ADMIN || this == ROLE_EVENT_MANAGER || 
               this == ROLE_REWARD_MANAGER || this == ROLE_MANAGER;
    }

    public boolean isOperationalRole() {
        return this == ROLE_OPERATOR || this == ROLE_MODERATOR || 
               this == ROLE_REPORT_VIEWER || this == ROLE_VIEWER;
    }

    public boolean isParticipantRole() {
        return this == ROLE_USER || this == ROLE_PARTICIPANT || 
               this == ROLE_PREMIUM_PARTICIPANT || this == ROLE_GUEST;
    }

    public boolean isPremiumRole() {
        return this == ROLE_PREMIUM_USER || this == ROLE_PREMIUM_PARTICIPANT;
    }

    public static RoleName fromString(String role) {
        if (role == null) {
            return null;
        }
        String normalizedRole = role.toUpperCase();
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }
        return RoleName.valueOf(normalizedRole);
    }

    public static Set<RoleName> fromStrings(String... roles) {
        return Arrays.stream(roles)
                    .map(RoleName::fromString)
                    .collect(Collectors.toSet());
    }

    public static Set<RoleName> getAdminRoles() {
        return Arrays.stream(values())
                    .filter(RoleName::isAdminRole)
                    .collect(Collectors.toSet());
    }

    public static Set<RoleName> getUserRoles() {
        return Arrays.stream(values())
                    .filter(RoleName::isParticipantRole)
                    .collect(Collectors.toSet());
    }

    public static Set<RoleName> getOperationalRoles() {
        return Arrays.stream(values())
                    .filter(RoleName::isOperationalRole)
                    .collect(Collectors.toSet());
    }

    /**
     * Get default roles for a user type
     */
    public static Set<RoleName> getDefaultRolesForType(UserType type) {
        switch (type) {
            case ADMIN:
                return getAdminRoles();
            case STAFF:
                return getOperationalRoles();
            case CUSTOMER:
                return Set.of(ROLE_USER);
            case GUEST:
            default:
                return Set.of(ROLE_GUEST);
        }
    }
}
