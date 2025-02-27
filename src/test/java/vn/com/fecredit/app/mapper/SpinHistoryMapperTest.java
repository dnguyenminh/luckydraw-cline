package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

class SpinHistoryMapperTest {

    private SpinHistoryMapper mapper;
    private Event testEvent;
    private EventLocation testLocation;
    private Participant testParticipant;
    private Reward testReward;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        mapper = new SpinHistoryMapper();
        testTime = LocalDateTime.now();
        
        testEvent = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        testLocation = EventLocation.builder()
            .id(2L)
            .name("Test Location")
            .province("Test Province")
            .build();

        testParticipant = Participant.builder()
            .id(3L)
            .fullName("Test Participant")
            .build();

        testReward = Reward.builder()
            .id(4L)
            .name("Test Reward")
            .build();
    }

    @Test
    void shouldMapEntityToResponse() {
        // Given
        SpinHistory entity = SpinHistory.builder()
            .id(1L)
            .event(testEvent)
            .eventLocation(testLocation)
            .participant(testParticipant)
            .reward(testReward)
            .spinTime(testTime)
            .winProbability(0.5)
            .finalProbability(0.6)
            .probabilityMultiplier(1.2)
            .win(true)
            .goldenHourActive(true)
            .goldenHourMultiplier(2.0)
            .notes("Test notes")
            .build();

        // When
        SpinHistoryDTO.SpinResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEventId()).isEqualTo(testEvent.getId());
        assertThat(response.getEventName()).isEqualTo(testEvent.getName());
        assertThat(response.getEventLocationId()).isEqualTo(testLocation.getId());
        assertThat(response.getEventLocationName()).isEqualTo(testLocation.getName());
        assertThat(response.getEventLocationProvince()).isEqualTo(testLocation.getProvince());
        assertThat(response.getParticipantId()).isEqualTo(testParticipant.getId());
        assertThat(response.getParticipantName()).isEqualTo(testParticipant.getFullName());
        assertThat(response.getRewardId()).isEqualTo(testReward.getId());
        assertThat(response.getRewardName()).isEqualTo(testReward.getName());
        assertThat(response.getSpinTime()).isEqualTo(testTime);
        assertThat(response.getWinProbability()).isEqualTo(0.5);
        assertThat(response.getFinalProbability()).isEqualTo(0.6);
        assertThat(response.getProbabilityMultiplier()).isEqualTo(1.2);
        assertThat(response.isWin()).isTrue();
        assertThat(response.isGoldenHourActive()).isTrue();
        assertThat(response.getGoldenHourMultiplier()).isEqualTo(2.0);
        assertThat(response.getNotes()).isEqualTo("Test notes");
    }

    @Test
    void shouldHandleNullEntityInResponse() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void shouldHandleNullReferencesInResponse() {
        // Given
        SpinHistory entity = SpinHistory.builder()
            .id(1L)
            .spinTime(testTime)
            .build();

        // When
        SpinHistoryDTO.SpinResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEventId()).isNull();
        assertThat(response.getEventName()).isNull();
        assertThat(response.getEventLocationId()).isNull();
        assertThat(response.getEventLocationName()).isNull();
        assertThat(response.getParticipantId()).isNull();
        assertThat(response.getParticipantName()).isNull();
        assertThat(response.getRewardId()).isNull();
        assertThat(response.getRewardName()).isNull();
    }

    @Test
    void shouldMapCreateRequestToEntity() {
        // Given
        SpinHistoryDTO.CreateRequest request = SpinHistoryDTO.CreateRequest.builder()
            .spinTime(testTime)
            .winProbability(0.5)
            .probabilityMultiplier(1.2)
            .goldenHourActive(true)
            .goldenHourMultiplier(2.0)
            .notes("Test notes")
            .build();

        // When
        SpinHistory entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getSpinTime()).isEqualTo(testTime);
        assertThat(entity.getWinProbability()).isEqualTo(0.5);
        assertThat(entity.getProbabilityMultiplier()).isEqualTo(1.2);
        assertThat(entity.isGoldenHourActive()).isTrue();
        assertThat(entity.getGoldenHourMultiplier()).isEqualTo(2.0);
        assertThat(entity.getNotes()).isEqualTo("Test notes");
    }

    @Test
    void shouldHandleNullCreateRequest() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void shouldUpdateEntity() {
        // Given
        SpinHistory entity = new SpinHistory();
        SpinHistoryDTO.CreateRequest request = SpinHistoryDTO.CreateRequest.builder()
            .spinTime(testTime)
            .winProbability(0.5)
            .probabilityMultiplier(1.2)
            .goldenHourActive(true)
            .goldenHourMultiplier(2.0)
            .notes("Updated notes")
            .build();

        // When
        mapper.updateEntity(request, entity);

        // Then
        assertThat(entity.getSpinTime()).isEqualTo(testTime);
        assertThat(entity.getWinProbability()).isEqualTo(0.5);
        assertThat(entity.getProbabilityMultiplier()).isEqualTo(1.2);
        assertThat(entity.isGoldenHourActive()).isTrue();
        assertThat(entity.getGoldenHourMultiplier()).isEqualTo(2.0);
        assertThat(entity.getNotes()).isEqualTo("Updated notes");
    }

    @Test
    void shouldCalculateStats() {
        // Given
        List<SpinHistory> spins = List.of(
            SpinHistory.builder()
                .event(testEvent)
                .participant(testParticipant)
                .spinTime(testTime)
                .win(true)
                .winProbability(0.5)
                .finalProbability(0.6)
                .goldenHourActive(true)
                .goldenHourMultiplier(2.0)
                .build(),
            SpinHistory.builder()
                .event(testEvent)
                .participant(testParticipant)
                .spinTime(testTime.plusHours(1))
                .win(false)
                .winProbability(0.4)
                .finalProbability(0.5)
                .goldenHourActive(false)
                .build()
        );

        // When
        SpinHistoryDTO.SpinStats stats = mapper.toStats(spins);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getEventId()).isEqualTo(testEvent.getId());
        assertThat(stats.getParticipantId()).isEqualTo(testParticipant.getId());
        assertThat(stats.getTotalSpins()).isEqualTo(2);
        assertThat(stats.getTotalWins()).isEqualTo(1);
        assertThat(stats.getWinRate()).isEqualTo(0.5);
        assertThat(stats.getAverageProbability()).isEqualTo(0.45);
        assertThat(stats.getAverageFinalProbability()).isEqualTo(0.55);
        assertThat(stats.getGoldenHourSpins()).isEqualTo(1);
        assertThat(stats.getAverageGoldenHourMultiplier()).isEqualTo(2.0);
        assertThat(stats.getFirstSpinTime()).isEqualTo(testTime);
        assertThat(stats.getLastSpinTime()).isEqualTo(testTime.plusHours(1));
        assertThat(stats.getLastWinTime()).isEqualTo(testTime);
    }

    @Test
    void shouldHandleNullStatsInput() {
        assertNull(mapper.toStats(null));
    }

    @Test
    void shouldHandleEmptyStatsInput() {
        assertNull(mapper.toStats(List.of()));
    }

    @Test
    void shouldReturnCorrectClasses() {
        assertEquals(SpinHistory.class, mapper.getEntityClass());
        assertEquals(SpinHistoryDTO.SpinResponse.class, mapper.getResponseClass());
        assertEquals(SpinHistoryDTO.CreateRequest.class, mapper.getCreateRequestClass());
        assertEquals(SpinHistoryDTO.CreateRequest.class, mapper.getUpdateRequestClass());
    }
}
