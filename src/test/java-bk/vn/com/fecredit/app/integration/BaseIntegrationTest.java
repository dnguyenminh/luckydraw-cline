package vn.com.fecredit.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.config.TestDataInitializer;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for integration tests
 * Provides common functionality and configuration for all integration tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected TestDataInitializer testDataInitializer;

    @Autowired
    protected TokenBlacklistService tokenBlacklistService;

    protected Map<String, String> testTokens;

    @BeforeEach
    void setUp() {
        testDataInitializer.initialize();
        initializeTestTokens();
    }

    @AfterEach
    void tearDown() {
        testDataInitializer.cleanUp();
        entityManager.clear();
    }

    private void initializeTestTokens() {
        testTokens = new HashMap<>();

        // Generate tokens for test users
        User regularUser = userRepository.findByUsername("testuser")
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        User adminUser = userRepository.findByUsername("testadmin")
                .orElseThrow(() -> new RuntimeException("Test admin not found"));

        testTokens.put("userAccessToken", jwtService.generateToken(regularUser));
        testTokens.put("userRefreshToken", jwtService.generateToken(
                Map.of("refresh", true), regularUser));
        testTokens.put("adminAccessToken", jwtService.generateToken(adminUser));
        testTokens.put("adminRefreshToken", jwtService.generateToken(
                Map.of("refresh", true), adminUser));
    }

    /**
     * Get authorization header value for a specific role
     * @param role The role to get authorization for
     * @return Bearer token header value
     */
    protected String getAuthHeader(RoleName role) {
        String token = role == RoleName.ADMIN ? 
                testTokens.get("adminAccessToken") : 
                testTokens.get("userAccessToken");
        return "Bearer " + token;
    }

    /**
     * Get refresh token for a specific role
     * @param role The role to get refresh token for
     * @return Refresh token
     */
    protected String getRefreshToken(RoleName role) {
        return role == RoleName.ADMIN ? 
                testTokens.get("adminRefreshToken") : 
                testTokens.get("userRefreshToken");
    }

    /**
     * Clear persistence context and reload entity
     * @param entity Entity to reload
     * @param <T> Entity type
     * @return Reloaded entity
     */
    protected <T> T reloadEntity(T entity) {
        entityManager.clear();
        return entityManager.merge(entity);
    }

    /**
     * Flush and clear persistence context
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Convert object to JSON string
     * @param obj Object to convert
     * @return JSON string
     */
    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
