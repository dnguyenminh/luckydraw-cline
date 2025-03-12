package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityThreadProfileTest {

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void testConcurrentTokenOperations() throws Exception {
        // Setup
        User testUser = (User) userService.loadUserByUsername("testuser");
        ExecutorService executor = null;
        ConcurrentHashMap<Integer, Long> times = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(1000);
        long startTime = System.currentTimeMillis();
        
        try {
            executor = Executors.newFixedThreadPool(20);
            
            // Run concurrent operations
            IntStream.range(0, 1000).forEach(i -> 
                executor.submit(() -> {
                    try {
                        long start = System.nanoTime();
                        
                        String token = jwtService.generateToken(testUser);
                        jwtService.isTokenValid(token, testUser);
                        tokenBlacklistService.blacklist(
                            token, 
                            false, 
                            System.currentTimeMillis() + 3600000,
                            "testuser",
                            "test"
                        );
                        
                        times.put(i, System.nanoTime() - start);
                    } finally {
                        latch.countDown();
                    }
                })
            );

            // Wait for completion
            boolean completed = latch.await(5, TimeUnit.MINUTES);
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Calculate metrics
            double avgMs = times.values().stream()
                .mapToDouble(t -> t / 1_000_000.0)
                .average()
                .orElse(0.0);
                
            double opsPerSec = completed ? (1000 * 1000.0 / totalTime) : 0.0;

            // Log results
            System.out.printf("""
                Performance Test Results:
                ======================
                Completed: %b
                Total Time: %d ms
                Average Time: %.2f ms
                Operations/sec: %.2f
                Sample Size: %d
                """,
                completed,
                totalTime,
                avgMs,
                opsPerSec,
                times.size()
            );

            // Verify performance
            assertThat(completed)
                .as("All operations should complete within timeout")
                .isTrue();
                
            assertThat(avgMs)
                .as("Average operation time")
                .isLessThan(500.0);
                
            assertThat(opsPerSec)
                .as("Operations per second")
                .isGreaterThan(10.0);
                
            assertThat(times.size())
                .as("All operations should be recorded")
                .isEqualTo(1000);

        } finally {
            if (executor != null) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService failed to terminate");
                }
            }
        }
    }
}
