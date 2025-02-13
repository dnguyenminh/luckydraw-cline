package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.model.Participant;

@Component
public class ParticipantMapper {

    public ParticipantDTO toDTO(Participant entity) {
        if (entity == null) {
            return null;
        }

        return ParticipantDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId() != null ? entity.getCustomerId() : "")
                .cardNumber(entity.getCardNumber() != null ? entity.getCardNumber() : "")
                .email(entity.getEmail() != null ? entity.getEmail() : "")
                .fullName(entity.getFullName() != null ? entity.getFullName() : "")
                .phoneNumber(entity.getPhoneNumber() != null ? entity.getPhoneNumber() : "")
                .province(entity.getProvince() != null ? entity.getProvince() : "")
                .dailySpinLimit(entity.getDailySpinLimit())
                .isActive(entity.getIsActive() != null ? entity.getIsActive() : true)
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now())
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}