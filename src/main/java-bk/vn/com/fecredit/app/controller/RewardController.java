package vn.com.fecredit.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.service.RewardService;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RewardDTO.Response createReward(@Valid @RequestBody RewardDTO.CreateRequest request) {
        return rewardService.create(request);
    }

    @PutMapping("/{id}")
    public RewardDTO.Response updateReward(
            @PathVariable Long id,
            @Valid @RequestBody RewardDTO.UpdateRequest request
    ) {
        return rewardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReward(@PathVariable Long id) {
        rewardService.delete(id);
    }

    @GetMapping("/{id}")
    public RewardDTO.Response getReward(@PathVariable Long id) {
        return rewardService.findById(id);
    }

    @GetMapping
    public Page<RewardDTO.Response> getAllRewards(Pageable pageable) {
        return rewardService.findAll(pageable);
    }

    @GetMapping("/event/{eventId}")
    public List<RewardDTO.Response> getRewardsByEvent(@PathVariable Long eventId) {
        return rewardService.findAllByEventId(eventId);
    }

    @GetMapping("/event/{eventId}/active")
    public List<RewardDTO.Summary> getActiveRewardsByEvent(@PathVariable Long eventId) {
        return rewardService.findAllActiveByEventId(eventId);
    }

    @PutMapping("/{id}/quantity")
    @ResponseStatus(HttpStatus.OK)
    public void updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity
    ) {
        rewardService.updateRemainingQuantity(id, quantity);
    }
}
