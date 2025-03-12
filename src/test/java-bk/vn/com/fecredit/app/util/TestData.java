package vn.com.fecredit.app.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.com.fecredit.app.dto.AuthRequest;
import vn.com.fecredit.app.dto.UserInfoDto;
import vn.com.fecredit.app.model.User;
import vn.com.fecredit.app.model.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Centralized test data creation utility.
 * Provides factory methods and constants for test data.
 */
public final class TestData {

    // Test user constants
    public static final Long TEST_USER_ID = 1L;
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "password123";
    public static final String TEST_ENCODED_PASSWORD = new BCryptPasswordEncoder().encode(TEST_PASSWORD);
    public static final String TEST_FIRST_NAME = "Test";
    public static final String TEST_LAST_NAME = "User";

    // Test role constants
    public static final Long USER_ROLE_ID = 1L;
    public static final Long ADMIN_ROLE_ID = 2L;
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String USER_ROLE_DESC = "Regular user role";
    public static final String ADMIN_ROLE_DESC = "Administrator role";

    private TestData() {
        // Prevent instantiation
    }

    public static AuthRequest createAuthRequest() {
        return AuthRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();
    }

    public static UserInfoDto createUserInfoDto() {
        return UserInfoDto.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
    }

    public static Role createUserRole() {
        Role role = new Role();
        role.setId(USER_ROLE_ID);
        role.setName(ROLE_USER);
        role.setDescription(USER_ROLE_DESC);
        return role;
    }

    public static Set<Role> createDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(createUserRole());
        return roles;
    }

    public static User createDefaultUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_ENCODED_PASSWORD);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setEnabled(true);
        user.setRoles(createDefaultRoles());
        return user;
    }
}