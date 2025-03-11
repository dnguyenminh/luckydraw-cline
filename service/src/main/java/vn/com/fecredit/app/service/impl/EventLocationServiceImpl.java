package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.service.EventLocationService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLocationServiceImpl implements EventLocationService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_INACTIVE = 0;

    private final EventLocationRepository eventLocationRepository;

    @Override
    @Transactional
    public EventLocation create(EventLocation location) {
        validateLocation(location);
        return eventLocationRepository.save(location);
    }

    @Override
    public Optional<EventLocation> findById(Long id) {
        return eventLocationRepository.findByIdWithRelationships(id);
    }

    @Override
    @Transactional
    public EventLocation update(EventLocation location) {
        validateLocation(location);
        validateExists(location.getId());
        return eventLocationRepository.save(location);
    }

    @Override
    public EventLocation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EventLocation", id));
    }

    @Override
    @Transactional
    public void activate(Long id) {
        EventLocation location = getById(id);
        location.activate();
        eventLocationRepository.save(location);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        EventLocation location = getById(id);
        if (eventLocationRepository.hasActiveParticipants(location.getId(), STATUS_ACTIVE)) {
            throw new IllegalStateException("Cannot deactivate location with active participants");
        }
        location.deactivate();
        eventLocationRepository.save(location);
    }

    @Override
    public Page<EventLocation> findAll(Pageable pageable) {
        return eventLocationRepository.findAll(pageable);
    }

    @Override
    public Page<EventLocation> findAllActive(Pageable pageable) {
        return eventLocationRepository.findAllByStatus(STATUS_ACTIVE, pageable);
    }

    @Override
    public List<EventLocation> findAllByEventId(Long eventId) {
        return eventLocationRepository.findAllByEventId(eventId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EventLocation location = getById(id);
        if (eventLocationRepository.hasActiveParticipants(location.getId(), STATUS_ACTIVE)) {
            throw new IllegalStateException("Cannot delete location with active participants");
        }
        if (eventLocationRepository.hasActiveRewards(location.getId(), STATUS_ACTIVE)) {
            throw new IllegalStateException("Cannot delete location with active rewards");
        }
        location.setStatus(STATUS_INACTIVE);
        eventLocationRepository.save(location);
    }

    @Override
    public List<EventLocation> findAll() {
        return eventLocationRepository.findAll();
    }

    @Override
    public List<EventLocation> findAllActive() {
        return eventLocationRepository.findAllByStatus(STATUS_ACTIVE);
    }

    @Override
    public List<EventLocation> findAllByEventId(Long eventId, boolean activeOnly) {
        if (activeOnly) {
            return eventLocationRepository.findAllByEventIdAndStatus(eventId, STATUS_ACTIVE);
        }
        return eventLocationRepository.findAllByEventId(eventId);
    }

    private void validateLocation(EventLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (location.getEvent() == null) {
            throw new IllegalArgumentException("Event is required");
        }
        if (location.getRegion() == null) {
            throw new IllegalArgumentException("Region is required");
        }
        if (location.getProvince() == null) {
            throw new IllegalArgumentException("Province is required");
        }
    }

    private void validateExists(Long id) {
        if (!eventLocationRepository.existsById(id)) {
            throw new EntityNotFoundException("EventLocation", id);
        }
    }
}
