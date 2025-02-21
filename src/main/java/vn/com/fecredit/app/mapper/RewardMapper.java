package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;

@Component
@RequiredArgsConstructor
public class RewardMapper {

    private final GoldenHourMapper goldenHourMapper;
    private final EventRepository eventRepository;

    public RewardDTO toDTO(Reward reward) {
        if (reward == null) {
            return null;
        }

        RewardDTO dto = RewardDTO.builder()
            .id(reward.getId())
            .eventId(reward.getEvent() != null ? reward.getEvent().getId() : null)
            .eventName(reward.getEvent() != null ? reward.getEvent().getName() : null)
            .eventRegionId(reward.getEventRegionId())
            .code(reward.getCode())
            .name(reward.getName())
            .description(reward.getDescription())
            .applicableProvinces(reward.getApplicableProvincesAsString())
            .quantity(reward.getQuantity())
            .remainingQuantity(reward.getRemainingQuantity())
            .probability(reward.getProbability())
            .maxQuantityInPeriod(reward.getMaxQuantityInPeriod())
            .startDate(reward.getStartDate())
            .endDate(reward.getEndDate())
            .isActive(reward.isActive())
            .version(reward.getVersion())
            .createdAt(reward.getCreatedAt())
            .updatedAt(reward.getUpdatedAt())
            .build();

        reward.getGoldenHours().forEach(gh -> 
            dto.getGoldenHours().add(goldenHourMapper.toDTO(gh)));

        return dto;
    }

    public Reward toEntity(RewardDTO.CreateRewardRequest request) {
        if (request == null) {
            return null;
        }

        Event event = request.getEventId() != null ? 
            eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + request.getEventId()))
            : null;

        Reward reward = Reward.builder()
            .event(event)
            .eventRegionId(request.getEventRegionId())
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .quantity(request.getQuantity())
            .remainingQuantity(request.getQuantity())
            .probability(request.getProbability())
            .maxQuantityInPeriod(request.getMaxQuantityInPeriod())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isActive(request.isActive())
            .build();

        reward.setApplicableProvincesFromString(request.getApplicableProvinces());

        request.getGoldenHours().forEach(gh -> 
            reward.addGoldenHour(goldenHourMapper.toEntity(gh)));

        return reward;
    }

    public void updateRewardFromRequest(RewardDTO.UpdateRewardRequest request, Reward reward) {
        if (request == null || reward == null) {
            return;
        }

        if (request.getCode() != null) reward.setCode(request.getCode());
        if (request.getName() != null) reward.setName(request.getName());
        if (request.getDescription() != null) reward.setDescription(request.getDescription());
        if (request.getQuantity() != null) {
            reward.setQuantity(request.getQuantity());
            if (request.getQuantity() < reward.getRemainingQuantity()) {
                reward.setRemainingQuantity(request.getQuantity());
            }
        }
        if (request.getProbability() != null) reward.setProbability(request.getProbability());
        if (request.getMaxQuantityInPeriod() != null) reward.setMaxQuantityInPeriod(request.getMaxQuantityInPeriod());
        if (request.getStartDate() != null) reward.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) reward.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) reward.setActive(request.isActive());
        if (request.getApplicableProvinces() != null) {
            reward.setApplicableProvincesFromString(request.getApplicableProvinces());
        }

        // Update golden hours
        reward.getGoldenHours().clear();
        request.getGoldenHours().forEach(gh -> 
            reward.addGoldenHour(goldenHourMapper.toEntity(gh)));
    }
}