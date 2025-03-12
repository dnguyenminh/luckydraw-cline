package vn.com.fecredit.app.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.SpinHistoryService;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    private final EventRepository eventRepository;
    private final SpinHistoryMapper spinHistoryMapper;
    private final SpinHistoryService spinHistoryService;

    public Participant toEntity(ParticipantDTO.CreateRequest request) {
        if (request == null) {
            return null;
        }

        Participant participant = new Participant();
        participant.setCustomerId(request.getCustomerId());
        participant.setCardNumber(request.getCardNumber());
        participant.setPhoneNumber(request.getPhoneNumber());

        if (request.getEventId() != null) {
            eventRepository.findById(request.getEventId()).ifPresent(event -> {
                participant.setEvent(event);
                participant.setCurrentEvent(event);
                participant.setDailySpinLimit(event.getDailySpinLimit());
                participant.setSpinsRemaining(event.getDailySpinLimit());
            });
        }

        return participant;
    }

    public void updateEntity(Participant participant, ParticipantDTO.UpdateRequest request) {
        if (participant == null || request == null) {
            return;
        }

        if (request.getCustomerId() != null) {
            participant.setCustomerId(request.getCustomerId());
        }
        if (request.getCardNumber() != null) {
            participant.setCardNumber(request.getCardNumber());
        }
        if (request.getPhoneNumber() != null) {
            participant.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEventId() != null && 
            (participant.getCurrentEvent() == null || !participant.getCurrentEvent().getId().equals(request.getEventId()))) {
            eventRepository.findById(request.getEventId()).ifPresent(event -> {
                participant.setCurrentEvent(event);
                participant.setDailySpinLimit(event.getDailySpinLimit());
                participant.setSpinsRemaining(event.getDailySpinLimit());
            });
        }

        if (request.getIsEligibleForSpin() != null) {
            participant.setIsEligibleForSpin(request.getIsEligibleForSpin());
        }
    }

    public ParticipantDTO.Response toResponse(Participant participant) {
        if (participant == null) {
            return null;
        }

        Set<SpinHistoryDTO.Summary> spinHistories = participant.getSpinHistories() != null ?
            spinHistoryMapper.toSummarySet(participant.getSpinHistories()) :
            Collections.emptySet();

        Event event = participant.getEvent();
        Event currentEvent = participant.getCurrentEvent();

        return ParticipantDTO.Response.builder()
                .id(participant.getId())
                .customerId(participant.getCustomerId())
                .cardNumber(participant.getCardNumber())
                .phoneNumber(participant.getPhoneNumber())
                .eventId(event != null ? event.getId() : null)
                .eventName(event != null ? event.getName() : null)
                .currentEventId(currentEvent != null ? currentEvent.getId() : null)
                .currentEventName(currentEvent != null ? currentEvent.getName() : null)
                .spinsRemaining(participant.getSpinsRemaining())
                .dailySpinLimit(participant.getDailySpinLimit())
                .lastSpinTime(participant.getLastSpinTime())
                .lastSyncTime(participant.getLastSyncTime())
                .isEligibleForSpin(participant.getIsEligibleForSpin())
                .entityStatus(participant.getEntityStatus())
                .spinHistories(spinHistories)
                .createdAt(participant.getCreatedAt())
                .createdBy(participant.getCreatedBy())
                .updatedAt(participant.getUpdatedAt())
                .updatedBy(participant.getUpdatedBy())
                .build();
    }

    public ParticipantDTO.Summary toSummary(Participant participant) {
        if (participant == null) {
            return null;
        }

        Event event = participant.getEvent();
        
        return ParticipantDTO.Summary.builder()
                .id(participant.getId())
                .customerId(participant.getCustomerId())
                .cardNumber(participant.getCardNumber())
                .phoneNumber(participant.getPhoneNumber())
                .eventId(event != null ? event.getId() : null)
                .eventName(event != null ? event.getName() : null)
                .spinsRemaining(participant.getSpinsRemaining())
                .dailySpinLimit(participant.getDailySpinLimit())
                .isEligibleForSpin(participant.getIsEligibleForSpin())
                .entityStatus(participant.getEntityStatus())
                .build();
    }

    public Set<ParticipantDTO.Summary> toSummarySet(Set<Participant> participants) {
        if (participants == null) {
            return Collections.emptySet();
        }
        return participants.stream()
                .map(this::toSummary)
                .collect(Collectors.toSet());
    }
}
