package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.EventLocation;

import java.time.LocalDateTime;
import java.util.List;

public interface EventLocationService {

    // Basic CRUD operations
    EventLocationDTO createLocation(EventLocationDTO.CreateRequest request);
    EventLocationDTO getLocation(Long id);
    EventLocationDTO updateLocation(Long id, EventLocationDTO.UpdateRequest request);
    void deleteLocation(Long id);

    // Status management
    void activateLocation(Long id);
    void deactivateLocation(Long id);

    // Listing operations
    Page<EventLocationDTO> listLocations(Pageable pageable);
    Page<EventLocationDTO> listActiveLocations(Pageable pageable);
    List<EventLocationDTO> listEventLocations(Long eventId);
    
    // Summary operations
    List<EventLocationDTO.Summary> getLocationSummaries();
    List<EventLocationDTO.Summary> getActiveLocationSummaries();
    List<EventLocationDTO.Summary> getEventLocationSummaries(Long eventId);

    // Statistics operations
    EventLocationDTO.Statistics getEventLocationStatistics(Long eventId);
    EventLocationDTO.Statistics getLocationStatistics(Long locationId, LocalDateTime startDate, LocalDateTime endDate);

    // Spin management
    void updateSpinLimits(Long locationId, Integer newLimit);
    Integer getRemainingSpinsToday(Long locationId);
    void decrementRemainingSpins(Long locationId);

    // Internal operations used by other services
    EventLocation getLocationEntity(Long id);
    boolean isLocationActive(Long id);
    boolean hasAvailableSpins(Long locationId);
}
