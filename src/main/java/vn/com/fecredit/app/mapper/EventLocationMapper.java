package vn.com.fecredit.app.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.mapper.common.AbstractBaseMapper;
import vn.com.fecredit.app.repository.EventRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventLocationMapper extends AbstractBaseMapper<EventLocation, EventLocationDTO> {

    private final EventRepository eventRepository;

    @Override
    public EventLocation toEntity(EventLocationDTO dto) {
        if (dto == null) {
            return null;
        }

        Event event = dto.getEventId() != null ? 
            eventRepository.findById(dto.getEventId()).orElse(null) : null;

        EventLocation location = EventLocation.builder()
                .id(dto.getId())
                .name(dto.getName())
                .province(dto.getProvince())
                .district(dto.getDistrict())
                .ward(dto.getWard())
                .address(dto.getAddress())
                .city(dto.getCity())
                .event(event)
                .build();

        if (dto.getIsActive() != null) {
            location.setActive(dto.getIsActive());
        }

        return location;
    }

    public EventLocation createEntityFromRequest(EventLocationDTO.CreateRequest request) {
        if (request == null) {
            return null;
        }

        Event event = request.getEventId() != null ? 
            eventRepository.findById(request.getEventId()).orElse(null) : null;

        return EventLocation.builder()
                .name(request.getName())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .address(request.getAddress())
                .city(request.getCity())
                .event(event)
                .build();
    }

    public void updateEntityFromRequest(EventLocation entity, EventLocationDTO.UpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getProvince() != null) {
            entity.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            entity.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            entity.setWard(request.getWard());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            entity.setCity(request.getCity());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
    }

    @Override
    public EventLocationDTO toDto(EventLocation entity) {
        if (entity == null) {
            return null;
        }

        return EventLocationDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .address(entity.getAddress())
                .city(entity.getCity())
                .eventId(entity.getEventId())
                .eventName(Objects.nonNull(entity.getEvent()) ? entity.getEvent().getName() : null)
                .status(entity.getStatus())
                .isActive(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .lastModifiedAt(entity.getLastModifiedAt())
                .lastModifiedBy(entity.getLastModifiedBy())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .version(entity.getVersion())
                .build();
    }

    public List<EventLocationDTO.EventLocationResponse> toResponseList(List<EventLocation> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventLocationDTO.EventLocationResponse toResponse(EventLocation entity) {
        if (entity == null) {
            return null;
        }

        return EventLocationDTO.EventLocationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .address(entity.getAddress())
                .city(entity.getCity())
                .isActive(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    public EventLocationDTO.EventLocationSummary toSummary(EventLocation entity) {
        if (entity == null) {
            return null;
        }

        return EventLocationDTO.EventLocationSummary.builder()
                .id(entity.getId())
                .name(entity.getName())
                .province(entity.getProvince())
                .city(entity.getCity())
                .isActive(entity.getActive())
                .build();
    }

    public List<EventLocationDTO.EventLocationSummary> toSummaryList(List<EventLocation> entities) {
        return entities.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
