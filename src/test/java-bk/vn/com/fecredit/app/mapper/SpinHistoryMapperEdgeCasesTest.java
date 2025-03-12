package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SpinHistoryMapperEdgeCasesTest {

    private SpinHistoryMapper mapper;
    private static final double DELTA = 0.0001;
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 3, 4, 19, 48, 55);

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(SpinHistoryMapper.class);
    }

    @Test
    void shouldHandleBasicNullAndEmptyInputs() {
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isEmpty();
        assertThat(mapper.toResponseSet(null)).isEmpty();
        assertThat(mapper.toSummary(null)).isNull();
        assertThat(mapper.toSummarySet(null)).isEmpty();
        assertDefaultStatistics(mapper.calculateStatistics(null));
        assertDefaultStatistics(mapper.calculateStatistics(Collections.emptyList()));
    }

    @Test
    void shouldHandleInvalidJsonMetadata() {
        SpinHistory entity = createBasicEntity(1L, FIXED_TIME);
        entity.setMetadata("{invalid:json}");
        entity.setNotes("{}");
        
        SpinHistoryDTO.Response response = mapper.toResponse(entity);
        assertThat(response.getMetadata()).isEqualTo("{invalid:json}");
        assertThat(response.getNotes()).isEqualTo("{}");
    }

    @Test
    void shouldHandleSpecialCharactersInStrings() {
        SpinHistory entity = createBasicEntity(1L, FIXED_TIME);
        entity.setDeviceId("\u0000\u0001\u0002"); // Control characters
        entity.setSessionId("\t\n\r"); // Whitespace
        entity.setNotes("Emoji: 😀 Unicode: \u1234"); // Unicode
        
        SpinHistoryDTO.Response response = mapper.toResponse(entity);
        
        assertThat(response.getDeviceId()).isEqualTo("\u0000\u0001\u0002");
        assertThat(response.getSessionId()).isEqualTo("\t\n\r");
        assertThat(response.getNotes()).contains("Emoji", "😀", "\u1234");
    }

    @Test
    void shouldHandleGeographicBoundaries() {
        SpinHistory entity = createBasicEntity(1L, FIXED_TIME);
        entity.setLatitude(90.0); // North pole
        entity.setLongitude(-180.0); // Date line
        
        SpinHistoryDTO.Response response = mapper.toResponse(entity);
        
        assertThat(response.getLatitude()).isEqualTo(90.0);
        assertThat(response.getLongitude()).isEqualTo(-180.0);
    }

    @ParameterizedTest
    @MethodSource("provideSpecialNumericValues")
    void shouldHandleSpecialNumericValues(Double probability, Double multiplier, Double expected) {
        SpinHistory entity = createSpinWithValues(true, probability, multiplier);
        SpinHistoryDTO.Response response = mapper.toResponse(entity);
        
        assertThat(response).isNotNull();
        if (Double.isNaN(expected)) {
            assertThat(response.getWinProbability()).isNaN();
        } else {
            assertThat(response.getWinProbability()).isEqualTo(expected);
        }
    }

    private static Stream<Arguments> provideSpecialNumericValues() {
        return Stream.of(
            Arguments.of(Double.NaN, 1.0, Double.NaN),
            Arguments.of(Double.POSITIVE_INFINITY, 1.0, Double.POSITIVE_INFINITY),
            Arguments.of(Double.NEGATIVE_INFINITY, 1.0, Double.NEGATIVE_INFINITY),
            Arguments.of(Double.MIN_VALUE, 1.0, Double.MIN_VALUE),
            Arguments.of(Double.MAX_VALUE, 1.0, Double.MAX_VALUE)
        );
    }

    @Test
    void shouldHandleNestedNullObjects() {
        SpinHistory entity = createBasicEntity(1L, FIXED_TIME);
        entity.setEvent(null);
        entity.setLocation(null);
        entity.setReward(null);
        entity.setParticipant(null);
        
        SpinHistoryDTO.Response response = mapper.toResponse(entity);
        
        assertThat(response).isNotNull()
            .satisfies(r -> {
                assertThat(r.getId()).isEqualTo(1L);
                assertThat(r.getEventId()).isNull();
                assertThat(r.getEventName()).isNull();
                assertThat(r.getLocationId()).isNull();
                assertThat(r.getLocationName()).isNull();
                assertThat(r.getRewardId()).isNull();
                assertThat(r.getRewardName()).isNull();
                assertThat(r.getParticipantId()).isNull();
                assertThat(r.getParticipantName()).isNull();
            });
    }

    @Test
    void shouldHandleUpdateWithNullAndEmptyFields() {
        SpinHistory entity = createComplexEntity();
        String originalNotes = entity.getNotes();
        Double originalProbability = entity.getWinProbability();
        
        SpinHistoryDTO.UpdateRequest updateRequest = SpinHistoryDTO.UpdateRequest.builder()
            .winProbability(null)
            .finalProbability(null)
            .probabilityMultiplier(null)
            .metadata("")
            .notes("")
            .status(null)
            .build();

        mapper.updateEntity(entity, updateRequest);

        assertThat(entity)
            .satisfies(e -> {
                assertThat(e.getWinProbability()).isEqualTo(originalProbability);
                assertThat(e.getMetadata()).isEmpty();
                assertThat(e.getNotes()).isEmpty();
                assertThat(e.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            });
    }

    @Test
    void shouldHandleCollectionWithProblematicEntities() {
        List<SpinHistory> entities = Arrays.asList(
            createBasicEntity(1L, FIXED_TIME),
            createBasicEntity(null, null),
            null,
            createBasicEntity(1L, FIXED_TIME.minusYears(100)),
            createBasicEntity(Long.MAX_VALUE, FIXED_TIME.plusYears(100))
        );
        
        List<SpinHistoryDTO.Response> responses = mapper.toResponseList(entities);
        
        assertThat(responses)
            .hasSize(4)
            .extracting(SpinHistoryDTO.Response::getId)
            .containsExactly(1L, null, 1L, Long.MAX_VALUE);
    }

    private SpinHistory createSpinWithValues(boolean win, Double probability, Double multiplier) {
        SpinHistory spin = new SpinHistory();
        spin.setWin(win);
        spin.setWinProbability(probability);
        spin.setProbabilityMultiplier(multiplier);
        return spin;
    }

    private SpinHistory createBasicEntity(Long id, LocalDateTime time) {
        SpinHistory entity = new SpinHistory();
        entity.setId(id);
        entity.setSpinTime(time);
        return entity;
    }

    private SpinHistory createComplexEntity() {
        SpinHistory entity = new SpinHistory();
        entity.setId(1L);
        entity.setSpinTime(FIXED_TIME);
        entity.setWin(true);
        entity.setWinProbability(0.75);
        entity.setProbabilityMultiplier(1.5);
        entity.setFinalProbability(0.8);
        entity.setGoldenHourActive(true);
        entity.setGoldenHourMultiplier(2.0);
        entity.setDeviceId("device123");
        entity.setSessionId("session456");
        entity.setLatitude(10.0);
        entity.setLongitude(20.0);
        entity.setMetadata("{\"key\":\"value\"}");
        entity.setNotes("Test notes");
        entity.setSpinResult("WIN");
        entity.setStatus(EntityStatus.ACTIVE);

        Event event = new Event();
        event.setId(100L);
        event.setName("Test Event");
        entity.setEvent(event);
        
        EventLocation location = new EventLocation();
        location.setId(200L);
        location.setName("Test Location");
        entity.setLocation(location);
        
        Reward reward = new Reward();
        reward.setId(300L);
        reward.setName("Test Reward");
        entity.setReward(reward);
        
        Participant participant = new Participant();
        participant.setId(400L);
        participant.setCustomerId("CUST-123");
        entity.setParticipant(participant);

        return entity;
    }

    private void assertDefaultStatistics(SpinHistoryDTO.Statistics stats) {
        assertThat(stats).isNotNull()
            .satisfies(s -> {
                assertThat(s.getTotalSpins()).isZero();
                assertThat(s.getWinningSpins()).isZero();
                assertThat(s.getWinRate()).isZero();
                assertThat(s.getAverageProbability()).isZero();
                assertThat(s.getEffectiveMultiplier()).isEqualTo(1.0);
            });
    }
}
