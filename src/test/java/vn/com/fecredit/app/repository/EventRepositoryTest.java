package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.model.Reward;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private EntityManager entityManager;

    private Event event;

    @BeforeEach
    void setUp() {
        // Create and save an event
        event = Event.builder()
            .code("TEST-EVENT")
            .name("Test Event")
            .description("Test Description")
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .totalSpins(1000L)
            .remainingSpins(1000L)
            .isActive(true)
            .version(0L)
            .build();
        event = eventRepository.save(event);
    }

    @Test
    void testOptimisticLocking() {
        // Given
        Event firstEvent = eventRepository.findById(event.getId()).get();
        Event secondEvent = eventRepository.findById(event.getId()).get();

        // When
        firstEvent.setName("Updated by first");
        eventRepository.save(firstEvent);

        // Then
        secondEvent.setName("Updated by second");
        assertThrows(OptimisticLockingFailureException.class, () -> {
            eventRepository.save(secondEvent);
        });
    }

    @Test
    void findByIdWithDetails_ShouldReturnEventWithDetails() {
        // Given
        EventLocation location = EventLocation.builder()
            .event(event)
            .name("Test Location")
            .province("Test Province")
            .totalSpins(100)
            .remainingSpins(100)
            .dailySpinLimit(3)
            .spinsRemaining(1000L)
            .isActive(true)
            .build();
        eventLocationRepository.save(location);
        event.addLocation(location);

        Reward reward = Reward.builder()
            .event(event)
            .name("Test Reward")
            .description("Test Reward Description")
            .quantity(100)
            .remainingQuantity(100)
            .probability(0.5)
            .isActive(true)
            .version(0L)
            .build();
        rewardRepository.save(reward);
        event.addReward(reward);

        eventRepository.save(event);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Event> foundEvent = eventRepository.findByIdWithDetails(event.getId());

        // Then
        assertThat(foundEvent).isPresent();
        Event result = foundEvent.get();
        assertThat(result.getEventLocations()).hasSize(1);
        assertThat(result.getEventLocations().iterator().next().getName()).isEqualTo("Test Location");
        assertThat(result.getRewards()).hasSize(1);
        assertThat(result.getRewards().iterator().next().getName()).isEqualTo("Test Reward");
        assertEquals(0L, result.getVersion());
    }

    @Test
    void findActiveEvents_ShouldReturnOnlyActiveEvents() {
        // Given
        Event inactiveEvent = Event.builder()
            .code("INACTIVE-EVENT")
            .name("Inactive Event")
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .totalSpins(1000L)
            .remainingSpins(1000L)
            .isActive(false)
            .version(0L)
            .build();
        eventRepository.save(inactiveEvent);

        // When
        List<Event> activeEvents = eventRepository.findByIsActiveTrue();

        // Then
        assertThat(activeEvents).hasSize(1);
        assertThat(activeEvents.get(0).getCode()).isEqualTo("TEST-EVENT");
    }

    @Test
    void existsByCode_ShouldReturnTrueForExistingCode() {
        // When
        boolean exists = eventRepository.existsByCode("TEST-EVENT");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_ShouldReturnFalseForNonExistingCode() {
        // When
        boolean exists = eventRepository.existsByCode("NON-EXISTING");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findActiveEventsWithRemainingSpins_ShouldReturnOnlyActiveEventsWithSpins() {
        // Given
        Event noSpinsEvent = Event.builder()
            .code("NO-SPINS-EVENT")
            .name("No Spins Event")
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .totalSpins(0L)
            .remainingSpins(0L)
            .isActive(true)
            .version(0L)
            .build();
        eventRepository.save(noSpinsEvent);

        // When
        List<Event> activeEvents = eventRepository.findActiveEventsWithRemainingSpins();

        // Then
        assertThat(activeEvents).hasSize(1);
        assertThat(activeEvents).extracting("code").containsExactly("TEST-EVENT");
        assertThat(activeEvents).extracting("remainingSpins").allMatch(spins -> (Long)spins > 0);
    }

    @Test
    void updateVersion_ShouldIncrementVersionNumber() {
        // Given
        Long initialVersion = event.getVersion();

        // When
        event.setName("Updated Name");
        event = eventRepository.save(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        Event updatedEvent = eventRepository.findById(event.getId()).get();
        assertEquals(initialVersion + 1, updatedEvent.getVersion());
    }
}