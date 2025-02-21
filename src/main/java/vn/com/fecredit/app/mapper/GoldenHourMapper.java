package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.model.GoldenHour;

@Component
public class GoldenHourMapper {

    public GoldenHourDTO toDTO(GoldenHour entity) {
        if (entity == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(entity.getStartHour()).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = now.withHour(entity.getEndHour()).withMinute(0).withSecond(0).withNano(0);

        return GoldenHourDTO.builder()
                .id(entity.getId())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .rewardId(entity.getReward() != null ? entity.getReward().getId() : null)
                .name(entity.getName())
                .startHour(entity.getStartHour())
                .endHour(entity.getEndHour())
                .startTime(startTime)
                .endTime(endTime)
                .multiplier(entity.getMultiplier())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public GoldenHour toEntity(GoldenHourDTO dto) {
        if (dto == null) {
            return null;
        }

        return GoldenHour.builder()
                .id(dto.getId())
                .name(dto.getName())
                .startHour(dto.getStartHour())
                .endHour(dto.getEndHour())
                .multiplier(dto.getMultiplier())
                .isActive(dto.getIsActive())
                .build();
    }

    public GoldenHour toEntity(GoldenHourDTO.CreateRequest request) {
        if (request == null) {
            return null;
        }

        Integer startHour = request.getStartHour();
        Integer endHour = request.getEndHour();

        if (startHour == null && request.getStartTime() != null) {
            startHour = request.getStartTime().getHour();
        }
        if (endHour == null && request.getEndTime() != null) {
            endHour = request.getEndTime().getHour();
        }

        return GoldenHour.builder()
                .name(request.getName())
                .startHour(startHour)
                .endHour(endHour)
                .multiplier(request.getMultiplier())
                .isActive(request.getIsActive())
                .build();
    }

    public void updateEntity(GoldenHour entity, GoldenHourDTO.UpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }

        if (request.getStartHour() != null || request.getStartTime() != null) {
            Integer startHour = request.getStartHour();
            if (startHour == null && request.getStartTime() != null) {
                startHour = request.getStartTime().getHour();
            }
            if (startHour != null) {
                entity.setStartHour(startHour);
            }
        }

        if (request.getEndHour() != null || request.getEndTime() != null) {
            Integer endHour = request.getEndHour();
            if (endHour == null && request.getEndTime() != null) {
                endHour = request.getEndTime().getHour();
            }
            if (endHour != null) {
                entity.setEndHour(endHour);
            }
        }

        if (request.getMultiplier() != null) {
            entity.setMultiplier(request.getMultiplier());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
    }
}