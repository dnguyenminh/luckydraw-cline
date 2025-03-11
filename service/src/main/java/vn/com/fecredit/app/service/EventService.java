package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.dto.EventDTO;
import java.time.LocalDateTime;

public interface EventService {

    EventDTO.Response createEvent(EventDTO.CreateRequest request);

    EventDTO.Response updateEvent(Long id, EventDTO.UpdateRequest request);
    
    EventDTO.Response getEventById(Long id);
    
    EventDTO.Response findByCode(String code);
    
    void deleteEvent(Long id);
    
    Page<EventDTO.Response> getAllEvents(Pageable pageable);
    
    Page<EventDTO.Response> listEvents(EntityStatus status, Pageable pageable);
    
    Page<EventDTO.Response> searchEvents(String searchText, 
                                       LocalDateTime startDate, 
                                       LocalDateTime endDate, 
                                       EntityStatus status, 
                                       Pageable pageable);

    EventDTO.Statistics getEventStatistics();
    
    EventDTO.Statistics getEventStatistics(Long eventId);
    
    EventDTO.Response updateEventStatus(Long id, EntityStatus status);
    
    boolean existsByCode(String code);
    
    boolean isActive(Long id);
    
    boolean hasActiveEvent();
    
    boolean canCreateNewEvent();
}
