package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.service.EventService;
import vn.com.fecredit.app.service.GoldenHourService;
import vn.com.fecredit.app.service.RewardService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class GoldenHourServiceImpl implements GoldenHourService {
    
    private final GoldenHourRepository goldenHourRepository;
    private final GoldenHourMapper mapper;
    private final EventService eventService;
    private final RewardService rewardService;

    @Override
    @Transactional
    public GoldenHourDTO.Response create(GoldenHourDTO.CreateRequest request) {
        Event event = eventService.getById(request.getEventId());
        Reward reward = null;
        if (request.getRewardId() != null) {
            reward = rewardService.getById(request.getRewardId());
        }

        GoldenHour entity = mapper.toEntity(request);
        entity.setEvent(event);
        entity.setReward(reward);
        entity.setStatus(EntityStatus.ACTIVE);
        
        GoldenHour saved = goldenHourRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GoldenHourDTO.Response update(Long id, GoldenHourDTO.UpdateRequest request) {
        GoldenHour entity = goldenHourRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Golden Hour not found with id: " + id));

        if (request.getRewardId() != null) {
            Reward reward = rewardService.getById(request.getRewardId());
            entity.setReward(reward);
        }

        mapper.updateEntity(entity, request);
        GoldenHour saved = goldenHourRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GoldenHour entity = goldenHourRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Golden Hour not found with id: " + id));
        entity.setStatus(EntityStatus.INACTIVE);
        goldenHourRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public GoldenHourDTO.Response getById(Long id) {
        return goldenHourRepository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Golden Hour not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHourDTO.Response> getAllByEventId(Long eventId) {
        List<GoldenHour> goldenHours = goldenHourRepository.findAllByEventIdAndStatus(eventId, EntityStatus.ACTIVE.name());
        return goldenHours.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoldenHourDTO.Response getCurrentGoldenHour(Long eventId) {
        LocalTime currentTime = LocalTime.now();
        Optional<GoldenHour> currentGoldenHour = goldenHourRepository.findAllByEventIdAndStatus(eventId, EntityStatus.ACTIVE.name())
            .stream()
            .filter(gh -> gh.isTimeInRange(currentTime) && gh.isDayActive(LocalDateTime.now().getDayOfWeek()))
            .findFirst();
            
        return currentGoldenHour.map(mapper::toResponse)
            .orElse(null);
    }
}
