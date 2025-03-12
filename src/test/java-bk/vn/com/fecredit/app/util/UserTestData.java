package vn.com.fecredit.app.util;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;

/**
 * Utility class for creating User test data.
 */
public final class UserTestData {

    // User Details
    public static final Long TEST_USER_ID = 1L;
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "password123";
    public static final String TEST_FIRST_NAME = "Test";
    public static final String TEST_LAST_NAME = "User";

    // Database Constants
    public static class Database {
        public static final String USERS_TABLE = "users";
        public static final String USER_ID_COL = "id";
        public static final String USER_EMAIL_COL = "email";
        public static final String USER_PASSWORD_COL = "password";
        public static final String USER_FIRST_NAME_COL = "first_name";
        public static final String USER_LAST_NAME_COL = "last_name";
        public static final String USER_ENABLED_COL = "enabled";
        public static final String USER_CREATED_AT_COL = "created_at";
        public static final String USER_UPDATED_AT_COL = "updated_at";
    }

    private UserTestData() {
        // Prevent instantiation
    }

    public static User createDefaultUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setEnabled(true);
        user.setRoles(RoleTestData.createDefaultRoles());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        return user;
    }

    public static User createTestUser() {
        return createDefaultUser();
    }

    public static User createUserWithRole(Role role) {
        User user = createDefaultUser();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return user;
    }
}