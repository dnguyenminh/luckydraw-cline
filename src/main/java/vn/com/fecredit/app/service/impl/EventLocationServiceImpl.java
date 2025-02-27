package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.EventDTO.EventResponse;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.EventLocationService;
import vn.com.fecredit.app.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class EventLocationServiceImpl implements EventLocationService {

    private final EventLocationRepository eventLocationRepository;
    private final EventRepository eventRepository;
    private final EventLocationMapper eventLocationMapper;
    private final EventMapper eventMapper;

    @Override
    public EventLocationDTO.EventLocationResponse createEventLocation(EventLocationDTO.CreateRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        EventLocation location = eventLocationMapper.createEntityFromRequest(request);
        location = eventLocationRepository.save(location);
        
        return eventLocationMapper.toResponse(location);
    }

    @Override
    public EventLocationDTO.EventLocationResponse updateEventLocation(Long id, EventLocationDTO.UpdateRequest request) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        
        eventLocationMapper.updateEntityFromRequest(location, request);
        location = eventLocationRepository.save(location);
        
        return eventLocationMapper.toResponse(location);
    }

    @Override
    public void deleteEventLocation(Long id) {
        if (!eventLocationRepository.existsById(id)) {
            throw new ResourceNotFoundException("EventLocation not found with id: " + id);
        }
        eventLocationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EventLocationDTO.EventLocationResponse getLocationById(Long id) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        return eventLocationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.LocationSummary> getAllLocations() {
        return eventLocationMapper.toSummaryList(eventLocationRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.LocationSummary> getActiveLocations() {
        return eventLocationMapper.toSummaryList(findAllActive());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventLocationDTO.LocationSummary> getLocations(PageRequest pageRequest) {
        Pageable pageable = pageRequest.toSpringPageRequest();
        Page<EventLocation> locationPage = eventLocationRepository.findAll(pageable);
        List<EventLocationDTO.LocationSummary> summaries = eventLocationMapper.toSummaryList(locationPage.getContent());
        
        return new PageImpl<>(summaries, pageable, locationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventLocationDTO.LocationSummary> searchLocations(SearchRequest searchRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize()
        );
        
        // TODO: Implement actual search functionality
        Page<EventLocation> locationPage = eventLocationRepository.findAll(pageable);
        List<EventLocationDTO.LocationSummary> summaries = eventLocationMapper.toSummaryList(locationPage.getContent());
        
        return new PageImpl<>(summaries, pageable, locationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.LocationSummary> getLocationsByEventId(Long eventId) {
        return eventLocationMapper.toSummaryList(eventLocationRepository.findByEventId(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getLocationEvents(Long id) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        return List.of(eventMapper.toResponse(location.getEvent()));
    }

    @Override
    @Transactional(readOnly = true)
    public EventLocationDTO.LocationStatus getLocationStatus(Long id) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        return eventLocationMapper.calculateStatus(location);
    }

    @Override
    @Transactional(readOnly = true)
    public EventLocationDTO.LocationStatistics getLocationStatistics(Long id) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        return eventLocationMapper.calculateStatistics(location);
    }

    @Override
    public void updateLocationStatus(Long id, boolean active) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        location.setActive(active);
        eventLocationRepository.save(location);
    }

    @Override
    @Transactional
    public void decrementRemainingSpins(Long id) {
        EventLocation location = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        
        Integer remainingSpins = location.getRemainingSpins();
        if (remainingSpins != null && remainingSpins > 0) {
            location.setRemainingSpins(remainingSpins - 1);
            eventLocationRepository.save(location);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return eventLocationRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLocations() {
        return eventLocationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveLocations() {
        return eventLocationRepository.countByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventLocation> findById(Long id) {
        return eventLocationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findAll() {
        return eventLocationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findAllActive() {
        return eventLocationRepository.findByActiveTrue();
    }
}
