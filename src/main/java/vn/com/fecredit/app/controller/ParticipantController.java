package vn.com.fecredit.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.dto.participant.ImportParticipantResponse;
import vn.com.fecredit.app.dto.participant.UpdateParticipantRequest;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.service.FileImportService;
import vn.com.fecredit.app.service.ParticipantService;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final FileImportService fileImportService;

    @GetMapping
    public ResponseEntity<Page<ParticipantDTO>> getAllParticipants(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(participantService.getAllParticipants(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDTO> getParticipant(@PathVariable Long id) {
        return ResponseEntity.ok(participantService.getParticipant(id));
    }

    @PostMapping
    public ResponseEntity<ParticipantDTO> createParticipant(
            @Valid @RequestBody CreateParticipantRequest request) {
        return ResponseEntity.ok(participantService.createParticipant(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipantDTO> updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParticipantRequest request) {
        return ResponseEntity.ok(participantService.updateParticipant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportParticipantResponse> importParticipants(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long eventId) {
        return ResponseEntity.ok(fileImportService.importParticipants(file, eventId));
    }

    @GetMapping("/check/{customerId}")
    public ResponseEntity<ParticipantDTO> checkParticipant(@PathVariable String customerId) {
        Participant participant = participantService.findByCustomerId(customerId);
        return ResponseEntity.ok(ParticipantDTO.builder()
                .id(participant.getId())
                .customerId(participant.getCustomerId())
                .fullName(participant.getFullName())
                .email(participant.getEmail())
                .phoneNumber(participant.getPhoneNumber())
                .province(participant.getProvince())
                .isActive(participant.getIsActive())
                .dailySpinLimit(participant.getDailySpinLimit())
                .build());
    }
}