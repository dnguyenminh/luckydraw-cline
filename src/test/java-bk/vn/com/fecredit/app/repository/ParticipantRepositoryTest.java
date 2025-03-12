package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Event testEvent;
    private User testUser;

    @BeforeEach
    void setUp() {
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .enabled(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create test event
        testEvent = Event.builder()
                .code("TEST-EVENT")
                .name("Test Event")
                .description("Test event description")
                .totalSpins(1000L)
                .remainingSpins(1000L)
                .startDate(LocalDateTime.now().minusHours(1))
                .endDate(LocalDateTime.now().plusHours(24))
                .isActive(true)
                .build();
        testEvent = eventRepository.save(testEvent);

        // Create test participant
        Participant participant = Participant.builder()
                .event(testEvent)
                .user(testUser)
                .name("Active Participant")
                .fullName("Active Participant Full")
                .email("active@test.com")
                .customerId("CUST123-207390394922000")
                .employeeId("EMP123")
                .cardNumber("CARD123")
                .spinsRemaining(5L)
                .isActive(true)
                .build();
        participantRepository.save(participant);
    }

    @Test
    void findByEventIdAndIsActiveTrue_ShouldReturnActiveParticipants() {
        // When
        var participants = participantRepository.findByEvent_IdAndIsActiveTrue(testEvent.getId());

        // Then
        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getName()).isEqualTo("Active Participant");
    }

    @Test
    void findByEventIdAndUserId_ShouldReturnParticipant() {
        // When
        var participant = participantRepository.findByEvent_IdAndUser_Id(testEvent.getId(), testUser.getId());

        // Then
        assertThat(participant).isPresent();
        assertThat(participant.get().getName()).isEqualTo("Active Participant");
    }

    @Test
    void existsByEventIdAndUserId_ShouldReturnTrue_WhenParticipantExists() {
        // When
        boolean exists = participantRepository.existsByEvent_IdAndUser_Id(testEvent.getId(), testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEventIdAndUserId_ShouldReturnFalse_WhenParticipantDoesNotExist() {
        // When
        boolean exists = participantRepository.existsByEvent_IdAndUser_Id(testEvent.getId(), 999L);

        // Then
        assertThat(exists).isFalse();
    }
}