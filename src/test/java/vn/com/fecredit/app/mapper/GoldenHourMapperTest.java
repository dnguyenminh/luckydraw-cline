package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.golden.CreateGoldenHourRequest;
import vn.com.fecredit.app.dto.golden.UpdateGoldenHourRequest;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

class GoldenHourMapperTest {

    private GoldenHourMapper mapper;
    private Event testEvent;
    private Reward testReward;
    private GoldenHour testGoldenHour;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mapper = new GoldenHourMapper();
        now = LocalDateTime.now();

        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .build();

        testReward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .build();

        testGoldenHour = GoldenHour.builder()
                .id(1L)
                .event(testEvent)
                .reward(testReward)
                .name("Test Golden Hour")
                .description("Test Description")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();
    }

    @Test
    void toDTO_Success() {
        GoldenHourDTO dto = mapper.toDTO(testGoldenHour);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEventId()).isEqualTo(1L);
        assertThat(dto.getRewardId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Golden Hour");
        assertThat(dto.getStartTime()).isEqualTo(now);
        assertThat(dto.getEndTime()).isEqualTo(now.plusHours(2));
        assertThat(dto.getMultiplier()).isEqualTo(2.0);
        assertThat(dto.getIsActive()).isTrue();
    }

    @Test
    void createEntityFromRequest_Success() {
        CreateGoldenHourRequest request = CreateGoldenHourRequest.builder()
                .eventId(1L)
                .rewardId(1L)
                .name("New Golden Hour")
                .description("New Description")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();

        GoldenHour entity = mapper.createEntityFromRequest(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("New Golden Hour");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getStartTime()).isEqualTo(now);
        assertThat(entity.getEndTime()).isEqualTo(now.plusHours(2));
        assertThat(entity.getMultiplier()).isEqualTo(2.0);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void createEntityFromNestedRequest_Success() {
        GoldenHourDTO.CreateRequest request = GoldenHourDTO.CreateRequest.builder()
                .eventId(1L)
                .rewardId(1L)
                .name("New Golden Hour")
                .description("New Description")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();

        GoldenHour entity = mapper.createEntityFromRequest(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("New Golden Hour");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getStartTime()).isEqualTo(now);
        assertThat(entity.getEndTime()).isEqualTo(now.plusHours(2));
        assertThat(entity.getMultiplier()).isEqualTo(2.0);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void updateEntityFromRequest_Success() {
        UpdateGoldenHourRequest request = UpdateGoldenHourRequest.builder()
                .name("Updated Golden Hour")
                .description("Updated Description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(3))
                .multiplier(3.0)
                .isActive(false)
                .build();

        mapper.updateEntityFromRequest(request, testGoldenHour);

        assertThat(testGoldenHour.getName()).isEqualTo("Updated Golden Hour");
        assertThat(testGoldenHour.getDescription()).isEqualTo("Updated Description");
        assertThat(testGoldenHour.getStartTime()).isEqualTo(now.plusHours(1));
        assertThat(testGoldenHour.getEndTime()).isEqualTo(now.plusHours(3));
        assertThat(testGoldenHour.getMultiplier()).isEqualTo(3.0);
        assertThat(testGoldenHour.isActive()).isFalse();
    }

    @Test
    void updateEntityFromNestedRequest_Success() {
        GoldenHourDTO.UpdateRequest request = GoldenHourDTO.UpdateRequest.builder()
                .name("Updated Golden Hour")
                .description("Updated Description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(3))
                .multiplier(3.0)
                .isActive(false)
                .build();

        mapper.updateEntityFromRequest(request, testGoldenHour);

        assertThat(testGoldenHour.getName()).isEqualTo("Updated Golden Hour");
        assertThat(testGoldenHour.getDescription()).isEqualTo("Updated Description");
        assertThat(testGoldenHour.getStartTime()).isEqualTo(now.plusHours(1));
        assertThat(testGoldenHour.getEndTime()).isEqualTo(now.plusHours(3));
        assertThat(testGoldenHour.getMultiplier()).isEqualTo(3.0);
        assertThat(testGoldenHour.isActive()).isFalse();
    }
}