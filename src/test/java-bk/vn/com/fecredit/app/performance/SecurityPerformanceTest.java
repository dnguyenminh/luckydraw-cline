package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityPerformanceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private UserService userService;

    private User testUser;
    private List<String> testTokens;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        testTokens = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    void tokenGeneration_Performance() {
        // Given
        int tokenCount = 1000;
        List<Long> generationTimes = new ArrayList<>();

        // When
        for (int i = 0; i < tokenCount; i++) {
            Instant start = Instant.now();
            String token = jwtService.generateToken(testUser);
            long duration = Duration.between(start, Instant.now()).toMillis();
            generationTimes.add(duration);
            testTokens.add(token);
        }

        // Then
        double averageTime = generationTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("Average token generation time: " + averageTime + "ms");
        assertThat(averageTime).isLessThan(10); // Should be less than 10ms
    }

    @Test
    void tokenValidation_Performance() {
        // Given
        int validationCount = 1000;
        String token = jwtService.generateToken(testUser);
        List<Long> validationTimes = new ArrayList<>();

        // When
        for (int i = 0; i < validationCount; i++) {
            Instant start = Instant.now();
            jwtService.isTokenValid(token, testUser);
            long duration = Duration.between(start, Instant.now()).toMillis();
            validationTimes.add(duration);
        }

        // Then
        double averageTime = validationTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("Average token validation time: " + averageTime + "ms");
        assertThat(averageTime).isLessThan(5); // Should be less than 5ms
    }

    @Test
    void tokenBlacklisting_ConcurrentPerformance() throws Exception {
        // Given
        int tokenCount = 100;
        int threadCount = 10;
        List<String> tokens = IntStream.range(0, tokenCount)
                .mapToObj(i -> jwtService.generateToken(testUser))
                .collect(Collectors.toList());

        // When
        Instant start = Instant.now();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                tokenBlacklistService.blacklist(token, false, 
                        System.currentTimeMillis() + 3600000,
                        "testuser", "performance_test");
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long duration = Duration.between(start, Instant.now()).toMillis();

        // Then
        System.out.println("Total time for concurrent blacklisting: " + duration + "ms");
        System.out.println("Average time per token: " + (double) duration / tokenCount + "ms");

        long blacklistedCount = blacklistedTokenRepository.count();
        assertThat(blacklistedCount).isEqualTo(tokenCount);
    }

    @Test
    void tokenBlacklist_LookupPerformance() {
        // Given
        int lookupCount = 1000;
        String token = jwtService.generateToken(testUser);
        tokenBlacklistService.blacklist(token, false, 
                System.currentTimeMillis() + 3600000,
                "testuser", "performance_test");
        List<Long> lookupTimes = new ArrayList<>();

        // When
        for (int i = 0; i < lookupCount; i++) {
            Instant start = Instant.now();
            tokenBlacklistService.isBlacklisted(token);
            long duration = Duration.between(start, Instant.now()).toMillis();
            lookupTimes.add(duration);
        }

        // Then
        double averageTime = lookupTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("Average blacklist lookup time: " + averageTime + "ms");
        assertThat(averageTime).isLessThan(5); // Should be less than 5ms
    }

    @Test
    void tokenBlacklist_CleanupPerformance() {
        // Given
        int tokenCount = 1000;
        long now = System.currentTimeMillis();
        List<BlacklistedToken> expiredTokens = IntStream.range(0, tokenCount)
                .mapToObj(i -> BlacklistedToken.builder()
                        .tokenHash("test_token_" + i)
                        .expirationTime(now - 3600000) // 1 hour ago
                        .revokedBy("testuser")
                        .revocationReason("performance_test")
                        .refreshToken(false)
                        .build())
                .collect(Collectors.toList());
        blacklistedTokenRepository.saveAll(expiredTokens);

        // When
        Instant start = Instant.now();
        int deletedCount = tokenBlacklistService.cleanupExpiredTokens();
        long duration = Duration.between(start, Instant.now()).toMillis();

        // Then
        System.out.println("Cleanup time for " + tokenCount + " tokens: " + duration + "ms");
        System.out.println("Average time per token: " + (double) duration / tokenCount + "ms");
        assertThat(deletedCount).isEqualTo(tokenCount);
        assertThat(blacklistedTokenRepository.count()).isEqualTo(0);
    }
}
