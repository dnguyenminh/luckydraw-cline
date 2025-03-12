package vn.com.fecredit.app.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.config.TestJpaConfig;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.util.EventTestBuilder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
class EntityStatusConverterIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void whenSavingEvent_thenStatusIsPersistedCorrectly() {
        // Given
        Event event = EventTestBuilder.createTestEvent(EntityStatus.ACTIVE);

        // When
        Event savedEvent = eventRepository.save(event);
        testEntityManager.flush();
        testEntityManager.clear();

        // Then
        Event foundEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(foundEvent.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(foundEvent.isActiveEntity()).isTrue();

        // Verify raw database value
        String dbStatus = (String) em.createNativeQuery(
            "SELECT status FROM events WHERE id = ?1")
            .setParameter(1, savedEvent.getId())
            .getSingleResult();
        assertThat(dbStatus).isEqualTo(EntityStatus.ACTIVE.getCode());
    }

    @Test
    void whenSavingEventWithNullStatus_thenDefaultStatusIsUsed() {
        // Given
        Event event = EventTestBuilder.createTestEvent(null);

        // When
        Event savedEvent = eventRepository.save(event);
        testEntityManager.flush();
        testEntityManager.clear();

        // Then
        Event foundEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(foundEvent.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(foundEvent.isActiveEntity()).isTrue();

        // Verify raw database value
        String dbStatus = (String) em.createNativeQuery(
            "SELECT status FROM events WHERE id = ?1")
            .setParameter(1, savedEvent.getId())
            .getSingleResult();
        assertThat(dbStatus).isEqualTo(EntityStatus.ACTIVE.getCode());
    }

    @Test
    void whenUpdatingStatus_thenNewStatusIsPersistedCorrectly() {
        // Given
        Event event = EventTestBuilder.createTestEvent(EntityStatus.ACTIVE);
        Event savedEvent = eventRepository.save(event);
        testEntityManager.flush();

        // When
        savedEvent.setStatus(EntityStatus.INACTIVE);
        eventRepository.save(savedEvent);
        testEntityManager.flush();
        testEntityManager.clear();

        // Then
        Event foundEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(foundEvent.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(foundEvent.isActiveEntity()).isFalse();
    }

    @Test
    void whenSavingMultipleEvents_thenAllStatusesArePersistedCorrectly() {
        // Given
        List<EntityStatus> statuses = Arrays.asList(
            EntityStatus.ACTIVE,
            EntityStatus.INACTIVE,
            EntityStatus.PENDING,
            EntityStatus.DELETED
        );

        // When
        statuses.forEach(status -> 
            eventRepository.save(EventTestBuilder.createTestEvent(status))
        );
        testEntityManager.flush();
        testEntityManager.clear();

        // Then
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(statuses.size());

        for (EntityStatus status : statuses) {
            assertThat(events)
                .filteredOn(e -> e.getStatus() == status)
                .hasSize(1);
        }
    }

    @ParameterizedTest
    @MethodSource("validStatusTransitions")
    void whenTransitioningStatus_thenChangeIsPersistedCorrectly(
            EntityStatus fromStatus, EntityStatus toStatus) {
        // Given
        Event event = EventTestBuilder.createTestEvent(fromStatus);
        Event savedEvent = eventRepository.save(event);
        testEntityManager.flush();

        // When
        savedEvent.setStatus(toStatus);
        eventRepository.save(savedEvent);
        testEntityManager.flush();
        testEntityManager.clear();

        // Then
        Event foundEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(foundEvent.getStatus()).isEqualTo(toStatus);

        // Verify raw value
        String dbStatus = (String) em.createNativeQuery(
            "SELECT status FROM events WHERE id = ?1")
            .setParameter(1, savedEvent.getId())
            .getSingleResult();
        assertThat(dbStatus).isEqualTo(toStatus.getCode());
    }

    private static Stream<Arguments> validStatusTransitions() {
        return Stream.of(
            Arguments.of(EntityStatus.DRAFT, EntityStatus.PENDING),
            Arguments.of(EntityStatus.PENDING, EntityStatus.APPROVED),
            Arguments.of(EntityStatus.APPROVED, EntityStatus.ACTIVE),
            Arguments.of(EntityStatus.ACTIVE, EntityStatus.INACTIVE),
            Arguments.of(EntityStatus.INACTIVE, EntityStatus.ACTIVE),
            Arguments.of(EntityStatus.INACTIVE, EntityStatus.DELETED)
        );
    }
}
