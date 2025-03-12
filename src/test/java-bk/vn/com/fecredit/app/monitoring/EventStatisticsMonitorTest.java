package vn.com.fecredit.app.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class EventStatisticsMonitorTest {

    private EventStatisticsMonitor monitor;
    private NotificationListener notificationListener;
    private List<Notification> receivedNotifications;

    @BeforeEach
    void setUp() {
        monitor = EventStatisticsMonitor.getInstance();
        monitor.clearMetrics();
        monitor.enableMonitoring();
        receivedNotifications = new ArrayList<>();
        
        notificationListener = (notification, handback) -> 
            receivedNotifications.add(notification);
        
        monitor.addNotificationListener(notificationListener, null, null);
    }

    @Test
    void shouldRecordOperationMetrics() {
        // Given
        String operation = "testOperation";
        long duration = 100L;

        // When
        monitor.recordOperation(operation, duration);

        // Then
        assertThat(monitor.getTotalOperations(operation)).isEqualTo(1);
        assertThat(monitor.getAverageProcessingTime(operation)).isEqualTo(duration);
        assertThat(monitor.getMaxProcessingTime(operation)).isEqualTo(duration);
    }

    @Test
    void shouldNotRecordMetricsWhenDisabled() {
        // Given
        monitor.disableMonitoring();
        String operation = "disabledOperation";

        // When
        monitor.recordOperation(operation, 100L);

        // Then
        assertThat(monitor.getTotalOperations(operation)).isZero();
    }

    @Test
    void shouldEmitNotificationWhenThresholdExceeded() {
        // Given
        String operation = "slowOperation";
        double threshold = 50.0;
        monitor.setPerformanceThreshold(operation, threshold);

        // When
        monitor.recordOperation(operation, 100L);

        // Then
        assertThat(receivedNotifications).hasSize(1);
        Notification notification = receivedNotifications.get(0);
        assertThat(notification.getType()).isEqualTo("eventstatistics.threshold.exceeded");
        assertThat(notification.getMessage()).contains(operation);
    }

    @Test
    void shouldCalculateAverageCorrectly() {
        // Given
        String operation = "avgOperation";
        long[] durations = {100L, 200L, 300L};
        double expectedAvg = 200.0;

        // When
        for (long duration : durations) {
            monitor.recordOperation(operation, duration);
        }

        // Then
        assertThat(monitor.getAverageProcessingTime(operation)).isEqualTo(expectedAvg);
    }

    @Test
    void shouldHandleConcurrentOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 1000;
        String operation = "concurrentOperation";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        monitor.recordOperation(operation, 1L);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        completionLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        long expectedTotal = (long) threadCount * operationsPerThread;
        assertThat(monitor.getTotalOperations(operation)).isEqualTo(expectedTotal);
    }

    @Test
    void shouldMaintainAccurateMaxTime() {
        // Given
        String operation = "maxTimeOperation";
        long maxDuration = 300L;

        // When
        monitor.recordOperation(operation, 100L);
        monitor.recordOperation(operation, maxDuration);
        monitor.recordOperation(operation, 200L);

        // Then
        assertThat(monitor.getMaxProcessingTime(operation)).isEqualTo(maxDuration);
    }

    @Test
    void shouldReturnAllMonitoredOperations() {
        // Given
        monitor.recordOperation("op1", 100L);
        monitor.recordOperation("op2", 200L);
        monitor.recordOperation("op3", 300L);

        // When
        String[] operations = monitor.getMonitoredOperations();

        // Then
        assertThat(operations).containsExactlyInAnyOrder("op1", "op2", "op3");
    }

    @Test
    void shouldClearMetricsCorrectly() {
        // Given
        monitor.recordOperation("clearOp", 100L);
        assertThat(monitor.getTotalOperations("clearOp")).isPositive();

        // When
        monitor.clearMetrics();

        // Then
        assertThat(monitor.getTotalOperations("clearOp")).isZero();
        assertThat(monitor.getMonitoredOperations()).isEmpty();
    }

    @Test
    void shouldHandleThresholdUpdates() {
        // Given
        String operation = "thresholdOperation";
        double initialThreshold = 100.0;
        double updatedThreshold = 200.0;

        // When
        monitor.setPerformanceThreshold(operation, initialThreshold);
        monitor.recordOperation(operation, 150L); // Should trigger notification
        int initialNotifications = receivedNotifications.size();

        monitor.setPerformanceThreshold(operation, updatedThreshold);
        monitor.recordOperation(operation, 150L); // Should not trigger notification

        // Then
        assertThat(receivedNotifications).hasSize(initialNotifications);
        assertThat(monitor.getPerformanceThreshold(operation)).isEqualTo(updatedThreshold);
    }

    @Test
    void shouldHandleMultipleNotificationListeners() throws Exception {
        // Given
        String operation = "multiListenerOperation";
        monitor.setPerformanceThreshold(operation, 50.0);
        
        NotificationListener secondListener = mock(NotificationListener.class);
        monitor.addNotificationListener(secondListener, null, null);

        // When
        monitor.recordOperation(operation, 100L);

        // Then
        assertThat(receivedNotifications).hasSize(1);
        verify(secondListener, times(1)).handleNotification(any(Notification.class), any());
    }
}
