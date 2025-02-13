package vn.com.fecredit.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.service.SpinService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spins")
@RequiredArgsConstructor
@Slf4j
public class SpinController {

    private final SpinService spinService;

    @PostMapping
    public ResponseEntity<SpinHistory> spin(@Valid @RequestBody SpinRequest request) {
        // Pre-check eligibility before actual spin to fail fast
        spinService.checkSpinEligibility(request);
        
        SpinHistory result = spinService.spin(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/latest/{participantId}")
    public ResponseEntity<SpinHistory> getLatestSpin(@PathVariable Long participantId) {
        SpinHistory history = spinService.getLatestSpinHistory(participantId);
        return history != null ? ResponseEntity.ok(history) : ResponseEntity.notFound().build();
    }

    @GetMapping("/remaining/{eventId}")
    public ResponseEntity<Long> getRemainingSpins(@PathVariable Long eventId) {
        long remainingSpins = spinService.getRemainingSpins(eventId);
        return ResponseEntity.ok(remainingSpins);
    }
}