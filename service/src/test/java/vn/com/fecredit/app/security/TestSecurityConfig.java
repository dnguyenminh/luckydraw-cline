package vn.com.fecredit.app.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    private final Map<String, User> testUsers = new HashMap<>();

    public TestSecurityConfig() {
        // Initialize test users with different roles
        setupTestUsers();
    }

    private void setupTestUsers() {
        // Create roles
        Role adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .code("ADMIN")
                .build();

        Role userRole = Role.builder()
                .name(RoleName.USER)
                .code("USER")
                .build();

        Role managerRole = Role.builder()
                .name(RoleName.MANAGER)
                .code("MANAGER")
                .build();

        // Create test users
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .email("admin@test.com")
                .status(1)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        User user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .email("user@test.com")
                .status(1)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        User manager = User.builder()
                .username("manager")
                .password(passwordEncoder().encode("manager123"))
                .email("manager@test.com")
                .status(1)
                .roles(new HashSet<>(Set.of(managerRole, userRole)))
                .build();

        // Store users in map
        testUsers.put(admin.getUsername(), admin);
        testUsers.put(user.getUsername(), user);
        testUsers.put(manager.getUsername(), manager);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = testUsers.get(username);
            if (user == null) {
                throw new RuntimeException("User not found: " + username);
            }
            return new UserSecurityDTO(user);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Helper methods for tests
    public User getTestUser(String username) {
        return testUsers.get(username);
    }

    public void addTestUser(User user) {
        testUsers.put(user.getUsername(), user);
    }

    public void clearTestUsers() {
        testUsers.clear();
    }
}
