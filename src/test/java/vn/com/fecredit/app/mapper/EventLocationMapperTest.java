package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;

class EventLocationMapperTest {

    private EventLocationMapper mapper;
    private ParticipantMapper participantMapper;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        participantMapper = mock(ParticipantMapper.class);
        mapper = new EventLocationMapper(participantMapper);
        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .build();
    }

    @Test
    void createEntityFromRequest_ShouldMapAllFields() {
        // Given
        EventLocationDTO.CreateRequest request = EventLocationDTO.CreateRequest.builder()
                .eventId(1L)
                .name("Test Location")
                .addressLine1("123 Test St")
                .addressLine2("Suite 456")
                .province("Test Province")
                .district("Test District")
                .postalCode("12345")
                .totalSpins(100)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.5)
                .sortOrder(1)
                .active(true)
                .build();

        // When
        EventLocation entity = mapper.createEntityFromRequest(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getEventId()).isEqualTo(request.getEventId());
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getAddressLine1()).isEqualTo(request.getAddressLine1());
        assertThat(entity.getAddressLine2()).isEqualTo(request.getAddressLine2());
        assertThat(entity.getProvince()).isEqualTo(request.getProvince());
        assertThat(entity.getDistrict()).isEqualTo(request.getDistrict());
        assertThat(entity.getPostalCode()).isEqualTo(request.getPostalCode());
        assertThat(entity.getTotalSpins()).isEqualTo(request.getTotalSpins());
        assertThat(entity.getDailySpinLimit()).isEqualTo(request.getDailySpinLimit());
        assertThat(entity.getWinProbabilityMultiplier()).isEqualTo(request.getWinProbabilityMultiplier());
        assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
        assertThat(entity.isActive()).isEqualTo(request.getActive());
    }

    @Test
    void updateEntityFromRequest_ShouldUpdateAllFields() {
        // Given
        EventLocation entity = EventLocation.builder()
                .id(1L)
                .eventId(1L)
                .name("Original Name")
                .addressLine1("Original Address 1")
                .province("Original Province")
                .active(true)
                .build();

        EventLocationDTO.UpdateRequest request = EventLocationDTO.UpdateRequest.builder()
                .name("Updated Name")
                .addressLine1("Updated Address 1")
                .addressLine2("Updated Address 2")
                .province("Updated Province")
                .district("Updated District")
                .postalCode("54321")
                .totalSpins(200)
                .dailySpinLimit(20)
                .winProbabilityMultiplier(2.0)
                .sortOrder(2)
                .active(false)
                .build();

        // When
        mapper.updateEntityFromRequest(entity, request);

        // Then
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getAddressLine1()).isEqualTo(request.getAddressLine1());
        assertThat(entity.getAddressLine2()).isEqualTo(request.getAddressLine2());
        assertThat(entity.getProvince()).isEqualTo(request.getProvince());
        assertThat(entity.getDistrict()).isEqualTo(request.getDistrict());
        assertThat(entity.getPostalCode()).isEqualTo(request.getPostalCode());
        assertThat(entity.getTotalSpins()).isEqualTo(request.getTotalSpins());
        assertThat(entity.getDailySpinLimit()).isEqualTo(request.getDailySpinLimit());
        assertThat(entity.getWinProbabilityMultiplier()).isEqualTo(request.getWinProbabilityMultiplier());
        assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
        assertThat(entity.isActive()).isEqualTo(request.getActive());
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        EventLocation location = EventLocation.builder()
                .id(1L)
                .eventId(1L)
                .name("Test Location")
                .addressLine1("123 Test St")
                .addressLine2("Suite 456")
                .province("Test Province")
                .district("Test District")
                .postalCode("12345")
                .totalSpins(100)
                .remainingSpins(50)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.5)
                .sortOrder(1)
                .active(true)
                .event(testEvent)
                .build();

        // When
        EventLocationDTO.EventLocationResponse response = mapper.toResponse(location);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(location.getId());
        assertThat(response.getEventId()).isEqualTo(location.getEventId());
        assertThat(response.getEventName()).isEqualTo(testEvent.getName());
        assertThat(response.getName()).isEqualTo(location.getName());
        assertThat(response.getAddressLine1()).isEqualTo(location.getAddressLine1());
        assertThat(response.getAddressLine2()).isEqualTo(location.getAddressLine2());
        assertThat(response.getProvince()).isEqualTo(location.getProvince());
        assertThat(response.getDistrict()).isEqualTo(location.getDistrict());
        assertThat(response.getPostalCode()).isEqualTo(location.getPostalCode());
        assertThat(response.getFullAddress()).isEqualTo(location.getFullAddress());
        assertThat(response.getTotalSpins()).isEqualTo(location.getTotalSpins());
        assertThat(response.getRemainingSpins()).isEqualTo(location.getRemainingSpins());
        assertThat(response.getDailySpinLimit()).isEqualTo(location.getDailySpinLimit());
        assertThat(response.getWinProbabilityMultiplier()).isEqualTo(location.getWinProbabilityMultiplier());
        assertThat(response.getSortOrder()).isEqualTo(location.getSortOrder());
        assertThat(response.isActive()).isEqualTo(location.isActive());
    }

    @Test
    void toSummary_ShouldMapAllFields() {
        // Given
        EventLocation location = EventLocation.builder()
                .id(1L)
                .eventId(1L)
                .name("Test Location")
                .province("Test Province")
                .remainingSpins(50)
                .active(true)
                .build();

        // When
        EventLocationDTO.LocationSummary summary = mapper.toSummary(location);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(location.getId());
        assertThat(summary.getEventId()).isEqualTo(location.getEventId());
        assertThat(summary.getName()).isEqualTo(location.getName());
        assertThat(summary.getProvince()).isEqualTo(location.getProvince());
        assertThat(summary.getRemainingSpins()).isEqualTo(location.getRemainingSpins());
        assertThat(summary.isActive()).isEqualTo(location.isActive());
        assertThat(summary.getStatistics()).isNotNull();
    }
}
