package vn.com.fecredit.app.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.security.RoleAuthority;

class UserSecurityDTOTest {

    private User user;
    private UserSecurityDTO userSecurity;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .status(1)
                .roles(new HashSet<>())
                .build();

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .code("ADMIN")
                .build();

        Role userRole = Role.builder()
                .name(RoleName.USER)
                .code("USER")
                .build();

        user.getRoles().add(adminRole);
        user.getRoles().add(userRole);

        userSecurity = new UserSecurityDTO(user);
    }

    @Nested
    class RoleTests {
        @Test
        void testGetAuthorities() {
            Set<String> expectedAuthorities = Set.of(
                RoleAuthority.toRoleString(RoleName.ADMIN),
                RoleAuthority.toRoleString(RoleName.USER)
            );

            Set<String> actualAuthorities = userSecurity.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.toSet());

            assertThat(actualAuthorities).isEqualTo(expectedAuthorities);
        }

        @Test
        void testHasRole() {
            assertThat(userSecurity.hasRole(RoleName.ADMIN)).isTrue();
            assertThat(userSecurity.hasRole(RoleName.USER)).isTrue();
            assertThat(userSecurity.hasRole(RoleName.MANAGER)).isFalse();
        }

        @Test
        void testHasRoleString() {
            assertThat(userSecurity.hasRole("ADMIN")).isTrue();
            assertThat(userSecurity.hasRole("USER")).isTrue();
            assertThat(userSecurity.hasRole("MANAGER")).isFalse();
            assertThat(userSecurity.hasRole("INVALID_ROLE")).isFalse();
            assertThat(userSecurity.hasRole((String) null)).isFalse();
        }

        @Test
        void testHasAnyRole() {
            assertThat(userSecurity.hasAnyRole(RoleName.ADMIN, RoleName.MANAGER)).isTrue();
            assertThat(userSecurity.hasAnyRole(RoleName.USER, RoleName.GUEST)).isTrue();
            assertThat(userSecurity.hasAnyRole(RoleName.MANAGER, RoleName.GUEST)).isFalse();
            assertThat(userSecurity.hasAnyRole()).isFalse();
        }

        @Test
        void testUserWithNoRoles() {
            User userWithNoRoles = User.builder()
                    .username("noroles")
                    .roles(new HashSet<>())
                    .build();
            UserSecurityDTO security = new UserSecurityDTO(userWithNoRoles);
            
            assertThat(security.getAuthorities()).isEmpty();
            assertThat(security.hasRole(RoleName.ADMIN)).isFalse();
            assertThat(security.hasRole("USER")).isFalse();
        }
    }

    @Nested
    class AccountStatusTests {
        @Test
        void testActiveAccount() {
            assertThat(userSecurity.isEnabled()).isTrue();
            assertThat(userSecurity.isAccountNonExpired()).isTrue();
            assertThat(userSecurity.isAccountNonLocked()).isTrue();
            assertThat(userSecurity.isCredentialsNonExpired()).isTrue();
            assertThat(userSecurity.isActive()).isTrue();
        }

        @Test
        void testDisabledAccount() {
            user.setStatus(0);
            UserSecurityDTO security = new UserSecurityDTO(user);
            
            assertThat(security.isEnabled()).isFalse();
            assertThat(security.isActive()).isFalse();
        }

        @Test
        void testLockedAccount() {
            user.setLockedUntil(LocalDateTime.now().plusDays(1));
            UserSecurityDTO security = new UserSecurityDTO(user);
            
            assertThat(security.isAccountNonLocked()).isFalse();
            assertThat(security.isActive()).isFalse();
        }

        @Test
        void testExpiredAccount() {
            user.setAccountExpired(true);
            UserSecurityDTO security = new UserSecurityDTO(user);
            
            assertThat(security.isAccountNonExpired()).isFalse();
            assertThat(security.isActive()).isFalse();
        }

        @Test
        void testExpiredCredentials() {
            user.setCredentialsExpired(true);
            UserSecurityDTO security = new UserSecurityDTO(user);
            
            assertThat(security.isCredentialsNonExpired()).isFalse();
        }
    }

    @Nested
    class UserDetailsTests {
        @Test
        void testBasicDetails() {
            assertThat(userSecurity.getUsername()).isEqualTo("testuser");
            assertThat(userSecurity.getPassword()).isEqualTo("password123");
            assertThat(userSecurity.getEmail()).isEqualTo("test@example.com");
            assertThat(userSecurity.getFullName()).isEqualTo("Test User");
        }

        @Test
        void testNullNames() {
            User userWithNullNames = User.builder()
                    .username("nullnames")
                    .firstName(null)
                    .lastName(null)
                    .build();
            UserSecurityDTO security = new UserSecurityDTO(userWithNullNames);
            
            assertThat(security.getFullName()).isEqualTo("nullnames");
        }

        @Test
        void testPartialNames() {
            User userWithFirstName = User.builder()
                    .username("partial")
                    .firstName("First")
                    .build();
            UserSecurityDTO security = new UserSecurityDTO(userWithFirstName);
            
            assertThat(security.getFullName()).isEqualTo("First");

            User userWithLastName = User.builder()
                    .username("partial")
                    .lastName("Last")
                    .build();
            security = new UserSecurityDTO(userWithLastName);
            
            assertThat(security.getFullName()).isEqualTo(" Last");
        }
    }
}
