package vn.com.fecredit.app.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the ParticipantEvent entity.
 * This class tests the functionality of the ParticipantEvent entity, including:
 * - Spin history management (adding/removing)
 * - Spin count tracking
 * - Daily limit enforcement
 * - Win rate calculation
 * - State validation
 * - Activation/deactivation rules
 */
class ParticipantEventTest {

    private ParticipantEvent participantEvent;
    private Event event;
    private EventLocation eventLocation;
    private Participant participant;

    /**
     * Sets up the test environment before each test.
     * Creates and configures all necessary entities for testing ParticipantEvent:
     * - Event with basic properties
     * - EventLocation linked to the event
     * - Participant with basic properties
     * - ParticipantEvent connecting participant to event with initial spin counts
     */
    @BeforeEach
    void setUp() {
        event = Event.builder()
                .name("Test Event")
                .code("TEST")
                .status(1)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();

        eventLocation = EventLocation.builder()
                .name("Test Location")
                .status(1)
                .event(event)
                .build();

        participant = Participant.builder()
                .name("Test Participant")
                .status(1)
                .build();

        participantEvent = ParticipantEvent.builder()
                .event(event)
                .eventLocation(eventLocation)
                .participant(participant)
                .totalSpins(10)
                .remainingSpins(10)
                .initialSpins(10)
                .dailySpinsUsed(0)
                .status(1)
                .build();
    }

    /**
     * Tests that when a spin history is added to a participant event:
     * - The daily spins used count is incremented
     * - The remaining spins count is decremented
     * - The spin history is added to the participant event's collection
     * - The bidirectional relationship is established
     */
    @Test
    void whenAddSpinHistory_thenUpdateCounts() {
        // Given
        SpinHistory spinHistory = SpinHistory.builder().build();

        // When
        participantEvent.addSpinHistory(spinHistory);

        // Then
        assertThat(participantEvent.getDailySpinsUsed()).isEqualTo(1);
        assertThat(participantEvent.getRemainingSpins()).isEqualTo(9);
        assertThat(participantEvent.getSpinHistories()).contains(spinHistory);
        assertThat(spinHistory.getParticipantEvent()).isEqualTo(participantEvent);
    }

    /**
     * Tests that when a spin history is removed from a participant event:
     * - The daily spins used count is decremented
     * - The remaining spins count is incremented
     * - The spin history is removed from the participant event's collection
     * - The bidirectional relationship is broken
     */
    @Test
    void whenRemoveSpinHistory_thenUpdateCounts() {
        // Given
        SpinHistory spinHistory = SpinHistory.builder().build();
        participantEvent.addSpinHistory(spinHistory);

        // When
        participantEvent.removeSpinHistory(spinHistory);

        // Then
        assertThat(participantEvent.getDailySpinsUsed()).isZero();
        assertThat(participantEvent.getRemainingSpins()).isEqualTo(10);
        assertThat(participantEvent.getSpinHistories()).doesNotContain(spinHistory);
        assertThat(spinHistory.getParticipantEvent()).isNull();
    }

    /**
     * Tests that the hasRemainingSpins method correctly determines
     * whether a participant has remaining spins available.
     */
    @Test
    void whenHasRemainingSpins_thenReturnTrue() {
        assertThat(participantEvent.hasRemainingSpins()).isTrue();
        
        participantEvent.setRemainingSpins(0);
        assertThat(participantEvent.hasRemainingSpins()).isFalse();
    }

    /**
     * Tests that the hasReachedDailyLimit method correctly determines
     * whether a participant has reached their daily spin limit.
     */
    @Test
    void whenHasReachedDailyLimit_thenReturnTrue() {
        assertThat(participantEvent.hasReachedDailyLimit()).isFalse();
        
        participantEvent.setDailySpinsUsed(10);
        assertThat(participantEvent.hasReachedDailyLimit()).isTrue();
    }

    /**
     * Tests that the resetDailySpins method correctly resets
     * the daily spins used counter to zero.
     */
    @Test
    void whenResetDailySpins_thenResetCounter() {
        // Given
        participantEvent.setDailySpinsUsed(5);

        // When
        participantEvent.resetDailySpins();

        // Then
        assertThat(participantEvent.getDailySpinsUsed()).isZero();
    }

    /**
     * Tests that the getWinRate method correctly calculates
     * the win rate based on the spin histories.
     */
    @Test
    void whenCalculateWinRate_thenReturnCorrectRate() {
        // Given
        SpinHistory win1 = SpinHistory.builder().win(true).build();
        SpinHistory win2 = SpinHistory.builder().win(true).build();
        SpinHistory loss = SpinHistory.builder().win(false).build();

        participantEvent.addSpinHistory(win1);
        participantEvent.addSpinHistory(win2);
        participantEvent.addSpinHistory(loss);

        // When
        double winRate = participantEvent.getWinRate();

        // Then
        assertThat(winRate).isEqualTo(2.0 / 3.0);
    }

    /**
     * Tests that the validateState method correctly throws exceptions
     * when the participant event is in an invalid state.
     */
    @Test
    void whenValidateState_thenThrowExceptionForInvalidState() {
        ParticipantEvent invalid = ParticipantEvent.builder().build();
        
        assertThatThrownBy(() -> invalid.validateState())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Event is required");

        invalid.setEvent(event);
        assertThatThrownBy(() -> invalid.validateState())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Event location is required");

        invalid.setEventLocation(eventLocation);
        assertThatThrownBy(() -> invalid.validateState())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Participant is required");
    }

    /**
     * Tests that the onActivate method correctly checks dependencies
     * and throws exceptions when dependencies are inactive.
     */
    @Test
    void whenActivate_thenCheckDependencies() {
        // Given
        event.setStatus(0);
        
        // When/Then
        assertThatThrownBy(() -> participantEvent.testOnActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive event");

        event.setStatus(1);
        eventLocation.setStatus(0);
        assertThatThrownBy(() -> participantEvent.testOnActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive location");

        eventLocation.setStatus(1);
        participant.setStatus(0);
        assertThatThrownBy(() -> participantEvent.testOnActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive participant");
    }

    /**
     * Tests that the onDeactivate method correctly checks for pending spins
     * and throws exceptions when there are unfinalized spins.
     */
    @Test
    void whenDeactivate_thenCheckPendingSpins() {
        // Given
        SpinHistory unfinalized = SpinHistory.builder()
            .finalized(false)
            .build();
        participantEvent.addSpinHistory(unfinalized);

        // When/Then
        assertThatThrownBy(() -> participantEvent.onDeactivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("pending spins");
    }
}
