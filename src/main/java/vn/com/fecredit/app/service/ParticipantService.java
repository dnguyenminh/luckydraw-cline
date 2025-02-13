package vn.com.fecredit.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.dto.participant.UpdateParticipantRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.ParticipantMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final ParticipantMapper participantMapper;

    @Transactional(readOnly = true)
    public Page<ParticipantDTO> getAllParticipants(String search, Pageable pageable) {
        // The search is case-insensitive
        Specification<Participant> specification = (root, query, builder) -> {
            if (search == null || search.trim().isEmpty()) {
                return null;
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("customerId")), searchPattern),
                builder.like(builder.lower(root.get("fullName")), searchPattern),
                builder.like(builder.lower(root.get("email")), searchPattern),
                builder.like(builder.lower(root.get("phoneNumber")), searchPattern)
            );
        };
        
        return participantRepository.findAll(specification, pageable)
                .map(participantMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ParticipantDTO getParticipant(Long id) {
        return participantMapper.toDTO(findById(id));
    }

    @Transactional
    public ParticipantDTO createParticipant(CreateParticipantRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Participant participant = Participant.builder()
                .name(request.getFullName()) // Set the name field to fullName
                .customerId(request.getCustomerId())
                .cardNumber(request.getCardNumber())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .province(request.getProvince())
                .dailySpinLimit(request.getDailySpinLimit())
                .spinsRemaining(request.getDailySpinLimit()) // Initialize spinsRemaining
                .event(event)
                .employeeId(request.getCustomerId()) // Assuming employeeId should be set to customerId
                .isActive(true) // Initialize isActive
                .build();

        return participantMapper.toDTO(participantRepository.save(participant));
    }

    @Transactional
    public ParticipantDTO updateParticipant(Long id, UpdateParticipantRequest request) {
        Participant participant = findById(id);
        
        participant.setFullName(request.getFullName());
        participant.setEmail(request.getEmail());
        participant.setPhoneNumber(request.getPhoneNumber());
        participant.setProvince(request.getProvince());
        participant.setDailySpinLimit(request.getDailySpinLimit());
        participant.setIsActive(request.getIsActive());

        return participantMapper.toDTO(participantRepository.save(participant));
    }

    @Transactional
    public void deleteParticipant(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Participant not found");
        }
        participantRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Participant findByCustomerId(String customerId) {
        return participantRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
    }

    private Participant findById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
    }
}