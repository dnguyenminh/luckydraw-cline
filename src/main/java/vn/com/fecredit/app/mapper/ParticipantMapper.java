package vn.com.fecredit.app.mapper;

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
                .customerId(entity.getCustomerId())
                .cardNumber(entity.getCardNumber())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .province(entity.getProvince())
                .dailySpinLimit(entity.getDailySpinLimit())
                .isActive(entity.getIsActive())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}