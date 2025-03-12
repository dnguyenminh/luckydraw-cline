package vn.com.fecredit.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.fecredit.app.dto.UserSecurityDTO;

@SpringBootTest
@Import({TestSecurityConfig.class, TestMethodSecurityConfig.class})
class MethodSecurityTest {

    @Autowired
    private TestSecuredService securedService;

    @Autowired
    private TestSecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class RoleBasedAccessTests {
        @Test
        void adminCanAccessAllMethods() {
            authenticateAs("admin");
            assertThat(securedService.adminOnlyMethod()).isEqualTo("Admin access granted");
            assertThat(securedService.managerOrAdminMethod()).isEqualTo("Manager/Admin access granted");
            assertThat(securedService.userOnlyMethod()).isEqualTo("User access granted"); // Due to role hierarchy
        }

        @Test
        void userCannotAccessAdminMethod() {
            authenticateAs("user");
            assertThatThrownBy(() -> securedService.adminOnlyMethod())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class ParameterValidationTests {
        @Test
        void adminCanAccessWithValidAmount() {
            authenticateAs("admin");
            assertThat(securedService.methodWithParameterValidation(500))
                .isEqualTo("Parameter validation passed for amount: 500.0");
        }

        @Test
        void adminCannotAccessWithInvalidAmount() {
            authenticateAs("admin");
            assertThatThrownBy(() -> securedService.methodWithParameterValidation(1500))
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class PostAuthorizationTests {
        @Test
        void managerCanAccessPostAuthorizedMethod() {
            authenticateAs("manager");
            String result = securedService.postAuthorizedMethod();
            assertThat(result).contains("manager");
        }

        @Test
        void adminCannotAccessPostAuthorizedMethod() {
            authenticateAs("admin");
            assertThatThrownBy(() -> securedService.postAuthorizedMethod())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class BeanReferenceTests {
        @Test
        void userCanAccessWithValidBeanReference() {
            authenticateAs("user");
            assertThat(securedService.methodWithBeanReference())
                .isEqualTo("Bean reference validation passed");
        }

        @Test
        void unknownUserCannotAccessWithBeanReference() {
            authenticateAs("unknown");
            assertThatThrownBy(() -> securedService.methodWithBeanReference())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class SelfAccessTests {
        @Test
        void userCanAccessOwnData() {
            String username = "user";
            authenticateAs(username);
            assertThat(securedService.selfAccessOnly(username))
                .isEqualTo("Self access granted for: user");
        }

        @Test
        void userCannotAccessOtherUserData() {
            authenticateAs("user");
            assertThatThrownBy(() -> securedService.selfAccessOnly("admin"))
                .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void adminCanAccessAnyUserData() {
            authenticateAs("admin");
            assertThat(securedService.adminOrSelfAccess("user"))
                .isEqualTo("Admin or self access granted for: user");
        }
    }

    @Nested
    class PreAndPostAuthorizationTests {
        @Test
        void adminCanPassBothChecks() {
            authenticateAs("admin");
            assertThat(securedService.methodWithPreAndPostAuthorization())
                .isEqualTo("Admin operation completed");
        }

        @Test
        void userCannotAccessDueToPreAuthorization() {
            authenticateAs("user");
            assertThatThrownBy(() -> securedService.methodWithPreAndPostAuthorization())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class AuthenticationTests {
        @Test
        void authenticatedUserCanAccessBasicMethod() {
            authenticateAs("user");
            assertThat(securedService.authenticatedOnlyMethod())
                .isEqualTo("Authenticated access granted");
        }

        @Test
        void unauthenticatedUserCannotAccess() {
            assertThatThrownBy(() -> securedService.authenticatedOnlyMethod())
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    private void authenticateAs(String username) {
        UserSecurityDTO userDetails = (UserSecurityDTO) securityConfig.userDetailsService()
                .loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
