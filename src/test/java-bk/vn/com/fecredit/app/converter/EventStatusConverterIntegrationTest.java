package vn.com.fecredit.app.converter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.enums.EventStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventStatusConverterIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldPersistEventStatusAsString() {
        // Given
        Event event = createEvent("Test Event", EventStatus.SCHEDULED);

        // When
        entityManager.persist(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        Event loadedEvent = entityManager.find(Event.class, event.getId());
        assertThat(loadedEvent.getEventStatus())
            .isNotNull()
            .isEqualTo(EventStatus.SCHEDULED);
    }

    @Test
    void shouldLoadEventStatusFromString() {
        // Given
        Event event = createEvent("Test Event", EventStatus.ACTIVE);
        entityManager.persist(event);
        entityManager.flush();
        entityManager.clear();

        // When
        Event loadedEvent = entityManager.find(Event.class, event.getId());

        // Then
        assertThat(loadedEvent.getEventStatus())
            .isNotNull()
            .isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void shouldHandleNullEventStatus() {
        // Given
        Event event = createEvent("Test Event", null);

        // When
        entityManager.persist(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        Event loadedEvent = entityManager.find(Event.class, event.getId());
        assertThat(loadedEvent.getEventStatus())
            .isNotNull()
            .isEqualTo(EventStatus.DRAFT); // Default value
    }

    @Test
    void shouldUpdateEventStatus() {
        // Given
        Event event = createEvent("Test Event", EventStatus.DRAFT);
        entityManager.persist(event);
        entityManager.flush();

        // When
        event.setEventStatus(EventStatus.ACTIVE);
        entityManager.merge(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        Event loadedEvent = entityManager.find(Event.class, event.getId());
        assertThat(loadedEvent.getEventStatus())
            .isNotNull()
            .isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void shouldTransitionThroughAllStatuses() {
        // Given
        Event event = createEvent("Test Event", EventStatus.DRAFT);
        entityManager.persist(event);

        // When/Then - Test all status transitions
        for (EventStatus status : EventStatus.values()) {
            event.setEventStatus(status);
            entityManager.flush();
            entityManager.clear();

            Event reloadedEvent = entityManager.find(Event.class, event.getId());
            assertThat(reloadedEvent.getEventStatus())
                .as("Status transition to %s failed", status)
                .isEqualTo(status);
            
            event = reloadedEvent;
        }
    }

    private Event createEvent(String name, EventStatus status) {
        Event event = new Event();
        event.setName(name);
        event.setEventStatus(status);
        event.setStartTime(LocalDateTime.now());
        event.setEndTime(LocalDateTime.now().plusDays(1));
        return event;
    }
}
