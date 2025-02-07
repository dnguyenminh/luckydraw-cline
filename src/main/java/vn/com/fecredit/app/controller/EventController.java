package vn.com.fecredit.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO.CreateEventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(eventService.getAllEvents(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, 
                                              @Valid @RequestBody EventDTO.UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<EventDTO> activateEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.activateEvent(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<EventDTO> deactivateEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.deactivateEvent(id));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<EventDTO.EventStatistics> getEventStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventStatistics(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<EventDTO.EventSummary> getEventSummary(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventSummary(id));
    }

    @GetMapping("/{eventId}/participants/{participantId}/eligibility")
    public ResponseEntity<Boolean> checkParticipantEligibility(@PathVariable Long eventId,
                                                              @PathVariable Long participantId) {
        return ResponseEntity.ok(eventService.isParticipantEligible(eventId, participantId));
    }
}