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
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class ParticipantEventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ParticipantEventRepository participantEventRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private EventLocationRepository eventLocationRepository;
    
    @Autowired
    private ParticipantRepository participantRepository;

    private Event event;
    private EventLocation location;
    private Participant participant;
    private Region region;

    @BeforeEach
    void setUp() {
        // Create and save test Region
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG001") // Different from NORTH, CENTRAL, SOUTH
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(region);

        // Create and save test Event
        event = Event.builder()
            .name("Test Event")
            .code("EVENT_TEST001") // Different from TET2024, SUMMER2024
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().plusDays(30))
            .initialSpins(10)
            .dailySpinLimit(5)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(event);

        // Create and save test EventLocation
        location = EventLocation.builder()
            .event(event)
            .region(region)
            .name("Test Location")
            .code("LOC_TEST001") // Different from HN-TET etc
            .initialSpins(10)
            .dailySpinLimit(5)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(location);

        // Create and save test Participant
        participant = Participant.builder()
            .name("Test Participant")
            .code("TEST_USER001") // Different from USER001 etc
            .account("TEST_USER001") // Using same as code
            .phone("0123456789")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(participant);
    }

    @Test
    void findByParticipantAndLocation_ShouldReturnParticipantEvent_WhenExists() {
        // Given
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .participant(participant)
            .eventLocation(location)
            .totalSpins(10)
            .availableSpins(10)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(participantEvent);

        // When
        Optional<ParticipantEvent> found = participantEventRepository
            .findByParticipantAndLocation(participant, location);

        // Then
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getAvailableSpins());
    }

    @Test
    void findActiveByLocation_ShouldReturnOnlyActive() {
        // Given
        ParticipantEvent active = ParticipantEvent.builder()
            .participant(participant)
            .eventLocation(location)
            .totalSpins(10)
            .availableSpins(10)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        Participant inactiveParticipant = Participant.builder()
            .name("Inactive Participant")
            .code("TEST_USER002") // Different from USER001 etc
            .account("TEST_USER002")
            .phone("0987654321")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(inactiveParticipant);

        ParticipantEvent inactive = ParticipantEvent.builder()
            .participant(inactiveParticipant)
            .eventLocation(location)
            .totalSpins(10)
            .availableSpins(10)
            .status(AbstractStatusAwareEntity.STATUS_INACTIVE)
            .build();

        persistAndFlush(active);
        persistAndFlush(inactive);

        // When
        List<ParticipantEvent> activeEvents = participantEventRepository
            .findStatusByLocation(location);

        // Then
        assertThat(activeEvents).hasSize(1);
        assertEquals(active.getId(), activeEvents.get(0).getId());
    }

    @Test
    void countActiveByLocation_ShouldReturnCorrectCount() {
        // Given
        ParticipantEvent active1 = ParticipantEvent.builder()
            .participant(participant)
            .eventLocation(location)
            .totalSpins(10)
            .availableSpins(10)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        Participant participant2 = Participant.builder()
            .name("Test Participant 2")
            .code("TEST_USER003") // Different from USER001 etc
            .account("TEST_USER003")
            .phone("0123456788")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(participant2);

        ParticipantEvent active2 = ParticipantEvent.builder()
            .participant(participant2)
            .eventLocation(location)
            .totalSpins(10)
            .availableSpins(10)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        persistAndFlush(active1);
        persistAndFlush(active2);

        // When
        long count = participantEventRepository.countStatusByLocation(location);

        // Then
        assertEquals(2, count);
    }
}
