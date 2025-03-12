package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.EventDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {
    
    EventDTO.Response createEvent(EventDTO.CreateRequest request);
    
    EventDTO.Response updateEvent(Long id, EventDTO.UpdateRequest request);
    
    Page<EventDTO.Response> listEvents(EntityStatus status, Pageable pageable);
    
    Optional<EventDTO.Response> getEvent(Long id);
    
    Optional<EventDTO.Response> findEventByCode(String code);
    
    EventDTO.Statistics getEventStatistics();
    
    Page<EventDTO.Response> searchEvents(String searchTerm, LocalDateTime startDate, 
            LocalDateTime endDate, EntityStatus status, Pageable pageable);

    Page<EventDTO.Response> searchEvents(String searchTerm, String startDateStr, 
            String endDateStr, Pageable pageable);
    
    void deleteEvent(Long id);
    
    boolean existsByCode(String code);
    
    List<EventDTO.Response> findActiveEvents();
    
    void updateEventStatus(Long id, EntityStatus status);
    
    long getEventParticipantCount(Long id);
}
