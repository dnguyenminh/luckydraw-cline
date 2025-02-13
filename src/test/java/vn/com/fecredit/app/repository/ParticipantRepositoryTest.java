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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EventRepository eventRepository;

    private Event event;
    private Participant activeParticipant;
    private Participant inactiveParticipant;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        event = new Event();
        // Generate a unique event code per test run
        event.setCode("TEST-EVENT-" + System.currentTimeMillis());
        event.setName("Test Event");
        event.setStartDate(now.minusDays(1));
        event.setEndDate(now.plusDays(1));
        event.setIsActive(true);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event = eventRepository.save(event);

        activeParticipant = new Participant();
        activeParticipant.setEvent(event);
        activeParticipant.setName("Active Participant");
        activeParticipant.setFullName("Active Participant Full");
        activeParticipant.setEmployeeId("EMP123");
        // Generate a unique customer ID per test run
        String uniqueCustomerId = "CUST123-" + System.nanoTime();
        activeParticipant.setCustomerId(uniqueCustomerId);
        activeParticipant.setCardNumber("CARD123");
        activeParticipant.setEmail("active@test.com");
        activeParticipant.setIsActive(true);
        activeParticipant.setSpinsRemaining(5L);
        activeParticipant.setCreatedAt(now);
        activeParticipant.setUpdatedAt(now);
        activeParticipant = participantRepository.save(activeParticipant);

        inactiveParticipant = new Participant();
        inactiveParticipant.setEvent(event);
        inactiveParticipant.setName("Inactive Participant");
        inactiveParticipant.setFullName("Inactive Participant Full");
        inactiveParticipant.setEmployeeId("EMP456");
        inactiveParticipant.setCustomerId("CUST456");
        inactiveParticipant.setCardNumber("CARD456");
        inactiveParticipant.setEmail("inactive@test.com");
        inactiveParticipant.setIsActive(false);
        inactiveParticipant.setSpinsRemaining(0L);
        inactiveParticipant.setCreatedAt(now);
        inactiveParticipant.setUpdatedAt(now);
        inactiveParticipant = participantRepository.save(inactiveParticipant);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void findByEventIdAndEmployeeId_ShouldReturnParticipant_WhenExists() {
        Optional<Participant> found = participantRepository
            .findByEventIdAndEmployeeId(event.getId(), "EMP123");

        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeId()).isEqualTo("EMP123");
    }

    @Test
    void findByCustomerId_ShouldReturnParticipant_WhenExists() {
        Optional<Participant> found = participantRepository.findByCustomerId(activeParticipant.getCustomerId());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(activeParticipant.getCustomerId());
    }

    @Test
    void existsByCustomerId_ShouldReturnTrue_WhenExists() {
        boolean exists = participantRepository.existsByCustomerId(activeParticipant.getCustomerId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCardNumber_ShouldReturnTrue_WhenExists() {
        boolean exists = participantRepository.existsByCardNumber("CARD123");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        boolean exists = participantRepository.existsByEmail("active@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void findByEventIdAndIsActive_ShouldReturnOnlyActiveParticipants() {
        List<Participant> activeParticipants = participantRepository
            .findByEventIdAndIsActive(event.getId(), true);

        assertThat(activeParticipants).hasSize(1);
        assertThat(activeParticipants.get(0).getEmployeeId()).isEqualTo("EMP123");
    }

    @Test
    void findEligibleParticipants_ShouldReturnOnlyActiveWithSpins() {
        List<Participant> eligible = participantRepository.findEligibleParticipants(event.getId());

        assertThat(eligible).hasSize(1);
        assertThat(eligible.get(0).getEmployeeId()).isEqualTo("EMP123");
    }

    @Test
    void updateSpinsRemaining_ShouldUpdateSpins() {
        int updated = participantRepository.updateSpinsRemaining(activeParticipant.getId(), 4L);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<Participant> found = participantRepository.findById(activeParticipant.getId());
        assertThat(updated).isEqualTo(1);
        assertThat(found).isPresent();
        assertThat(found.get().getSpinsRemaining()).isEqualTo(4L);
    }

    @Test
    void searchParticipants_ShouldFindByMultipleCriteria() {
        // Use a more specific search text that matches only the active participant
        List<Participant> results = participantRepository.searchParticipants(event.getId(), "Active Participant");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).contains("Active Participant");
    }

    @Test
    void updateParticipantStatus_ShouldUpdateStatus() {
        int updated = participantRepository.updateParticipantStatus(
            activeParticipant.getId(), false);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<Participant> found = participantRepository.findById(activeParticipant.getId());
        assertThat(updated).isEqualTo(1);
        assertThat(found).isPresent();
        assertThat(found.get().getIsActive()).isFalse();
    }

    @Test
    void hasEligibleParticipants_ShouldReturnTrue_WhenEligibleExist() {
        boolean hasEligible = participantRepository.hasEligibleParticipants(event.getId());

        assertThat(hasEligible).isTrue();
    }
}