package vn.com.fecredit.app.monitoring.base;

import java.time.Duration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test categories and configuration for different types of tests
 */
public final class TestCategory {

    private TestCategory() {} // Prevent instantiation

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface StressTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface IntegrationTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface EnduranceTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ChaosTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD}) 
    public @interface PerformanceTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface MonitoringTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ResourceTest {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ConcurrencyTest {}

    /**
     * Configuration for test categories with default values
     */
    public static final class Config {
        // Test durations
        public static final Duration DEFAULT_STRESS_DURATION = Duration.ofMinutes(30);
        public static final Duration DEFAULT_ENDURANCE_DURATION = Duration.ofHours(1);
        public static final Duration DEFAULT_CHAOS_DURATION = Duration.ofMinutes(15);
        public static final Duration DEFAULT_PERFORMANCE_DURATION = Duration.ofMinutes(5);
        
        // Thread configurations
        public static final int DEFAULT_STRESS_THREADS = 10;
        public static final int DEFAULT_CONCURRENT_THREADS = 4;
        
        // Other settings
        public static final int DEFAULT_BATCH_SIZE = 1000;
        public static final int DEFAULT_RETRY_ATTEMPTS = 3;
        public static final int DEFAULT_TIMEOUT_SECONDS = 30;
        public static final int DEFAULT_WARMUP_SECONDS = 10;

        // Resource thresholds
        public static final int MAX_MEMORY_USAGE_PERCENT = 80;
        public static final int MAX_CPU_USAGE_PERCENT = 90;
        public static final int MAX_THREAD_COUNT = 100;
        
        // Performance thresholds
        public static final long MAX_RESPONSE_TIME_MS = 1000;
        public static final long TARGET_THROUGHPUT = 1000;
        public static final double ERROR_RATE_THRESHOLD = 0.01;

        private Config() {} // Prevent instantiation
    }
}
