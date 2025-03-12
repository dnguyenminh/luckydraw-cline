package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;

@SpringBootTest
@ActiveProfiles("test")
class EventLocationServiceConcurrencyTest {

    @Autowired
    private EventLocationService eventLocationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Test
    @Transactional
    void decrementRemainingSpins_Concurrently() throws Exception {
        // Given
        Event event = Event.builder()
                .name("Test Event")
                .build();
        event = eventRepository.save(event);

        EventLocation location = EventLocation.builder()
                .eventId(event.getId())
                .name("Test Location")
                .addressLine1("123 Test St")
                .addressLine2("Suite 456")
                .province("Test Province")
                .district("Test District")
                .postalCode("12345")
                .totalSpins(100)
                .remainingSpins(100)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.0)
                .sortOrder(1)
                .active(true)
                .event(event)
                .build();
        location = eventLocationRepository.save(location);

        final Long locationId = location.getId();
        int numberOfThreads = 10;
        int decrementsPerThread = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < decrementsPerThread; j++) {
                    eventLocationService.decrementRemainingSpins(locationId);
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        // Then
        EventLocation updatedLocation = eventLocationRepository.findById(locationId).get();
        int expectedRemainingSpins = 100 - (numberOfThreads * decrementsPerThread);
        assertThat(updatedLocation.getRemainingSpins()).isEqualTo(expectedRemainingSpins);
    }
}
