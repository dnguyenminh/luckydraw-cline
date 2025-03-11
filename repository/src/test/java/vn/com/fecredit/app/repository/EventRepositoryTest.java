package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    // Fixed test time for consistent test execution
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2024, 7, 1, 0, 0);

    @BeforeEach
    void setUp() {
        // Clean up any preset data that might affect our tests
        eventRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Event createEvent(String code, String name, boolean active) {
        return Event.builder()
                .code(code)
                .name(name)
                .description("Test event description")
                .startTime(TEST_TIME.minusMonths(1))
                .endTime(TEST_TIME.plusMonths(1))
                .createdAt(TEST_TIME.minusDays(1))
                .createdBy("test")
                .updatedAt(TEST_TIME.minusDays(1))
                .updatedBy("test")
                .initialSpins(10)
                .dailySpinLimit(5)
                .defaultWinProbability(0.1)
                .status(active ? AbstractStatusAwareEntity.STATUS_ACTIVE : AbstractStatusAwareEntity.STATUS_INACTIVE)
                .build();
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenExists() {
        // Given
        Event event = createEvent("EVENT_TEST001", "Test Event", true);
        entityManager.persist(event);
        entityManager.flush();

        // When
        Optional<Event> found = eventRepository.findByCode("EVENT_TEST001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Event", found.get().getName());
    }

    @Test
    void findActive_ShouldReturnOnlyActiveEvents() {
        // Given
        Event activeEvent1 = createEvent("EVENT_ACTIVE001", "Active Event 1", true);
        Event activeEvent2 = createEvent("EVENT_ACTIVE002", "Active Event 2", true);
        Event inactiveEvent = createEvent("EVENT_INACTIVE001", "Inactive Event", false);

        entityManager.persist(activeEvent1);
        entityManager.persist(activeEvent2);
        entityManager.persist(inactiveEvent);
        entityManager.flush();

        // When
        List<Event> activeEvents = eventRepository.findActive(
                AbstractStatusAwareEntity.STATUS_ACTIVE,
                TEST_TIME
        );

        // Then
        assertThat(activeEvents).hasSize(2);
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE001")));
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE002")));
        assertFalse(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_INACTIVE001")));
    }

    @Test
    void findCurrent_ShouldReturnEventsInDateRange() {
        // Given
        Event current = createEvent("EVENT_CURRENT001", "Current Event", true);
        Event future = createEvent("EVENT_FUTURE001", "Future Event", true);
        future.setStartTime(TEST_TIME.plusMonths(2));
        future.setEndTime(TEST_TIME.plusMonths(3));

        entityManager.persist(current);
        entityManager.persist(future);
        entityManager.flush();

        // When
        List<Event> currentEvents = eventRepository.findCurrent(TEST_TIME);

        // Then
        assertThat(currentEvents).hasSize(1);
        assertTrue(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_CURRENT001")));
        assertFalse(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_FUTURE001")));
    }

    @Test
    void findByIdWithDetails_ShouldFetchRelatedEntities() {
        // Given
        Event event = createEvent("EVENT_DETAILS001", "Test Event", true);
        entityManager.persist(event);
        entityManager.flush();

        // When
        Optional<Event> found = eventRepository.findByIdWithDetails(event.getId());

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEventLocations());
    }
}
