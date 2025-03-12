package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.EventLocationDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventLocationService {
    // Core CRUD operations
    EventLocationDTO.Response createLocation(EventLocationDTO.CreateRequest request);
    EventLocationDTO.Response updateLocation(Long id, EventLocationDTO.UpdateRequest request);
    Optional<EventLocationDTO.Response> getLocation(Long id);
    void deleteLocation(Long id);

    // List operations with filtering
    Page<EventLocationDTO.Response> listLocations(Pageable pageable);
    Page<EventLocationDTO.Response> listActiveLocations(Pageable pageable);
    List<EventLocationDTO.Response> listEventLocations(Long eventId);
    
    // Status management
    void activateLocation(Long id);
    void deactivateLocation(Long id);
    
    // Summary views
    List<EventLocationDTO.Summary> getLocationSummaries();
    List<EventLocationDTO.Summary> getActiveLocationSummaries();
    List<EventLocationDTO.Summary> getEventLocationSummaries(Long eventId);
    
    // Statistics and analytics
    EventLocationDTO.Statistics getLocationStatistics(Long id, LocalDateTime startDate, LocalDateTime endDate);
    EventLocationDTO.LocationStatistics getEventLocationStatistics(Long id);
    
    // Operational features
    void updateSpinLimits(Long id, Integer newDailyLimit);
    Long getRemainingSpinsToday(Long id);
    Long decrementRemainingSpins(Long id);
}
