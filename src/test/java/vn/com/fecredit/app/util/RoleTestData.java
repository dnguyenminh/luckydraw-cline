package vn.com.fecredit.app.util;

import vn.com.fecredit.app.model.Role;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating Role test data.
 */
public final class RoleTestData {

    // Role Names
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Role IDs
    public static final Long USER_ROLE_ID = 1L;
    public static final Long ADMIN_ROLE_ID = 2L;

    // Role Descriptions
    public static final String USER_ROLE_DESC = "Regular user role";
    public static final String ADMIN_ROLE_DESC = "Administrator role";

    // Database Constants
    public static class Database {
        public static final String ROLES_TABLE = "roles";
        public static final String ROLE_ID_COL = "id";
        public static final String ROLE_NAME_COL = "name";
        public static final String ROLE_DESC_COL = "description";
    }

    private RoleTestData() {
        // Prevent instantiation
    }

    public static Role createUserRole() {
        Role role = new Role();
        role.setId(USER_ROLE_ID);
        role.setName(ROLE_USER);
        role.setDescription(USER_ROLE_DESC);
        return role;
    }

    public static Role createAdminRole() {
        Role role = new Role();
        role.setId(ADMIN_ROLE_ID);
        role.setName(ROLE_ADMIN);
        role.setDescription(ADMIN_ROLE_DESC);
        return role;
    }

    public static Set<Role> createDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(createUserRole());
        roles.add(createAdminRole());
        return roles;
    }

    public static Set<String> createDefaultRoleNames() {
        Set<String> roles = new HashSet<>();
        roles.add(ROLE_USER);
        roles.add(ROLE_ADMIN);
        return roles;
    }
}