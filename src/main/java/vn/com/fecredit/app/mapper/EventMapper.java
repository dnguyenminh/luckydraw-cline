package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.model.Event;

@Component
public class EventMapper {
    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        return EventDTO.builder()
                .id(event.getId())
                .code(event.getCode())
                .name(event.getName())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isActive(event.getIsActive())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .participantCount(event.getParticipants().size())
                .rewardCount(event.getRewards().size())
                .build();
    }

    public Event toEntity(EventDTO.CreateEventRequest request) {
        if (request == null) {
            return null;
        }

        return Event.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();
    }

    public void updateEntityFromDTO(Event event, EventDTO.UpdateEventRequest request) {
        if (request == null) {
            return;
        }

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }
    }
}