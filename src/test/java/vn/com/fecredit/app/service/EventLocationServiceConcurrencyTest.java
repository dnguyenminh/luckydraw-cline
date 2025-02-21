package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;

@SpringBootTest
@ActiveProfiles("test")
class EventLocationServiceConcurrencyTest {

    @Autowired
    private EventLocationService locationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository locationRepository;

    @Test
    @Transactional
    void concurrentAllocateSpins_ShouldNotOverallocate() throws InterruptedException {
        // Given
        Event event = Event.builder()
            .code("TEST-001")
            .name("Test Event")
            .build();
        event = eventRepository.save(event);

        EventLocation location = locationService.createEventLocation(
            event,
            "Test Location",
            100L
        );

        int numThreads = 10;
        int numAttemptsPerThread = 15;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Boolean> results = new ArrayList<>();

        // When
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numAttemptsPerThread; j++) {
                        boolean allocated = locationService.allocateSpin(location.getId());
                        if (allocated) {
                            results.add(true);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        EventLocation finalLocation = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(finalLocation.getRemainingSpins()).isEqualTo(0L);
        assertThat(results).hasSize(100); // Initial spins
    }
}