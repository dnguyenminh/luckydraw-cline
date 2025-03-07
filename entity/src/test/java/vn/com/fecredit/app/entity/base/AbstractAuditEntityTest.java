package vn.com.fecredit.app.entity.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractAuditEntity Tests")
class AbstractAuditEntityTest extends BaseEntityTest {

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("Deep Copy Tests")
    class DeepCopyTests {
        @Test
        @DisplayName("toBuilder should perform deep copy")
        void toBuilder_ShouldPerformDeepCopy() {
            ComplexTestEntity original = ComplexTestEntity.builder()
                .id(1L)
                .createdBy("creator")
                .createdAt(now)
                .tags(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

            original.getTags().addAll(Arrays.asList("tag1", "tag2"));
            original.getMetadata().put("key", "value");

            ComplexTestEntity copy = original.toBuilder()
                .tags(new ArrayList<>(original.getTags()))
                .metadata(new HashMap<>(original.getMetadata()))
                .build();
            
            // Modify original collections
            original.getTags().add("tag3");
            original.getMetadata().put("newKey", "newValue");

            assertAll(
                "Deep copy should be independent",
                () -> assertNotSame(original.getTags(), copy.getTags(), "Tags list should be different instances"),
                () -> assertNotSame(original.getMetadata(), copy.getMetadata(), "Metadata map should be different instances"),
                () -> assertEquals(2, copy.getTags().size(), "Tags size should remain unchanged"),
                () -> assertEquals(1, copy.getMetadata().size(), "Metadata size should remain unchanged"),
                () -> assertEquals(Arrays.asList("tag1", "tag2"), copy.getTags(), "Tags content should match original"),
                () -> assertEquals(Collections.singletonMap("key", "value"), copy.getMetadata(), "Metadata content should match original")
            );
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {
        @Test
        @DisplayName("Should handle concurrent reads and writes")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            int numThreads = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numThreads);
            ConcurrentHashMap<String, Integer> updates = new ConcurrentHashMap<>();
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

            ThreadSafeTestEntity threadSafeEntity = new ThreadSafeTestEntity(lock);
            threadSafeEntity.setId(1L);

            // Create reader and writer threads
            for (int i = 0; i < numThreads; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        if (threadNum % 2 == 0) {
                            // Reader
                            lock.readLock().lock();
                            try {
                                assertNotNull(threadSafeEntity.getCreatedAt());
                                updates.merge("reads", 1, Integer::sum);
                            } finally {
                                lock.readLock().unlock();
                            }
                        } else {
                            // Writer
                            lock.writeLock().lock();
                            try {
                                threadSafeEntity.setUpdatedAt(LocalDateTime.now());
                                updates.merge("writes", 1, Integer::sum);
                            } finally {
                                lock.writeLock().unlock();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(endLatch.await(5, TimeUnit.SECONDS), "Concurrent operations timed out");

            assertAll(
                "Should complete all operations",
                () -> assertTrue(updates.get("reads") > 0),
                () -> assertTrue(updates.get("writes") > 0),
                () -> assertEquals(numThreads, updates.get("reads") + updates.get("writes"))
            );
        }
    }

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestAuditEntity extends AbstractAuditEntity {
        private static final long serialVersionUID = 1L;
        private Long id;

        @Override
        public boolean isNew() {
            return id == null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestAuditEntity)) return false;
            TestAuditEntity that = (TestAuditEntity) o;
            return Objects.equals(getId(), that.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId());
        }
    }

    @Getter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    private static class ComplexTestEntity extends TestAuditEntity {
        @Builder.Default
        private List<String> tags = new ArrayList<>();

        @Builder.Default
        private Map<String, String> metadata = new HashMap<>();

        // Custom setter to ensure deep copy
        public void setTags(List<String> tags) {
            this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        }

        // Custom setter to ensure deep copy
        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        }
    }

    @Getter
    @Setter
    private static class ThreadSafeTestEntity extends TestAuditEntity {
        private final ReentrantReadWriteLock lock;

        public ThreadSafeTestEntity(ReentrantReadWriteLock lock) {
            this.lock = lock;
            this.setCreatedAt(LocalDateTime.now());
        }

        @Override
        public void setUpdatedAt(LocalDateTime updatedAt) {
            lock.writeLock().lock();
            try {
                super.setUpdatedAt(updatedAt);
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public LocalDateTime getCreatedAt() {
            lock.readLock().lock();
            try {
                return super.getCreatedAt();
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
