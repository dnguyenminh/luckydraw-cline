package vn.com.fecredit.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.service.EventService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventDTO.EventResponse createEvent(EventDTO.CreateRequest request) {
        Event event = eventMapper.toEntity(request.toNew());
        event = eventRepository.save(event);
        return EventDTO.EventResponse.fromNew(eventMapper.toResponse(event));
    }

    @Override
    @Transactional
    public EventDTO.EventResponse updateEvent(Long id, EventDTO.UpdateRequest request) {
        Event event = findEventById(id);
        eventMapper.updateEntity(request.toNew(), event);
        event = eventRepository.save(event);
        return EventDTO.EventResponse.fromNew(eventMapper.toResponse(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventDTO.EventResponse getEventById(Long id) {
        Event event = findEventById(id);
        return EventDTO.EventResponse.fromNew(eventMapper.toResponse(event));
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventById(id);
        event.setDeleted(true);
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public boolean activateEvent(Long id) {
        Event event = findEventById(id);
        event.setActive(true);
        eventRepository.save(event);
        return true;
    }

    @Override
    @Transactional
    public boolean deactivateEvent(Long id) {
        Event event = findEventById(id);
        event.setActive(false);
        eventRepository.save(event);
        return true;
    }

    @Override
    public boolean pauseEvent(Long id) {
        // Implement pause logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean resumeEvent(Long id) {
        // Implement resume logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean startEvent(Long id) {
        // Implement start logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean endEvent(Long id) {
        // Implement end logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean updateEventStatus(Long id, String status) {
        // Implement status update logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.EventSummary> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.EventSummary> getActiveEvents() {
        List<Event> events = eventRepository.findByActiveTrue();
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.EventSummary> getCurrentEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.EventSummary> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByActiveTrueAndStartDateAfter(now);
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.EventSummary> getPastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByEndDateBefore(now);
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDTO.EventSummary> getEventsByLocation(Long locationId) {
        // Implement location-based query
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EventDTO.EventSummary> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Event> events = eventRepository.findByStartDateBetween(startDate, endDate);
        return eventMapper.toSummaryList(events).stream()
                .map(EventDTO.EventSummary::fromNew)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EventDTO.EventSummary> getEvents(PageRequest pageRequest) {
        // Implement pagination
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<EventDTO.EventSummary> searchEvents(SearchRequest searchRequest) {
        // Implement search
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isEventActive(Long id) {
        return findEventById(id).isActive();
    }

    @Override
    public boolean hasEventStarted(Long id) {
        Event event = findEventById(id);
        return event.getStartDate().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean hasEventEnded(Long id) {
        Event event = findEventById(id);
        return event.getEndDate().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isEventOngoing(Long id) {
        Event event = findEventById(id);
        LocalDateTime now = LocalDateTime.now();
        return event.isActive() && 
               event.getStartDate().isBefore(now) && 
               event.getEndDate().isAfter(now);
    }

    @Override
    public boolean existsByCode(String code) {
        return eventRepository.existsByCode(code);
    }

    @Override
    public EventDTO.EventResponse getEventByCode(String code) {
        Event event = eventRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with code " + code));
        return EventDTO.EventResponse.fromNew(eventMapper.toResponse(event));
    }

    @Override
    public EventDTO.EventResponse findByCode(String code) {
        return getEventByCode(code);
    }

    @Override
    public EventDTO.EventStatus getEventStatus(Long id) {
        // Implement status check
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EventDTO.EventStatistics getEventStatistics(Long id) {
        // Implement statistics calculation
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long countEvents() {
        return eventRepository.count();
    }

    @Override
    public long countActiveEvents() {
        return eventRepository.countByActiveTrue();
    }

    @Override
    public long countEventsByStatus(String status) {
        // Implement status-based counting
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long countEventsByLocation(Long locationId) {
        // Implement location-based counting
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateEventLocation(Long eventId, Long locationId) {
        // Implement location update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkParticipantEligibility(Long eventId, Long participantId) {
        Event event = findEventById(eventId);
        Optional<Participant> participant = participantRepository.findById(participantId);
        
        if (participant.isEmpty()) {
            return false;
        }

        return participant.get().isActive() && 
               participant.get().getEvent().getId().equals(eventId);
    }

    @Override
    public List<ParticipantDTO.ParticipantSummary> getEventParticipants(Long eventId) {
        // Implement participants query
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void addRewardToEvent(Long eventId, Long rewardId) {
        // Implement reward addition
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removeRewardFromEvent(Long eventId, Long rewardId) {
        // Implement reward removal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<RewardDTO.RewardResponse> getEventRewards(Long eventId) {
        // Implement rewards query
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public void updateEventDates(Long eventId, LocalDateTime startDate, LocalDateTime endDate) {
        Event event = findEventById(eventId);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        eventRepository.save(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }
}
