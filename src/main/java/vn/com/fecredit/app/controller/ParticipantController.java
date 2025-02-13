package vn.com.fecredit.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ParticipantDTO>> getAllParticipants(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(participantService.getAllParticipants(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParticipantDTO> getParticipant(@PathVariable Long id) {
        return ResponseEntity.ok(participantService.getParticipant(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParticipantDTO> createParticipant(
            @Valid @RequestBody CreateParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participantService.createParticipant(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParticipantDTO> updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParticipantRequest request) {
        return ResponseEntity.ok(participantService.updateParticipant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportParticipantResponse> importParticipants(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long eventId) {
        return ResponseEntity.ok(fileImportService.importParticipants(file, eventId));
    }

    @GetMapping("/check/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
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
                .dailySpinLimit(participant.getDailySpinLimit().longValue())
                .spinsRemaining(participant.getSpinsRemaining())
                .eventId(participant.getEvent() != null ? participant.getEvent().getId() : null)
                .createdAt(participant.getCreatedAt())
                .updatedAt(participant.getUpdatedAt())
                .build());
    }
}