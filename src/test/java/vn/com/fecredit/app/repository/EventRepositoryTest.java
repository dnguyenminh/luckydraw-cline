package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private Event activeEvent;
    private Event inactiveEvent;
    private Event futureEvent;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        participantRepository.deleteAll();
        rewardRepository.deleteAll();
        
        activeEvent = new Event();
        activeEvent.setCode("ACTIVE-EVENT");
        activeEvent.setName("Active Event");
        activeEvent.setStartDate(now.minusDays(1));
        activeEvent.setEndDate(now.plusDays(1));
        activeEvent.setIsActive(true);
        activeEvent.setCreatedAt(now);
        activeEvent.setUpdatedAt(now);
        activeEvent = eventRepository.save(activeEvent);

        inactiveEvent = new Event();
        inactiveEvent.setCode("INACTIVE-EVENT");
        inactiveEvent.setName("Inactive Event");
        inactiveEvent.setStartDate(now.minusDays(1));
        inactiveEvent.setEndDate(now.plusDays(1));
        inactiveEvent.setIsActive(false);
        inactiveEvent.setCreatedAt(now);
        inactiveEvent.setUpdatedAt(now);
        inactiveEvent = eventRepository.save(inactiveEvent);

        futureEvent = new Event();
        futureEvent.setCode("FUTURE-EVENT");
        futureEvent.setName("Future Event");
        futureEvent.setStartDate(now.plusDays(2));
        futureEvent.setEndDate(now.plusDays(3));
        futureEvent.setIsActive(true);
        futureEvent.setCreatedAt(now);
        futureEvent.setUpdatedAt(now);
        futureEvent = eventRepository.save(futureEvent);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenEventExists() {
        Optional<Event> foundEvent = eventRepository.findByCode("ACTIVE-EVENT");

        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getCode()).isEqualTo("ACTIVE-EVENT");
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenEventDoesNotExist() {
        Optional<Event> foundEvent = eventRepository.findByCode("NON-EXISTENT");

        assertThat(foundEvent).isEmpty();
    }

    @Test
    void findActiveEvents_ShouldReturnOnlyCurrentActiveEvents() {
        List<Event> currentActiveEvents = eventRepository.findActiveEvents(now);

        assertThat(currentActiveEvents).hasSize(1);
        assertThat(currentActiveEvents.get(0).getCode()).isEqualTo("ACTIVE-EVENT");
    }

    @Test
    void findByCodeAndIsActive_ShouldReturnEvent_WhenEventIsActive() {
        Optional<Event> foundEvent = eventRepository.findByCodeAndIsActive("ACTIVE-EVENT", true);

        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getCode()).isEqualTo("ACTIVE-EVENT");
    }

    @Test
    void findByCodeAndIsActive_ShouldReturnEmpty_WhenEventIsInactive() {
        Optional<Event> foundEvent = eventRepository.findByCodeAndIsActive("INACTIVE-EVENT", true);

        assertThat(foundEvent).isEmpty();
    }

    @Test
    void findByIdWithDetails_ShouldReturnEventWithDetails() {
        Reward reward = new Reward();
        reward.setEvent(activeEvent);
        reward.setName("Test Reward");
        reward.setQuantity(10);
        reward.setRemainingQuantity(10);
        reward.setIsActive(true);
        reward.setCreatedAt(now);
        reward.setUpdatedAt(now);
        rewardRepository.save(reward);

        Participant participant = new Participant();
        participant.setEvent(activeEvent);
        participant.setName("Test Participant");
        participant.setEmployeeId("EMP123");
        participant.setFullName("Test Participant Full");
        participant.setIsActive(true);
        participant.setCreatedAt(now);
        participant.setUpdatedAt(now);
        participantRepository.save(participant);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<Event> eventWithDetails = eventRepository.findByIdWithDetails(activeEvent.getId());

        assertThat(eventWithDetails).isPresent();
        Event event = eventWithDetails.get();
        assertThat(event.getRewards()).hasSize(1);
        assertThat(event.getParticipants()).hasSize(1);
        assertThat(event.getRewards().iterator().next().getName()).isEqualTo("Test Reward");
        assertThat(event.getParticipants().iterator().next().getEmployeeId()).isEqualTo("EMP123");
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenEventDoesNotExist() {
        boolean exists = eventRepository.existsByCode("NON-EXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    void updateEventStatus_ShouldUpdateIsActive() {
        int updated = eventRepository.updateEventStatus(activeEvent.getId(), false);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Event updatedEvent = eventRepository.findById(activeEvent.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(updatedEvent.getIsActive()).isFalse();
    }

    @Test
    void findCurrentEvents_ShouldReturnEventsInDateRange() {
        List<Event> currentEvents = eventRepository.findCurrentEvents(
                now.minusDays(2),
                now.plusDays(2));

        assertThat(currentEvents).hasSize(2)
                .extracting("code")
                .containsExactlyInAnyOrder("ACTIVE-EVENT", "INACTIVE-EVENT");
    }

    @Test
    void updateRemainingSpins_ShouldUpdateSpins() {
        activeEvent.setRemainingSpins(10L);
        activeEvent = eventRepository.save(activeEvent);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        int updated = eventRepository.updateRemainingSpins(activeEvent.getId(), 9L);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Event updatedEvent = eventRepository.findById(activeEvent.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(updatedEvent.getRemainingSpins()).isEqualTo(9L);
    }
}