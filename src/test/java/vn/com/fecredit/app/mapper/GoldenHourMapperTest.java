package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

class GoldenHourMapperTest {

    private GoldenHourMapper mapper;
    private Event event;
    private Reward reward;

    @BeforeEach
    void setUp() {
        mapper = new GoldenHourMapper();

        event = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        reward = Reward.builder()
            .id(1L)
            .name("Test Reward")
            .build();
    }

    @Test
    void toDTO_ShouldMapAllFields() {
        // Given
        GoldenHour entity = GoldenHour.builder()
            .id(1L)
            .event(event)
            .reward(reward)
            .name("Test Golden Hour")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();

        // When
        GoldenHourDTO dto = mapper.toDTO(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEventId()).isEqualTo(1L);
        assertThat(dto.getRewardId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Golden Hour");
        assertThat(dto.getStartHour()).isEqualTo(9);
        assertThat(dto.getEndHour()).isEqualTo(17);
        assertThat(dto.getMultiplier()).isEqualTo(2.0);
        assertThat(dto.getIsActive()).isTrue();
    }

    @Test
    void toEntity_FromCreateRequest_ShouldMapAllFields() {
        // Given
        GoldenHourDTO.CreateRequest request = GoldenHourDTO.CreateRequest.builder()
            .name("Test Golden Hour")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();

        // When
        GoldenHour entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Test Golden Hour");
        assertThat(entity.getStartHour()).isEqualTo(9);
        assertThat(entity.getEndHour()).isEqualTo(17);
        assertThat(entity.getMultiplier()).isEqualTo(2.0);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void toEntity_FromCreateRequestWithDateTime_ShouldMapHours() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().withHour(9);
        LocalDateTime endTime = LocalDateTime.now().withHour(17);
        
        GoldenHourDTO.CreateRequest request = GoldenHourDTO.CreateRequest.builder()
            .name("Test Golden Hour")
            .startTime(startTime)
            .endTime(endTime)
            .multiplier(2.0)
            .isActive(true)
            .build();

        // When
        GoldenHour entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Test Golden Hour");
        assertThat(entity.getStartHour()).isEqualTo(9);
        assertThat(entity.getEndHour()).isEqualTo(17);
        assertThat(entity.getMultiplier()).isEqualTo(2.0);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void updateEntity_ShouldUpdateOnlyProvidedFields() {
        // Given
        GoldenHour entity = GoldenHour.builder()
            .name("Original Name")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();

        GoldenHourDTO.UpdateRequest request = GoldenHourDTO.UpdateRequest.builder()
            .name("Updated Name")
            .multiplier(3.0)
            .build();

        // When
        mapper.updateEntity(entity, request);

        // Then
        assertThat(entity.getName()).isEqualTo("Updated Name");
        assertThat(entity.getStartHour()).isEqualTo(9);
        assertThat(entity.getEndHour()).isEqualTo(17);
        assertThat(entity.getMultiplier()).isEqualTo(3.0);
        assertThat(entity.isActive()).isTrue();
    }
}