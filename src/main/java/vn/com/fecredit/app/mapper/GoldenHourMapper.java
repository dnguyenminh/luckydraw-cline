package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class GoldenHourMapper {

    public GoldenHourDTO toDTO(GoldenHour goldenHour) {
        if (goldenHour == null) {
            return null;
        }

        return GoldenHourDTO.builder()
                .id(goldenHour.getId())
                .name(goldenHour.getName())
                .startTime(LocalDateTime.of(LocalDateTime.now().toLocalDate(), goldenHour.getStartTime()))
                .endTime(LocalDateTime.of(LocalDateTime.now().toLocalDate(), goldenHour.getEndTime()))
                .multiplier(goldenHour.getMultiplier())
                .isActive(goldenHour.getIsActive())
                .rewardId(goldenHour.getReward() != null ? goldenHour.getReward().getId() : null)
                .build();
    }

    public GoldenHour toEntity(GoldenHourDTO dto) {
        if (dto == null) {
            return null;
        }

        return GoldenHour.builder()
                .id(dto.getId())
                .name(dto.getName())
                .startTime(dto.getStartTime().toLocalTime())
                .endTime(dto.getEndTime().toLocalTime())
                .multiplier(dto.getMultiplier())
                .isActive(dto.getIsActive())
                .build();
    }

    public GoldenHour createEntity(GoldenHourDTO.CreateRequest request) {
        if (request == null) {
            return null;
        }

        return GoldenHour.builder()
                .name(request.getName())
                .startTime(request.getStartTime().toLocalTime())
                .endTime(request.getEndTime().toLocalTime())
                .multiplier(request.getMultiplier())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public void updateEntityFromDTO(GoldenHourDTO.UpdateRequest request, GoldenHour entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getStartTime() != null) {
            entity.setStartTime(request.getStartTime().toLocalTime());
        }
        if (request.getEndTime() != null) {
            entity.setEndTime(request.getEndTime().toLocalTime());
        }
        if (request.getMultiplier() != null) {
            entity.setMultiplier(request.getMultiplier());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }

    public void setReward(GoldenHour goldenHour, Reward reward) {
        goldenHour.setReward(reward);
    }
}