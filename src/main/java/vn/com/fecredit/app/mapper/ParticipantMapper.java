package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.model.Participant;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    public ParticipantDTO toDTO(Participant entity) {
        if (entity == null) {
            return null;
        }

        Event event = entity.getEvent();
        EventLocation eventLocation = entity.getEventLocation();

        ParticipantDTO.ParticipantDTOBuilder builder = ParticipantDTO.builder()
                .id(entity.getId())
                .customerId(getOrEmpty(entity.getCustomerId()))
                .employeeId(getOrEmpty(entity.getEmployeeId()))
                .cardNumber(getOrEmpty(entity.getCardNumber()))
                .fullName(getOrEmpty(entity.getFullName()))
                .email(getOrEmpty(entity.getEmail()))
                .phoneNumber(getOrEmpty(entity.getPhoneNumber()))
                .name(getOrEmpty(entity.getName()))
                .province(getOrEmpty(entity.getProvince()))
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .isActive(entity.getIsActive())
                .spinsRemaining(entity.getSpinsRemaining())
                .dailySpinLimit(entity.getDailySpinLimit())
                .isEligibleForSpin(entity.getIsEligibleForSpin())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Add event information if available
        if (event != null) {
            builder.eventId(event.getId())
                   .eventName(event.getName())
                   .eventStartDate(event.getStartDate())
                   .eventEndDate(event.getEndDate());
        }

        // Add event location information if available
        if (eventLocation != null) {
            builder.eventLocationId(eventLocation.getId())
                   .eventLocationName(getOrEmpty(eventLocation.getName()))
                   .location(getOrEmpty(eventLocation.getLocation()))
                   .locationTotalSpins(eventLocation.getTotalSpins())
                   .locationRemainingSpins(eventLocation.getRemainingSpins());
        }

        return builder.build();
    }

    public void updateEntityFromDTO(ParticipantDTO dto, Participant entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setCustomerId(dto.getCustomerId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setCardNumber(dto.getCardNumber());
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setName(dto.getName());
        entity.setProvince(dto.getProvince());
        entity.setIsActive(dto.isActive());
        entity.setDailySpinLimit(dto.getDailySpinLimit());
        entity.setSpinsRemaining(dto.getSpinsRemaining());
        entity.setIsEligibleForSpin(dto.isEligibleForSpin());
        
        if (dto.getUpdatedAt() != null) {
            entity.setUpdatedAt(dto.getUpdatedAt());
        } else {
            entity.setUpdatedAt(LocalDateTime.now());
        }
    }

    private String getOrEmpty(String value) {
        return value != null ? value : "";
    }
}