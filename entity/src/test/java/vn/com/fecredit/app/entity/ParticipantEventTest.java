package vn.com.fecredit.app.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParticipantEventTest {

    private ParticipantEvent participantEvent;
    private Event event;
    private EventLocation eventLocation;
    private Participant participant;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .name("Test Event")
                .code("TEST")
                .status(1)
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

    @Test
    void whenHasRemainingSpins_thenReturnTrue() {
        assertThat(participantEvent.hasRemainingSpins()).isTrue();
        
        participantEvent.setRemainingSpins(0);
        assertThat(participantEvent.hasRemainingSpins()).isFalse();
    }

    @Test
    void whenHasReachedDailyLimit_thenReturnTrue() {
        assertThat(participantEvent.hasReachedDailyLimit()).isFalse();
        
        participantEvent.setDailySpinsUsed(10);
        assertThat(participantEvent.hasReachedDailyLimit()).isTrue();
    }

    @Test
    void whenResetDailySpins_thenResetCounter() {
        // Given
        participantEvent.setDailySpinsUsed(5);

        // When
        participantEvent.resetDailySpins();

        // Then
        assertThat(participantEvent.getDailySpinsUsed()).isZero();
    }

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

    @Test
    void whenActivate_thenCheckDependencies() {
        // Given
        event.setStatus(0);
        
        // When/Then
        assertThatThrownBy(() -> participantEvent.onActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive event");

        event.setStatus(1);
        eventLocation.setStatus(0);
        assertThatThrownBy(() -> participantEvent.onActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive location");

        eventLocation.setStatus(1);
        participant.setStatus(0);
        assertThatThrownBy(() -> participantEvent.onActivate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inactive participant");
    }

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
