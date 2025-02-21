package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;

@Component
public class EventLocationMapper {
    
    public EventLocationDTO toDTO(EventLocation location) {
        if (location == null) {
            return null;
        }

        return EventLocationDTO.builder()
            .id(location.getId())
            .eventId(location.getEvent() != null ? location.getEvent().getId() : null)
            .eventName(location.getEvent() != null ? location.getEvent().getName() : null)
            .name(location.getName())
            .location(location.getLocation())
            .totalSpins(location.getTotalSpins())
            .remainingSpins(location.getRemainingSpins())
            .active(location.isActive())
            .version(location.getVersion())
            .createdAt(location.getCreatedAt())
            .updatedAt(location.getUpdatedAt())
            .build();
    }

    public EventLocation toEntity(EventLocationDTO dto, Event event) {
        if (dto == null) {
            return null;
        }

        return EventLocation.builder()
            .id(dto.getId())
            .event(event)
            .name(dto.getName())
            .location(dto.getLocation())
            .totalSpins(dto.getTotalSpins())
            .remainingSpins(dto.getRemainingSpins())
            .isActive(dto.isActive())
            .version(dto.getVersion())
            .build();
    }

    public void updateEntityFromDTO(EventLocationDTO dto, EventLocation entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setLocation(dto.getLocation());
        entity.setTotalSpins(dto.getTotalSpins());
        entity.setRemainingSpins(dto.getRemainingSpins());
        entity.setActive(dto.isActive());
    }
}