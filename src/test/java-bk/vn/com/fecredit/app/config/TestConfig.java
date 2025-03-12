package vn.com.fecredit.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authManager.authenticate(any())).thenAnswer(invocation -> {
            // Create a test authentication object
            return invocation.getArgument(0);
        });
        return authManager;
    }

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(createTestUser());
        when(userDetailsService.loadUserByUsername("testadmin")).thenReturn(createTestAdmin());
        return userDetailsService;
    }

    @Bean
    public User createTestUser() {
        Role userRole = Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .status(EntityStatus.ACTIVE)
                .build();

        return User.builder()
                .id(1L)
                .username("testuser")
                .password(testPasswordEncoder().encode("password123"))
                .email("testuser@example.com")
                .fullName("Test User")
                .roles(Set.of(userRole))
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Bean
    public User createTestAdmin() {
        Role adminRole = Role.builder()
                .id(2L)
                .name(RoleName.ADMIN)
                .status(EntityStatus.ACTIVE)
                .build();

        return User.builder()
                .id(2L)
                .username("testadmin")
                .password(testPasswordEncoder().encode("admin123"))
                .email("testadmin@example.com")
                .fullName("Test Admin")
                .roles(Set.of(adminRole))
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Bean
    @Primary
    public JwtProperties testJwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        props.setAccessExpiration(900000L); // 15 minutes
        props.setRefreshExpiration(86400000L); // 24 hours
        props.setIssuer("FE Credit");
        props.setAudience("FE Credit App");
        props.setHttpOnly(true);
        props.setSecure(false);
        props.setPath("/");
        props.setSameSite("Lax");
        return props;
    }

    @Bean
    public Role createUserRole() {
        return Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Bean
    public Role createAdminRole() {
        return Role.builder()
                .id(2L)
                .name(RoleName.ADMIN)
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Bean
    public Set<Role> createDefaultRoles() {
        return Collections.singleton(createUserRole());
    }

    @Bean
    public Set<Role> createAdminRoles() {
        return Collections.singleton(createAdminRole());
    }
}
