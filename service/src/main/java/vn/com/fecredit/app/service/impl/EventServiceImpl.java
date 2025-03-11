package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.EventService;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventDTO.Response createEvent(EventDTO.CreateRequest request) {
        Event event = eventMapper.toEntity(request);
        event.setStatus(EntityStatus.ACTIVE.getValue());
        event = eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    public EventDTO.Response updateEvent(Long id, EventDTO.UpdateRequest request) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", id));
            
        eventMapper.updateEntity(event, request);
        event = eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    public EventDTO.Response getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return eventMapper.toResponse(event);
    }

    @Override
    public EventDTO.Response findByCode(String code) {
        Event event = eventRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "code", code));
        return eventMapper.toResponse(event);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        event.setStatus(EntityStatus.DELETED.getValue());
        eventRepository.save(event);
    }

    @Override
    public Page<EventDTO.Response> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(eventMapper::toResponse);
    }

    @Override
    public Page<EventDTO.Response> listEvents(EntityStatus status, Pageable pageable) {
        return eventRepository.findByStatus(status.getValue(), pageable)
            .map(eventMapper::toResponse);
    }

    @Override
    public Page<EventDTO.Response> searchEvents(String searchText, 
                                              LocalDateTime startDate, 
                                              LocalDateTime endDate, 
                                              EntityStatus status, 
                                              Pageable pageable) {
        return eventRepository.findBySearchCriteria(searchText, startDate, endDate, status.getValue(), pageable)
            .map(eventMapper::toResponse);
    }

    @Override
    public EventDTO.Statistics getEventStatistics() {
        return eventMapper.toStatistics(eventRepository.findAll());
    }

    @Override
    public EventDTO.Statistics getEventStatistics(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        return eventMapper.toStatistics(java.util.List.of(event));
    }

    @Override
    public EventDTO.Response updateEventStatus(Long id, EntityStatus status) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        event.setStatus(status.getValue());
        event = eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    public boolean existsByCode(String code) {
        return eventRepository.existsByCode(code);
    }

    @Override
    public boolean isActive(Long id) {
        return eventRepository.findById(id)
            .map(Event::isActive)
            .orElse(false);
    }

    @Override
    public boolean hasActiveEvent() {
        return eventRepository.existsByStatus(EntityStatus.ACTIVE.getValue());
    }

    @Override
    public boolean canCreateNewEvent() {
        return !hasActiveEvent();
    }
}
