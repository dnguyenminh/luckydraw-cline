package vn.com.fecredit.app.repository.test;

import java.time.LocalDateTime;
import java.util.HashSet;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

public class TestDataFactory {

    public static User createDefaultUser() {
        return User.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .position("Tester")
                .status(1)
                .credentialsExpired(false)
                .accountExpired(false)
                .lockedUntil(null)
                .roles(new HashSet<>())
                .build();
    }

    public static User createDisabledUser() {
        return User.builder()
                .username("disabled")
                .password("password123")
                .email("disabled@example.com")
                .firstName("Disabled")
                .lastName("User")
                .phoneNumber("1234567890")
                .status(0)
                .build();
    }

    public static User createLockedUser() {
        return User.builder()
                .username("locked")
                .password("password123")
                .email("locked@example.com")
                .firstName("Locked")
                .lastName("User")
                .lockedUntil(LocalDateTime.now().plusDays(1))
                .status(1)
                .build();
    }

    public static Role createAdminRole() {
        return Role.builder()
                .name(RoleName.ADMIN)
                .code("ADMIN")
                .description("Administrator role")
                .priority(100)
                .status(1)
                .build();
    }

    public static Role createUserRole() {
        return Role.builder()
                .name(RoleName.USER)
                .code("USER")
                .description("Regular user role")
                .priority(1)
                .status(1)
                .build();
    }

    public static Role createManagerRole() {
        return Role.builder()
                .name(RoleName.MANAGER)
                .code("MANAGER")
                .description("Manager role")
                .priority(50)
                .status(1)
                .build();
    }

    public static Role createOperatorRole() {
        return Role.builder()
                .name(RoleName.OPERATOR)
                .code("OPERATOR")
                .description("Operator role")
                .priority(10)
                .status(1)
                .build();
    }

    public static Role createInactiveRole() {
        return Role.builder()
                .name(RoleName.GUEST)
                .code("GUEST")
                .description("Guest role")
                .priority(0)
                .status(0)
                .build();
    }
}
