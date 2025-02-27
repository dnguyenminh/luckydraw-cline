package vn.com.fecredit.app.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.event.CreateEventRequest;
import vn.com.fecredit.app.dto.event.UpdateEventRequest;
import vn.com.fecredit.app.dto.event.EventResponse;
import vn.com.fecredit.app.dto.event.EventSummary;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.mapper.common.AbstractBaseMapper;

@Component
@RequiredArgsConstructor
public class EventMapper extends AbstractBaseMapper<Event, EventResponse, CreateEventRequest, UpdateEventRequest> {

    private final EventLocationMapper locationMapper;
    private final RewardMapper rewardMapper;

    @Override
    public Event toEntity(CreateEventRequest request) {
        return Event.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dailySpinLimit(request.getDailySpinLimit())
                .totalSpins(request.getTotalSpins())
                .remainingSpins(request.getTotalSpins())
                .active(request.getActive() != null ? request.getActive() : true)
                .deleted(false)
                .build();
    }

    @Override
    public void updateEntity(UpdateEventRequest request, Event event) {
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
        if (request.getDailySpinLimit() != null) {
            event.setDailySpinLimit(request.getDailySpinLimit());
        }
        if (request.getTotalSpins() != null) {
            event.setTotalSpins(request.getTotalSpins());
            if (event.getRemainingSpins() == null || event.getRemainingSpins() > request.getTotalSpins()) {
                event.setRemainingSpins(request.getTotalSpins());
            }
        }
        if (request.getActive() != null) {
            event.setActive(request.getActive());
        }
    }

    @Override
    public EventResponse toResponse(Event event) {
        if (event == null) {
            return null;
        }

        return EventResponse.builder()
                .id(event.getId())
                .code(event.getCode())
                .name(event.getName())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .dailySpinLimit(event.getDailySpinLimit())
                .totalSpins(event.getTotalSpins())
                .remainingSpins(event.getRemainingSpins())
                .active(event.isActive())
                .deleted(event.isDeleted())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .locations(event.getLocations() != null ? event.getLocations().stream()
                        .map(locationMapper::toResponse)
                        .collect(Collectors.toList()) : null)
                .rewards(event.getRewards() != null ? event.getRewards().stream()
                        .map(rewardMapper::toResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public List<EventSummary> toSummaryList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public EventSummary toSummary(Event event) {
        if (event == null) {
            return null;
        }

        return EventSummary.builder()
                .id(event.getId())
                .code(event.getCode())
                .name(event.getName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .remainingSpins(event.getRemainingSpins())
                .active(event.isActive())
                .build();
    }
}
