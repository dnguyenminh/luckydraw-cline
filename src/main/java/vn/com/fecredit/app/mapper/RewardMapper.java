package vn.com.fecredit.app.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.model.Reward;

@Component
public class RewardMapper {
    private final GoldenHourMapper goldenHourMapper;

    public RewardMapper(GoldenHourMapper goldenHourMapper) {
        this.goldenHourMapper = goldenHourMapper;
    }

    public RewardDTO toDTO(Reward reward) {
        if (reward == null) {
            return null;
        }

        return RewardDTO.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .quantity(reward.getQuantity())
                .remainingQuantity(reward.getRemainingQuantity())
                .maxQuantityInPeriod(reward.getMaxQuantityInPeriod())
                .probability(reward.getProbability())
                .applicableProvinces(reward.getApplicableProvinces())
                .startDate(reward.getStartDate())
                .endDate(reward.getEndDate())
                .isActive(reward.getIsActive())
                .eventId(reward.getEvent() != null ? reward.getEvent().getId() : null)
                .goldenHours(reward.getGoldenHours() != null ? reward.getGoldenHours().stream()
                        .map(goldenHourMapper::toDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public Reward toEntity(RewardDTO.CreateRewardRequest createRequest) {
        if (createRequest == null) {
            return null;
        }

        return Reward.builder()
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .quantity(createRequest.getQuantity())
                .remainingQuantity(createRequest.getQuantity())
                .maxQuantityInPeriod(createRequest.getMaxQuantityInPeriod())
                .probability(createRequest.getProbability())
                .applicableProvinces(createRequest.getApplicableProvinces())
                .startDate(createRequest.getStartDate())
                .endDate(createRequest.getEndDate())
                .isActive(createRequest.getIsActive())
                .build();
    }

    public void updateRewardFromRequest(RewardDTO.UpdateRewardRequest updateRequest, Reward reward) {
        if (updateRequest == null || reward == null) {
            return;
        }

        if (updateRequest.getName() != null) {
            reward.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            reward.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getQuantity() != null) {
            reward.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getMaxQuantityInPeriod() != null) {
            reward.setMaxQuantityInPeriod(updateRequest.getMaxQuantityInPeriod());
        }
        if (updateRequest.getProbability() != null) {
            reward.setProbability(updateRequest.getProbability());
        }
        if (updateRequest.getApplicableProvinces() != null) {
            reward.setApplicableProvinces(updateRequest.getApplicableProvinces());
        }
        if (updateRequest.getStartDate() != null) {
            reward.setStartDate(updateRequest.getStartDate());
        }
        if (updateRequest.getEndDate() != null) {
            reward.setEndDate(updateRequest.getEndDate());
        }
        if (updateRequest.getIsActive() != null) {
            reward.setIsActive(updateRequest.getIsActive());
        }
    }
}