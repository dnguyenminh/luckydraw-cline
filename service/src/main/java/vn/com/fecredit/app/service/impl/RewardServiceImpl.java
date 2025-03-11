package vn.com.fecredit.app.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.RewardService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final EventLocationRepository eventLocationRepository;
    private final EventRepository eventRepository;
    private final RewardMapper rewardMapper;

    public RewardServiceImpl(RewardRepository rewardRepository,
                            EventLocationRepository eventLocationRepository,
                            EventRepository eventRepository,
                            RewardMapper rewardMapper) {
        this.rewardRepository = rewardRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.eventRepository = eventRepository;
        this.rewardMapper = rewardMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Reward getById(Long id) {
        return getReward(id);
    }

    @Override
    public RewardDTO.Response create(RewardDTO.CreateRequest request) {
        EventLocation location = getEventLocation(request.getEventLocationId());
        
        Reward reward = rewardMapper.toEntity(request);
        reward.setEventLocation(location);
        reward.setStatus(EntityStatus.ACTIVE.getValue());
        
        reward = rewardRepository.save(reward);
        return rewardMapper.toResponse(reward);
    }

    @Override
    public RewardDTO.Response update(Long id, RewardDTO.UpdateRequest request) {
        Reward reward = getReward(id);
        
        if (request.getActive() != null) {
            reward.setStatus(request.getActive() ? 
                EntityStatus.ACTIVE.getValue() : 
                EntityStatus.INACTIVE.getValue());
        }
        
        rewardMapper.updateEntity(reward, request);
        reward = rewardRepository.save(reward);
        return rewardMapper.toResponse(reward);
    }

    @Override
    public void delete(Long id) {
        Reward reward = getReward(id);
        reward.setStatus(EntityStatus.INACTIVE.getValue());
        rewardRepository.save(reward);
    }

    @Override
    @Transactional(readOnly = true)
    public RewardDTO.Response findById(Long id) {
        return rewardMapper.toResponse(getReward(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardDTO.Response> findAllByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event", "id", eventId));
        
        return event.getEventLocations().stream()
            .map(location -> rewardRepository.findByLocationAndStatus(
                location, 
                EntityStatus.ACTIVE.getValue()
            ))
            .flatMap(List::stream)
            .map(rewardMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RewardDTO.Response> findAll(Pageable pageable) {
        return rewardRepository.findAll(pageable)
            .map(rewardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardDTO.Summary> findAllActiveByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event", "id", eventId));
        
        return event.getEventLocations().stream()
            .map(location -> rewardRepository.findByLocationAndStatus(
                location, 
                EntityStatus.ACTIVE.getValue()
            ))
            .flatMap(List::stream)
            .map(rewardMapper::toSummary)
            .collect(Collectors.toList());
    }

    @Override
    public void updateRemainingQuantity(Long id, Integer quantity) {
        getReward(id); // Validate existence
        rewardRepository.updateRemainingQuantity(id, quantity);
    }

    @Override
    public void decrementRemainingQuantityById(Long id) {
        rewardRepository.decrementRemainingQuantityById(id);
    }

    @Override
    public boolean hasAvailableQuantity(Long id) {
        return rewardRepository.hasAvailableQuantity(id);
    }

    @Override
    public void validateReward(Long eventId, Long rewardId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event", "id", eventId));
            
        Reward reward = getReward(rewardId);
        
        if (!event.getEventLocations().contains(reward.getEventLocation())) {
            throw new InvalidOperationException("Reward does not belong to event");
        }
        
        if (!reward.isActive()) {
            throw new InvalidOperationException("Reward is not active");
        }
    }

    private Reward getReward(Long id) {
        return rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reward", "id", id));
    }

    private EventLocation getEventLocation(Long id) {
        return eventLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EventLocation", "id", id));
    }
}
