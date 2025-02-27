package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.dto.EventDTO.CreateRequest;
import vn.com.fecredit.app.dto.EventDTO.UpdateRequest;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;

public interface EventService {

    // Core CRUD operations
    EventDTO.EventResponse createEvent(CreateRequest request);
    EventDTO.EventResponse updateEvent(Long id, UpdateRequest request);
    EventDTO.EventResponse getEventById(Long id);
    void deleteEvent(Long id);

    // Event Status Management
    boolean activateEvent(Long id);
    boolean deactivateEvent(Long id);
    boolean pauseEvent(Long id);
    boolean resumeEvent(Long id);
    boolean startEvent(Long id);
    boolean endEvent(Long id);
    boolean updateEventStatus(Long id, String status);

    // Event Queries
    List<EventDTO.EventSummary> getAllEvents();
    List<EventDTO.EventSummary> getActiveEvents();
    List<EventDTO.EventSummary> getCurrentEvents();
    List<EventDTO.EventSummary> getUpcomingEvents();
    List<EventDTO.EventSummary> getPastEvents();
    List<EventDTO.EventSummary> getEventsByLocation(Long locationId);
    List<EventDTO.EventSummary> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Page<EventDTO.EventSummary> getEvents(PageRequest pageRequest);
    Page<EventDTO.EventSummary> searchEvents(SearchRequest searchRequest);

    // Event State Checks
    boolean isEventActive(Long id);
    boolean hasEventStarted(Long id);
    boolean hasEventEnded(Long id);
    boolean isEventOngoing(Long id);
    
    // Event Code Management
    boolean existsByCode(String code);
    EventDTO.EventResponse getEventByCode(String code);
    EventDTO.EventResponse findByCode(String code);

    // Event Statistics and Status
    EventDTO.EventStatus getEventStatus(Long id);
    EventDTO.EventStatistics getEventStatistics(Long id);
    long countEvents();
    long countActiveEvents();
    long countEventsByStatus(String status);
    long countEventsByLocation(Long locationId);

    // Event Location Management
    void updateEventLocation(Long eventId, Long locationId);

    // Event Participant Management
    boolean checkParticipantEligibility(Long eventId, Long participantId);
    List<ParticipantDTO.ParticipantSummary> getEventParticipants(Long eventId);

    // Event Reward Management
    void addRewardToEvent(Long eventId, Long rewardId);
    void removeRewardFromEvent(Long eventId, Long rewardId);
    List<RewardDTO.RewardResponse> getEventRewards(Long eventId);

    // Event Date Management
    void updateEventDates(Long eventId, LocalDateTime startDate, LocalDateTime endDate);
}
