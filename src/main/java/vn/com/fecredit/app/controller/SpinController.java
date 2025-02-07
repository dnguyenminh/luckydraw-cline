package vn.com.fecredit.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.dto.spin.SpinCheckResponse;
import vn.com.fecredit.app.dto.spin.SpinRequest;
import vn.com.fecredit.app.mapper.SpinHistoryMapper;
import vn.com.fecredit.app.model.LuckyDrawResult;
import vn.com.fecredit.app.service.SpinService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spins")
public class SpinController {
    private final SpinService spinService;
    private final SpinHistoryMapper spinHistoryMapper;

    @GetMapping("/{participantId}/history")
    public ResponseEntity<SpinHistoryDTO> getParticipantSpinHistory(@PathVariable Long participantId) {
        return ResponseEntity.ok(spinHistoryMapper.toDTO(spinService.getLatestSpinHistory(participantId)));
    }

    @PostMapping("/check")
    public ResponseEntity<SpinCheckResponse> checkSpinEligibility(@Valid @RequestBody SpinRequest request) {
        return ResponseEntity.ok(spinService.checkSpinEligibility(request));
    }

    @PostMapping
    public ResponseEntity<SpinHistoryDTO> spin(@Valid @RequestBody SpinRequest request) {
        LuckyDrawResult result = spinService.spin(request);
        return ResponseEntity.ok(spinHistoryMapper.toDTO(result.getSpinHistory()));
    }
}