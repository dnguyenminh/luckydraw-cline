package vn.com.fecredit.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.ApiResponse;
import vn.com.fecredit.app.dto.Summary;
import vn.com.fecredit.app.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN')")  // Role-based access
    public ResponseEntity<Summary.Response> createEvent(@Valid @RequestBody Summary.CreateRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN')")  // Multiple roles allowed
    public ResponseEntity<Summary.Response> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Summary.UpdateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN', 'REPORT_VIEWER')")  // Read access for viewers
    public ResponseEntity<Summary.Response> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id)
                .orElseThrow(() -> new RuntimeException("Event not found")));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN', 'REPORT_VIEWER')")
    public ResponseEntity<Page<Summary.Response>> listEvents(
            @RequestParam(required = false) EntityStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.listEvents(status, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only admin can delete
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN', 'REPORT_VIEWER')")
    public ResponseEntity<Summary.Statistics> getEventStatistics() {
        return ResponseEntity.ok(eventService.getEventStatistics());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN', 'REPORT_VIEWER')")
    public ResponseEntity<Page<Summary.Response>> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.searchEvents(keyword, startDate, endDate, pageable));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EntityStatus status) {
        eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Event status updated successfully"));
    }

    @GetMapping("/{id}/participants/count")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'ADMIN', 'REPORT_VIEWER')")
    public ResponseEntity<Long> getEventParticipantCount(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventParticipantCount(id));
    }
}
