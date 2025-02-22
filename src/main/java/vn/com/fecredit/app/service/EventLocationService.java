package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class EventLocationService {

    private final EventLocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventLocationMapper mapper;

    @Transactional(readOnly = true)
    public EventLocationDTO findById(Long id) {
        return locationRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
    }

    public EventLocationDTO create(EventLocationDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        EventLocation location = mapper.toEntity(dto, event);
        location = locationRepository.save(location);
        return mapper.toDTO(location);
    }

    public EventLocationDTO update(Long id, EventLocationDTO dto) {
        EventLocation existing = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));

        mapper.updateEntityFromDTO(dto, existing);
        existing = locationRepository.save(existing);
        return mapper.toDTO(existing);
    }

    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("EventLocation not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }

    public void updateRemainingSpins(Long id, Long newValue) {
        EventLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventLocation not found with id: " + id));
        
        location.setRemainingSpins(newValue);
        locationRepository.save(location);
    }

    public EventLocation createEventLocation(Event event, String name, Long totalSpins) {
        EventLocation location = EventLocation.builder()
                .event(event)
                .name(name)
                .totalSpins(totalSpins)
                .remainingSpins(totalSpins)
                .isActive(true)
                .build();
        
        return locationRepository.save(location);
    }

    @Transactional
    public boolean allocateSpin(Long id) {
        try {
            int updatedRows = locationRepository.decrementSpins(id);
            locationRepository.flush(); // Ensure changes are written to DB
            return updatedRows > 0;
        } catch (Exception e) {
//            log.error("Error while allocating spin for location {}: {}", id, e.getMessage());
            return false;
        }
    }
}