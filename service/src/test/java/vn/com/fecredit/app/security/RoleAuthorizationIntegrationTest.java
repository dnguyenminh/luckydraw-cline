package vn.com.fecredit.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

@SpringBootTest
@Import(TestSecurityConfig.class)
class RoleAuthorizationIntegrationTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TestSecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class AuthenticationTests {
        @Test
        void testUserDetailsServiceLoadUser() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("admin");
            assertThat(userDetails.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_ADMIN");
        }

        @Test
        void testSecurityContextAuthentication() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("manager");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(currentAuth.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_MANAGER", "ROLE_USER");
        }
    }

    @Nested
    class RoleBasedAuthorizationTests {
        @Test
        void testAdminAccess() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("admin");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_ADMIN");
        }

        @Test
        void testUserAccess() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("user");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_USER")
                    .doesNotContain("ROLE_ADMIN");
        }

        @Test
        void testMultipleRoles() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("manager");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_MANAGER", "ROLE_USER");
        }
    }

    @Nested
    class RoleAuthorityTests {
        @Test
        void testCustomRoleAuthority() {
            Role adminRole = Role.builder()
                    .name(RoleName.ADMIN)
                    .code("ADMIN")
                    .build();

            Set<GrantedAuthority> authorities = Set.of(RoleAuthority.of(adminRole.getName()));
            Set<String> authorityStrings = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            assertThat(authorityStrings).containsExactly("ROLE_ADMIN");
        }

        @Test
        void testRoleAuthorityCompatibility() {
            RoleAuthority customAuthority = RoleAuthority.of(RoleName.ADMIN);
            SimpleGrantedAuthority springAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");

            assertThat(customAuthority.getAuthority()).isEqualTo(springAuthority.getAuthority());
        }
    }

    @Nested
    class UserSecurityTests {
        @Test
        void testUserDetailsConversion() {
            User user = User.builder()
                    .username("tester")
                    .password("test123")
                    .roles(new HashSet<>(Set.of(
                        Role.builder().name(RoleName.USER).build()
                    )))
                    .status(1)
                    .build();

            UserSecurityDTO userDetails = new UserSecurityDTO(user);
            
            assertThat(userDetails.getUsername()).isEqualTo("tester");
            assertThat(userDetails.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
            assertThat(userDetails.isEnabled()).isTrue();
        }

        @Test
        void testDisabledUserSecurity() {
            User user = User.builder()
                    .username("disabled")
                    .password("test123")
                    .status(0)
                    .build();

            UserSecurityDTO userDetails = new UserSecurityDTO(user);
            
            assertThat(userDetails.isEnabled()).isFalse();
            assertThat(userDetails.isAccountNonLocked()).isTrue();
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        }
    }
}
