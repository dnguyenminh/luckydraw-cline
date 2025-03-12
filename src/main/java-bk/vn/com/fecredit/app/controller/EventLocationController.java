package vn.com.fecredit.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.ApiResponse;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.service.EventLocationService;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class EventLocationController {

    private final EventLocationService locationService;

    @PostMapping
    public ResponseEntity<EventLocationDTO.Response> createLocation(
            @Valid @RequestBody EventLocationDTO.CreateRequest request) {
        return ResponseEntity.ok(locationService.createLocation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventLocationDTO.Response> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody EventLocationDTO.UpdateRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventLocationDTO.Response> getLocation(@PathVariable Long id) {
        return ResponseEntity.of(locationService.getLocation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Location deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<Page<EventLocationDTO.Response>> listLocations(Pageable pageable) {
        return ResponseEntity.ok(locationService.listLocations(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<EventLocationDTO.Response>> listActiveLocations(Pageable pageable) {
        return ResponseEntity.ok(locationService.listActiveLocations(pageable));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventLocationDTO.Response>> listEventLocations(@PathVariable Long eventId) {
        return ResponseEntity.ok(locationService.listEventLocations(eventId));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse> activateLocation(@PathVariable Long id) {
        locationService.activateLocation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Location activated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateLocation(@PathVariable Long id) {
        locationService.deactivateLocation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Location deactivated successfully"));
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<EventLocationDTO.Summary>> getLocationSummaries() {
        return ResponseEntity.ok(locationService.getLocationSummaries());
    }

    @GetMapping("/summaries/active")
    public ResponseEntity<List<EventLocationDTO.Summary>> getActiveLocationSummaries() {
        return ResponseEntity.ok(locationService.getActiveLocationSummaries());
    }

    @GetMapping("/event/{eventId}/summaries")
    public ResponseEntity<List<EventLocationDTO.Summary>> getEventLocationSummaries(@PathVariable Long eventId) {
        return ResponseEntity.ok(locationService.getEventLocationSummaries(eventId));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<EventLocationDTO.Statistics> getLocationStatistics(
            @PathVariable Long id,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(locationService.getLocationStatistics(id, startDate, endDate));
    }

    @GetMapping("/{id}/daily-statistics")
    public ResponseEntity<EventLocationDTO.LocationStatistics> getEventLocationStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getEventLocationStatistics(id));
    }

    @PutMapping("/{id}/spin-limits")
    public ResponseEntity<ApiResponse> updateSpinLimits(
            @PathVariable Long id,
            @RequestParam Integer newDailyLimit) {
        locationService.updateSpinLimits(id, newDailyLimit);
        return ResponseEntity.ok(new ApiResponse(true, "Spin limits updated successfully"));
    }

    @GetMapping("/{id}/remaining-spins")
    public ResponseEntity<Long> getRemainingSpinsToday(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getRemainingSpinsToday(id));
    }

    @PostMapping("/{id}/decrement-spins")
    public ResponseEntity<Long> decrementRemainingSpins(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.decrementRemainingSpins(id));
    }
}
