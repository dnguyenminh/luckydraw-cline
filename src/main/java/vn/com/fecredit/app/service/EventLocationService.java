package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;

import vn.com.fecredit.app.dto.EventDTO.EventResponse;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.EventLocation;

import java.util.List;
import java.util.Optional;

public interface EventLocationService {

    /**
     * Create new event location
     */
    EventLocationDTO.EventLocationResponse createEventLocation(EventLocationDTO.CreateRequest request);

    /**
     * Update event location
     */
    EventLocationDTO.EventLocationResponse updateEventLocation(Long id, EventLocationDTO.UpdateRequest request);

    /**
     * Delete location
     */
    void deleteEventLocation(Long id);

    /**
     * Get location by ID
     */
    EventLocationDTO.EventLocationResponse getLocationById(Long id);

    /**
     * Get all locations
     */
    List<EventLocationDTO.LocationSummary> getAllLocations();

    /**
     * Get all active locations
     */
    List<EventLocationDTO.LocationSummary> getActiveLocations();

    /**
     * Get paginated locations
     */
    Page<EventLocationDTO.LocationSummary> getLocations(PageRequest pageRequest);

    /**
     * Search locations
     */
    Page<EventLocationDTO.LocationSummary> searchLocations(SearchRequest searchRequest);

    /**
     * Get locations by event ID
     */
    List<EventLocationDTO.LocationSummary> getLocationsByEventId(Long eventId);

    /**
     * Get location events
     */
    List<EventResponse> getLocationEvents(Long id);

    /**
     * Get location status
     */
    EventLocationDTO.LocationStatus getLocationStatus(Long id);

    /**
     * Get location statistics
     */
    EventLocationDTO.LocationStatistics getLocationStatistics(Long id);

    /**
     * Update location status
     */
    void updateLocationStatus(Long id, boolean active);

    /**
     * Decrement remaining spins
     */
    void decrementRemainingSpins(Long id);

    /**
     * Check if location exists
     */
    boolean existsById(Long id);

    /**
     * Count locations
     */
    long countLocations();

    /**
     * Count active locations
     */
    long countActiveLocations();

    /**
     * Find location by ID
     */
    Optional<EventLocation> findById(Long id);

    /**
     * Find all locations
     */
    List<EventLocation> findAll();

    /**
     * Find active locations
     */
    List<EventLocation> findAllActive();
}
