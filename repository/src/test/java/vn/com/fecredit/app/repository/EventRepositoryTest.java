package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

@TestPropertySource(locations = "classpath:application-test.properties")
class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2024, 7, 1, 0, 0);

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        entityManager.flush();
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
                .version(0L)
                .build();
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenExists() {
        Event event = createEvent("EVENT_TEST001", "Test Event", true);
        event = persistAndFlush(event);

        Optional<Event> found = eventRepository.findByCode("EVENT_TEST001");

        assertTrue(found.isPresent());
        assertEquals("Test Event", found.get().getName());
    }

    @Test
    void findActive_ShouldReturnOnlyActiveEvents() {
        Event activeEvent1 = persistAndFlush(createEvent("EVENT_ACTIVE001", "Active Event 1", true));
        Event activeEvent2 = persistAndFlush(createEvent("EVENT_ACTIVE002", "Active Event 2", true));
        Event inactiveEvent = persistAndFlush(createEvent("EVENT_INACTIVE001", "Inactive Event", false));

        Set<Event> activeEvents = eventRepository.findActive(
                AbstractStatusAwareEntity.STATUS_ACTIVE,
                TEST_TIME
        );

        assertThat(activeEvents).hasSize(2);
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE001")));
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE002")));
        assertFalse(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_INACTIVE001")));
    }

    @Test
    void findCurrent_ShouldReturnEventsInDateRange() {
        Event current = persistAndFlush(createEvent("EVENT_CURRENT001", "Current Event", true));
        
        Event future = createEvent("EVENT_FUTURE001", "Future Event", true);
        future.setStartTime(TEST_TIME.plusMonths(2));
        future.setEndTime(TEST_TIME.plusMonths(3));
        future = persistAndFlush(future);

        Set<Event> currentEvents = eventRepository.findCurrent(TEST_TIME);

        assertThat(currentEvents).hasSize(1);
        assertTrue(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_CURRENT001")));
        assertFalse(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_FUTURE001")));
    }

    @Test
    void findByIdWithDetails_ShouldFetchRelatedEntities() {
        // Create event and region
        Event event = createEvent("EVENT_DETAILS001", "Test Event", true);
        Region region = persistAndFlush(testDataFactory.createRegion("REGION001", "Test Region"));
        
        // Create provinces and add them to region
        Province province1 = persistAndFlush(testDataFactory.createProvince("PROV001", "Test Province 1"));
        Province province2 = persistAndFlush(testDataFactory.createProvince("PROV002", "Test Province 2"));
        region.addProvince(province1);
        region.addProvince(province2);
        region = persistAndFlush(region);

        // Create location and add provinces to event
        EventLocation location = testDataFactory.createEventLocation(event, region, "LOC001", "Test Location");
        event.getProvinces().add(province1);
        event.getProvinces().add(province2);
        event.addLocation(location);
        event = persistAndFlush(event);
        
        clear();

        Optional<Event> found = eventRepository.findByIdWithDetails(event.getId());

        assertTrue(found.isPresent(), "Event should be found");
        assertNotNull(found.get().getEventLocations(), "Event locations should not be null");
        assertFalse(found.get().getEventLocations().isEmpty(), "Event locations should not be empty");
        assertEquals(1, found.get().getEventLocations().size(), "Should have exactly one location");
        assertNotNull(found.get().getProvinces(), "Event provinces should not be null");
        assertEquals(2, found.get().getProvinces().size(), "Should have exactly two provinces");
        
        EventLocation foundLocation = found.get().getEventLocations().iterator().next();
        assertEquals("LOC001", foundLocation.getCode(), "Location code should match");
        assertNotNull(foundLocation.getEvent(), "Location should have event reference");
        assertNotNull(foundLocation.getRegion(), "Location should have region reference");
        assertEquals(2, foundLocation.getRegion().getProvinces().size(), "Region should have two provinces");
    }

    @Test
    void existsByCode_ShouldReturnTrueForExistingCode() {
        Event event = createEvent("EXISTS_TEST", "Exists Test", true);
        persistAndFlush(event);

        boolean exists = eventRepository.existsByCode("EXISTS_TEST");

        assertTrue(exists);
    }

    @Test
    void existsByCode_ShouldReturnFalseForNonExistingCode() {
        boolean exists = eventRepository.existsByCode("NON_EXISTENT_CODE");
        assertFalse(exists);
    }

    @Test
    void findActiveEventsWithRemainingSpins_ShouldFilterCorrectly() {
        Event activeWithSpins = createEvent("ACTIVE_SPINS", "Active With Spins", true);
        activeWithSpins.setRemainingSpins(10L);
        persistAndFlush(activeWithSpins);

        Event activeNoSpins = createEvent("ACTIVE_NO_SPINS", "Active No Spins", true);
        activeNoSpins.setRemainingSpins(0L);
        persistAndFlush(activeNoSpins);

        List<Event> results = eventRepository.findActiveEventsWithRemainingSpins();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCode()).isEqualTo("ACTIVE_SPINS");
    }

    @Test
    void shouldDetectOptimisticLocking() {
        Event event = persistAndFlush(createEvent("LOCK_TEST", "Lock Test", true));
        Event firstInstance = eventRepository.findById(event.getId()).get();
        Event secondInstance = eventRepository.findById(event.getId()).get();

        firstInstance.setName("First Update");
        eventRepository.save(firstInstance);

        secondInstance.setName("Second Update");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            eventRepository.save(secondInstance);
        });
    }
}
