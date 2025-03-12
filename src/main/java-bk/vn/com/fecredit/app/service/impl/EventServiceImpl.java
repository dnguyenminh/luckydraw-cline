package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.exception.EventNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    @Transactional
    public EventDTO.Response createEvent(EventDTO.CreateRequest request) {
        Event event = eventMapper.toEntity(request);
        event = eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventDTO.Response updateEvent(Long id, EventDTO.UpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        eventMapper.updateEntity(event, request);
        event = eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO.Response> listEvents(EntityStatus status, Pageable pageable) {
        return eventRepository.findAllByStatus(status, pageable)
                .map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventDTO.Response> getEvent(Long id) {
        return eventRepository.findById(id)
                .map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventDTO.Response> findEventByCode(String code) {
        return eventRepository.findByCode(code)
                .map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDTO.Statistics getEventStatistics() {
        List<Event> events = eventRepository.findAll();
        return eventMapper.toStatistics(events);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO.Response> searchEvents(String searchTerm, LocalDateTime startDate, 
            LocalDateTime endDate, EntityStatus status, Pageable pageable) {
        return eventRepository.searchEvents(searchTerm, startDate, endDate, status, pageable)
                .map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO.Response> searchEvents(String searchTerm, String startDateStr, 
            String endDateStr, Pageable pageable) {
        LocalDateTime startDate = startDateStr != null ? 
                LocalDateTime.parse(startDateStr, DATE_FORMATTER) : null;
        LocalDateTime endDate = endDateStr != null ? 
                LocalDateTime.parse(endDateStr, DATE_FORMATTER) : null;
        return searchEvents(searchTerm, startDate, endDate, EntityStatus.ACTIVE, pageable);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        event.setStatus(EntityStatus.DELETED);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return eventRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO.Response> findActiveEvents() {
        return eventRepository.findActiveEvents().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateEventStatus(Long id, EntityStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        event.setStatus(status);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventParticipantCount(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        return event.getParticipants().size();
    }
}
