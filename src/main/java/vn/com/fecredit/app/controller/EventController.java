package vn.com.fecredit.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO.CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents(true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO.UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<EventDTO.EventStatistics> getEventStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventStatistics(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<EventDTO.EventSummary> getEventSummary(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventSummary(id));
    }
}