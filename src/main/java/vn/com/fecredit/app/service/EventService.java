package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getAllEvents(Boolean activeOnly) {
        List<Event> events;
        if (activeOnly != null && activeOnly) {
            events = eventRepository.findByIsActiveTrue();
        } else {
            events = eventRepository.findAll();
        }
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO getEvent(Long id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        // Check if the event's end date has passed and deactivate it if necessary
        if (event.getEndDate() != null && event.getEndDate().isBefore(LocalDateTime.now()) && event.getIsActive()) {
            event.setIsActive(false);
            eventRepository.save(event);
        }
        
        return eventMapper.toDTO(event);
    }

    @Transactional
    public EventDTO createEvent(EventDTO.CreateEventRequest request) {
        Assert.notNull(request, "Create event request cannot be null");
        validateEventDates(request.getStartDate(), request.getEndDate());
        
        Event event = eventMapper.toEntity(request);
        validateEvent(event);
        
        if (eventRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Event code must be unique");
        }

        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, EventDTO.UpdateEventRequest request) {
        Assert.notNull(request, "Update event request cannot be null");
        Assert.notNull(eventId, "Event ID cannot be null");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        validateEventDates(request.getStartDate(), request.getEndDate());
        eventMapper.updateEntityFromDTO(event, request);
        validateEvent(event);
        
        // Update the updatedAt timestamp
        event.setUpdatedAt(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        if (!event.getParticipants().isEmpty()) {
            throw new IllegalStateException("Cannot delete event with participants");
        }
        eventRepository.delete(event);
    }

    @Transactional
    public EventDTO activateEvent(Long eventId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        if (event.getEndDate() != null && event.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot activate event that has ended");
        }
        
        validateEventDates(event.getStartDate(), event.getEndDate());
        event.setIsActive(true);
        event.setUpdatedAt(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public EventDTO deactivateEvent(Long eventId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        event.setIsActive(false);
        event.setUpdatedAt(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventDTO.EventStatistics getEventStatistics(Long eventId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        long totalRewardsGiven = event.getSpinHistories().stream()
                .filter(spinHistory -> spinHistory.getWon() != null && spinHistory.getWon())
                .count();

        return EventDTO.EventStatistics.builder()
                .id(event.getId())
                .name(event.getName())
                .totalParticipants(event.getParticipants().size())
                .totalSpins(event.getSpinHistories().size())
                .totalRewardsGiven((int) totalRewardsGiven)
                .build();
    }

    @Transactional(readOnly = true)
    public EventDTO.EventSummary getEventSummary(Long eventId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        int remainingRewards = event.getRewards().stream()
                .mapToInt(Reward::getRemainingQuantity)
                .sum();

        return EventDTO.EventSummary.builder()
                .id(event.getId())
                .name(event.getName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isActive(event.getIsActive())
                .participantCount(event.getParticipants().size())
                .totalSpins(event.getSpinHistories().size())
                .remainingRewards(remainingRewards)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isParticipantEligible(Long eventId, Long participantId) {
        Assert.notNull(eventId, "Event ID cannot be null");
        Assert.notNull(participantId, "Participant ID cannot be null");
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        Participant participant = event.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "id", participantId));

        if (participant.getSpinsRemaining() <= 0) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getStartDate() != null && now.isBefore(event.getStartDate())) {
            return false;
        }

        if (event.getEndDate() != null && now.isAfter(event.getEndDate())) {
            return false;
        }

        return event.getIsActive() && 
               participant.getSpinsRemaining() > 0 && 
               event.getSpinHistories().stream()
                    .filter(sh -> sh.getParticipant().getId().equals(participantId))
                    .count() < participant.getSpinsRemaining();
    }

    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && startDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event start date cannot be in the past");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Event start date must be before end date");
        }
    }

    private void validateEvent(Event event) {
        Assert.notNull(event, "Event cannot be null");
        Assert.hasText(event.getName(), "Event name cannot be empty");
        
        if (event.getStartDate() != null && event.getEndDate() != null) {
            validateEventDates(event.getStartDate(), event.getEndDate());
        }
    }
}