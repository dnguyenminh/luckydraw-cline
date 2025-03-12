package vn.com.fecredit.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

@SpringBootTest
@Import({TestSecurityConfig.class, TestMethodSecurityConfig.class})
class SecurityComponentsIntegrationTest {

    @Autowired
    private TestSecuredService securedService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TestSecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class RoleHierarchyIntegrationTests {
        @Test
        void verifyAdminInheritsAllRoles() {
            UserSecurityDTO admin = (UserSecurityDTO) userDetailsService.loadUserByUsername("admin");
            Set<String> authorities = admin.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            assertThat(authorities).contains(
                "ROLE_ADMIN",
                "ROLE_MANAGER",
                "ROLE_USER"
            );
        }

        @Test
        void verifyManagerInheritsUserRole() {
            UserSecurityDTO manager = (UserSecurityDTO) userDetailsService.loadUserByUsername("manager");
            Set<String> authorities = manager.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            assertThat(authorities).contains(
                "ROLE_MANAGER",
                "ROLE_USER"
            );
            assertThat(authorities).doesNotContain("ROLE_ADMIN");
        }
    }

    @Nested
    class SecurityContextIntegrationTests {
        @Test
        void verifySecurityContextPropagation() {
            // Set up authentication
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("admin");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Verify context is available in service
            String result = securedService.postAuthorizedMethod();
            assertThat(result).contains(userDetails.getUsername());
        }

        @Test
        void verifyAuthenticationCleanup() {
            // Set authentication
            authenticateAs("admin");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

            // Clear context
            SecurityContextHolder.clearContext();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    class UserSecurityIntegrationTests {
        @Test
        void verifyUserDetailsConversion() {
            User user = User.builder()
                    .username("tester")
                    .password("test123")
                    .roles(Set.of(Role.builder().name(RoleName.USER).build()))
                    .status(1)
                    .build();

            UserSecurityDTO userDetails = new UserSecurityDTO(user);
            
            assertThat(userDetails.getUsername()).isEqualTo("tester");
            assertThat(userDetails.isEnabled()).isTrue();
            assertThat(userDetails.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
        }

        @Test
        void verifyMethodSecurityWithUserDetails() {
            UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername("user");
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );

            assertThat(securedService.userOnlyMethod()).isEqualTo("User access granted");
            assertThatThrownBy(() -> securedService.adminOnlyMethod())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class RoleAuthorityIntegrationTests {
        @Test
        void verifyRoleAuthorityCompatibility() {
            UserSecurityDTO admin = (UserSecurityDTO) userDetailsService.loadUserByUsername("admin");
            RoleAuthority customAuthority = RoleAuthority.of(RoleName.ADMIN);

            assertThat(admin.getAuthorities())
                .anyMatch(auth -> auth.getAuthority().equals(customAuthority.getAuthority()));
        }

        @Test
        void verifyRoleAuthorityWithMethodSecurity() {
            authenticateAs("admin");
            assertThat(securedService.adminOnlyMethod())
                .isEqualTo("Admin access granted");
        }
    }

    private void authenticateAs(String username) {
        UserSecurityDTO userDetails = (UserSecurityDTO) userDetailsService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
