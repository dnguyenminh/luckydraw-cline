package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.enums.RecurringDay;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class GoldenHourMapperTest {

    private GoldenHourMapper mapper;
    private Event testEvent;
    private Reward testReward;
    private GoldenHour testGoldenHour;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(GoldenHourMapper.class);

        testEvent = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        testReward = Reward.builder()
            .id(2L)
            .name("Test Reward")
            .build();

        testGoldenHour = GoldenHour.builder()
            .id(3L)
            .name("Test Golden Hour")
            .description("Test Description")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusHours(2))
            .multiplier(2.0)
            .probabilityMultiplier(1.5)
            .activeDays("1234567")
            .event(testEvent)
            .reward(testReward)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // When
        GoldenHourDTO.Response response = mapper.toResponse(testGoldenHour);

        // Then
        assertThat(response.getId()).isEqualTo(testGoldenHour.getId());
        assertThat(response.getName()).isEqualTo(testGoldenHour.getName());
        assertThat(response.getDescription()).isEqualTo(testGoldenHour.getDescription());
        assertThat(response.getStartTime()).isEqualTo(testGoldenHour.getStartTimeAsLocalTime());
        assertThat(response.getEndTime()).isEqualTo(testGoldenHour.getEndTimeAsLocalTime());
        assertThat(response.getMultiplier()).isEqualTo(testGoldenHour.getMultiplier());
        assertThat(response.getProbabilityMultiplier()).isEqualTo(testGoldenHour.getProbabilityMultiplier());
        assertThat(response.getEventId()).isEqualTo(testEvent.getId());
        assertThat(response.getRewardId()).isEqualTo(testReward.getId());
        assertThat(response.getEventName()).isEqualTo(testEvent.getName());
        assertThat(response.getRewardName()).isEqualTo(testReward.getName());
    }

    @Test
    void toEntity_shouldMapAllFields() {
        // Given
        Set<RecurringDay> activeDays = new HashSet<>();
        activeDays.add(RecurringDay.MONDAY);
        activeDays.add(RecurringDay.WEDNESDAY);

        GoldenHourDTO.CreateRequest request = GoldenHourDTO.CreateRequest.builder()
            .name("New Golden Hour")
            .description("New Description")
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(12, 0))
            .multiplier(2.0)
            .probabilityMultiplier(1.5)
            .activeDays(activeDays)
            .eventId(1L)
            .rewardId(2L)
            .build();

        // When
        GoldenHour entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDescription()).isEqualTo(request.getDescription());
        assertThat(entity.getStartTimeAsLocalTime()).isEqualTo(request.getStartTime());
        assertThat(entity.getEndTimeAsLocalTime()).isEqualTo(request.getEndTime());
        assertThat(entity.getMultiplier()).isEqualTo(request.getMultiplier());
        assertThat(entity.getProbabilityMultiplier()).isEqualTo(request.getProbabilityMultiplier());
        assertThat(entity.getActiveDaysAsSet()).containsExactlyInAnyOrderElementsOf(request.getActiveDays());
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Given
        Set<RecurringDay> activeDays = new HashSet<>();
        activeDays.add(RecurringDay.TUESDAY);
        activeDays.add(RecurringDay.THURSDAY);

        GoldenHourDTO.UpdateRequest request = GoldenHourDTO.UpdateRequest.builder()
            .name("Updated Golden Hour")
            .description("Updated Description")
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(16, 0))
            .multiplier(3.0)
            .probabilityMultiplier(2.0)
            .activeDays(activeDays)
            .rewardId(2L)
            .active(true)
            .build();

        GoldenHour entity = GoldenHour.builder()
            .id(testGoldenHour.getId())
            .name(testGoldenHour.getName())
            .description(testGoldenHour.getDescription())
            .startTime(testGoldenHour.getStartTime())
            .endTime(testGoldenHour.getEndTime())
            .multiplier(testGoldenHour.getMultiplier())
            .probabilityMultiplier(testGoldenHour.getProbabilityMultiplier())
            .activeDays(testGoldenHour.getActiveDays())
            .event(testGoldenHour.getEvent())
            .reward(testGoldenHour.getReward())
            .status(EntityStatus.ACTIVE)
            .build();

        // When
        mapper.updateEntity(entity, request);

        // Then
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDescription()).isEqualTo(request.getDescription());
        assertThat(entity.getStartTimeAsLocalTime()).isEqualTo(request.getStartTime());
        assertThat(entity.getEndTimeAsLocalTime()).isEqualTo(request.getEndTime());
        assertThat(entity.getMultiplier()).isEqualTo(request.getMultiplier());
        assertThat(entity.getProbabilityMultiplier()).isEqualTo(request.getProbabilityMultiplier());
        assertThat(entity.getActiveDaysAsSet()).containsExactlyInAnyOrderElementsOf(request.getActiveDays());
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE.name());
    }
}
