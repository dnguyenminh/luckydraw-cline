package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.EventRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class EventLocationMapperTest {

    @Mock
    private EventRepository eventRepository;

    private EventLocationMapper mapper;

    @BeforeEach
    void setUp() {
        openMocks(this);
        mapper = new EventLocationMapperImpl();
        ((EventLocationMapperImpl) mapper).eventRepository = eventRepository;
    }

    @Test
    void shouldMapCreateRequestToEntity() {
        // Given
        EventLocationDTO.CreateRequest request = EventLocationDTO.CreateRequest.builder()
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .build();

        // When
        EventLocation entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDailySpinLimit()).isEqualTo(request.getDailySpinLimit());
        assertThat(entity.getWinProbabilityMultiplier()).isEqualTo(request.getWinProbabilityMultiplier());
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldMapEntityToResponse() {
        // Given
        EventLocation entity = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        // When
        EventLocationDTO.Response response = mapper.toResponse(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getLocationName()).isEqualTo(entity.getName());
        assertThat(response.getDailySpinLimit()).isEqualTo(entity.getDailySpinLimit());
        assertThat(response.getWinProbabilityMultiplier()).isEqualTo(entity.getWinProbabilityMultiplier());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void shouldMapUpdateRequestToEntity() {
        // Given
        EventLocation entity = EventLocation.builder()
                .id(1L)
                .name("Original Name")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        EventLocationDTO.UpdateRequest request = EventLocationDTO.UpdateRequest.builder()
                .name("Updated Name")
                .dailySpinLimit(200)
                .winProbabilityMultiplier(0.7)
                .active(false)
                .build();

        // When
        mapper.updateEntity(entity, request);

        // Then
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDailySpinLimit()).isEqualTo(request.getDailySpinLimit());
        assertThat(entity.getWinProbabilityMultiplier()).isEqualTo(request.getWinProbabilityMultiplier());
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldMapEntityToSummary() {
        // Given
        EventLocation entity = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .dailySpinLimit(100)
                .status(EntityStatus.ACTIVE)
                .build();

        // When
        EventLocationDTO.Summary summary = mapper.toSummary(entity);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(entity.getId());
        assertThat(summary.getLocationName()).isEqualTo(entity.getName());
        assertThat(summary.getDailySpinLimit()).isEqualTo(entity.getDailySpinLimit());
        assertThat(summary.getStatus()).isEqualTo(entity.getStatus());
    }
}
