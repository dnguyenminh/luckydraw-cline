package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.RewardService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final RewardMapper rewardMapper;

    @Override
    @Transactional(readOnly = true)
    public Reward getById(Long id) {
        return rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + id));
    }

    @Override
    @Transactional
    public RewardDTO.Response create(RewardDTO.CreateRequest request) {
        Reward reward = rewardMapper.toEntity(request);
        reward = rewardRepository.save(reward);
        return rewardMapper.toResponse(reward);
    }

    @Override
    @Transactional
    public RewardDTO.Response update(Long id, RewardDTO.UpdateRequest request) {
        Reward reward = getById(id);
        rewardMapper.updateEntity(reward, request);
        reward = rewardRepository.save(reward);
        return rewardMapper.toResponse(reward);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Reward reward = getById(id);
        reward.setStatus(EntityStatus.DELETED);
        rewardRepository.save(reward);
    }

    @Override
    @Transactional(readOnly = true)
    public RewardDTO.Response findById(Long id) {
        return rewardMapper.toResponse(getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardDTO.Response> findAllByEventId(Long eventId) {
        return rewardRepository.findAllByEventIdAndStatus(eventId, EntityStatus.ACTIVE.name())
                .stream()
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
        return rewardRepository.findAllAvailableByEventId(eventId, EntityStatus.ACTIVE.name())
                .stream()
                .map(rewardMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateRemainingQuantity(Long id, Integer quantity) {
        rewardRepository.updateRemainingQuantity(id, quantity);
    }

    @Override
    @Transactional
    public void decrementRemainingQuantityById(Long id) {
        rewardRepository.decrementRemainingQuantityById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableQuantity(Long id) {
        return rewardRepository.hasAvailableQuantity(id);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateReward(Long eventId, Long rewardId) {
        Reward reward = getById(rewardId);
        if (!eventId.equals(reward.getEvent().getId())) {
            throw new IllegalArgumentException("Reward does not belong to event");
        }
        if (!EntityStatus.ACTIVE.name().equals(reward.getStatus())) {
            throw new IllegalArgumentException("Reward is not active");
        }
        if (!hasAvailableQuantity(rewardId)) {
            throw new IllegalArgumentException("Reward is out of stock");
        }
    }
}
