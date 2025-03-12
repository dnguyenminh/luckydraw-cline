package vn.com.fecredit.app.util;

/**
 * Centralized constants for tests.
 */
public final class TestConstants {

    private TestConstants() {
        // Prevent instantiation
    }

    /**
     * Test profile and environment constants.
     */
    public static final String TEST_PROFILE = "test";

    /**
     * API-related constants.
     */
    public static class Api {
        public static final String API_BASE = "/api";
        public static final String AUTH_BASE = API_BASE + "/auth";
        public static final String LOGIN_ENDPOINT = AUTH_BASE + "/login";
        public static final String USERS_ENDPOINT = API_BASE + "/users";
        public static final String ROLES_ENDPOINT = API_BASE + "/roles";
        public static final String REWARDS_ENDPOINT = API_BASE + "/rewards";
    }

    /**
     * Authentication-related constants.
     */
    public static class Auth {
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String TEST_JWT_SECRET = "verySecretKeyForTestingPurposesOnlyDoNotUseInProduction";
        public static final String INVALID_TOKEN = 
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxNTE2MjM5MDIyfQ." +
            "thisisaninvalidsignature";
        public static final long TOKEN_EXPIRATION = 3600000; // 1 hour
        public static final String TEST_EMAIL = "test@example.com";
        public static final String TEST_PASSWORD = "password123";
    }

    /**
     * Database-related constants.
     */
    public static class Database {
        public static final String USERS_TABLE = "users";
        public static final String ROLES_TABLE = "roles";
        public static final String USER_ROLES_TABLE = "user_roles";
        public static final String REWARDS_TABLE = "rewards";

        // User columns
        public static final String USER_ID_COL = "id";
        public static final String USER_EMAIL_COL = "email";
        public static final String USER_PASSWORD_COL = "password";
        public static final String USER_FIRST_NAME_COL = "first_name";
        public static final String USER_LAST_NAME_COL = "last_name";
        public static final String USER_ENABLED_COL = "enabled";

        // Role columns
        public static final String ROLE_ID_COL = "id";
        public static final String ROLE_NAME_COL = "name";
        public static final String ROLE_DESC_COL = "description";
    }

    /**
     * Test user data constants.
     */
    public static class TestUser {
        public static final Long USER_ID = 1L;
        public static final String FIRST_NAME = "Test";
        public static final String LAST_NAME = "User";
        public static final String ENCODED_PASSWORD = "$2a$10$sMQAEI2oYvmF.lUkA7v2/.sJzoZsyz6yOGHHWeGV3NQS/FxNlLE4a";
    }
}