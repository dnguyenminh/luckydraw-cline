package vn.com.fecredit.app.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

@TestConfiguration
@Profile("test")
@RequiredArgsConstructor
public class TestDataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initialize() {
        createRoles();
        createUsers();
        createBlacklistedTokens();
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = Role.builder()
                    .name(RoleName.USER)
                    .status(EntityStatus.ACTIVE)
                    .build();

            Role adminRole = Role.builder()
                    .name(RoleName.ADMIN)
                    .status(EntityStatus.ACTIVE)
                    .build();

            roleRepository.saveAll(Arrays.asList(userRole, adminRole));
        }
    }

    private void createUsers() {
        if (userRepository.count() == 0) {
            Role userRole = roleRepository.findByName(RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User regularUser = User.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("password123"))
                    .email("testuser@example.com")
                    .fullName("Test User")
                    .roles(Set.of(userRole))
                    .status(EntityStatus.ACTIVE)
                    .build();

            User adminUser = User.builder()
                    .username("testadmin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("testadmin@example.com")
                    .fullName("Test Admin")
                    .roles(Set.of(adminRole))
                    .status(EntityStatus.ACTIVE)
                    .build();

            User inactiveUser = User.builder()
                    .username("inactive")
                    .password(passwordEncoder.encode("inactive123"))
                    .email("inactive@example.com")
                    .fullName("Inactive User")
                    .roles(Set.of(userRole))
                    .status(EntityStatus.INACTIVE)
                    .build();

            userRepository.saveAll(Arrays.asList(regularUser, adminUser, inactiveUser));
        }
    }

    private void createBlacklistedTokens() {
        if (blacklistedTokenRepository.count() == 0) {
            long now = Instant.now().toEpochMilli();

            BlacklistedToken activeToken = BlacklistedToken.builder()
                    .tokenHash("active_token_hash")
                    .expirationTime(now + 3600000) // expires in 1 hour
                    .revokedBy("testadmin")
                    .revocationReason("Test blacklist")
                    .refreshToken(false)
                    .build();

            BlacklistedToken expiredToken = BlacklistedToken.builder()
                    .tokenHash("expired_token_hash")
                    .expirationTime(now - 3600000) // expired 1 hour ago
                    .revokedBy("testadmin")
                    .revocationReason("Test expired")
                    .refreshToken(true)
                    .build();

            blacklistedTokenRepository.saveAll(Arrays.asList(activeToken, expiredToken));
        }
    }

    public void cleanUp() {
        blacklistedTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
