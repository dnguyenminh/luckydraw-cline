package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public EventDTO getEvent(Long id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return eventMapper.toDTO(event);
    }

    @Transactional
    public EventDTO createEvent(EventDTO.CreateEventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, EventDTO.UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        eventMapper.updateEntityFromDTO(event, request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        if (!event.getParticipants().isEmpty()) {
            throw new IllegalStateException("Cannot delete event with participants");
        }
        eventRepository.delete(event);
    }

    @Transactional
    public EventDTO activateEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        event.setIsActive(true);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional
    public EventDTO deactivateEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        event.setIsActive(false);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDTO(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventDTO.EventStatistics getEventStatistics(Long eventId) {
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
}