package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;

@ExtendWith(MockitoExtension.class)
class EventLocationMapperTest {

    @InjectMocks
    private EventLocationMapper mapper;

    @Test
    void toDTO_ShouldMapAllFields() {
        // Given
        Event event = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        EventLocation location = EventLocation.builder()
            .id(1L)
            .event(event)
            .name("Test Location")
            .location("Test Address")
            .totalSpins(100L)
            .remainingSpins(50L)
            .isActive(true)
            .version(1L)
            .build();

        // When
        EventLocationDTO dto = mapper.toDTO(location);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(location.getId());
        assertThat(dto.getEventId()).isEqualTo(event.getId());
        assertThat(dto.getEventName()).isEqualTo(event.getName());
        assertThat(dto.getName()).isEqualTo(location.getName());
        assertThat(dto.getLocation()).isEqualTo(location.getLocation());
        assertThat(dto.getTotalSpins()).isEqualTo(location.getTotalSpins());
        assertThat(dto.getRemainingSpins()).isEqualTo(location.getRemainingSpins());
        assertThat(dto.isActive()).isEqualTo(location.isActive());
        assertThat(dto.getVersion()).isEqualTo(location.getVersion());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        Event event = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        EventLocationDTO dto = EventLocationDTO.builder()
            .id(1L)
            .eventId(1L)
            .name("Test Location")
            .location("Test Address")
            .totalSpins(100L)
            .remainingSpins(50L)
            .active(true)
            .version(1L)
            .build();

        // When
        EventLocation entity = mapper.toEntity(dto, event);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getEvent()).isEqualTo(event);
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getLocation()).isEqualTo(dto.getLocation());
        assertThat(entity.getTotalSpins()).isEqualTo(dto.getTotalSpins());
        assertThat(entity.getRemainingSpins()).isEqualTo(dto.getRemainingSpins());
        assertThat(entity.isActive()).isEqualTo(dto.isActive());
        assertThat(entity.getVersion()).isEqualTo(dto.getVersion());
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateAllFields() {
        // Given
        EventLocationDTO dto = EventLocationDTO.builder()
            .name("Updated Location")
            .location("Updated Address")
            .totalSpins(200L)
            .remainingSpins(150L)
            .active(false)
            .build();

        EventLocation entity = EventLocation.builder()
            .name("Original Location")
            .location("Original Address")
            .totalSpins(100L)
            .remainingSpins(50L)
            .isActive(true)
            .build();

        // When
        mapper.updateEntityFromDTO(dto, entity);

        // Then
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getLocation()).isEqualTo(dto.getLocation());
        assertThat(entity.getTotalSpins()).isEqualTo(dto.getTotalSpins());
        assertThat(entity.getRemainingSpins()).isEqualTo(dto.getRemainingSpins());
        assertThat(entity.isActive()).isEqualTo(dto.isActive());
    }
}