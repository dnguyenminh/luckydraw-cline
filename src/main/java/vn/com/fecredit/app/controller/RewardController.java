package vn.com.fecredit.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.service.RewardService;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@Tag(name = "Reward", description = "Reward management APIs")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    @Operation(summary = "Get all rewards")
    public ResponseEntity<List<RewardDTO>> getAllRewards() {
        return ResponseEntity.ok(rewardService.getAllRewards());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reward by ID")
    public ResponseEntity<RewardDTO> getRewardById(@PathVariable Long id) {
        return ResponseEntity.ok(rewardService.getRewardById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new reward")
    public ResponseEntity<RewardDTO> createReward(@Valid @RequestBody RewardDTO.CreateRewardRequest request) {
        return ResponseEntity.ok(rewardService.createReward(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reward")
    public ResponseEntity<RewardDTO> updateReward(
            @PathVariable Long id,
            @Valid @RequestBody RewardDTO.UpdateRewardRequest request) {
        return ResponseEntity.ok(rewardService.updateReward(id, request));
    }

    @PutMapping("/{id}/quantity")
    @Operation(summary = "Update reward quantity")
    public ResponseEntity<RewardDTO> updateQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        return ResponseEntity.ok(rewardService.updateQuantity(id, quantity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reward")
    public ResponseEntity<Void> deleteReward(@PathVariable Long id) {
        rewardService.deleteReward(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/golden-hours")
    @Operation(summary = "Add golden hour to reward")
    public ResponseEntity<RewardDTO> addGoldenHour(
            @PathVariable Long id,
            @Valid @RequestBody GoldenHourDTO.CreateRequest request) {
        return ResponseEntity.ok(rewardService.addGoldenHour(id, request));
    }

    @DeleteMapping("/{rewardId}/golden-hours/{goldenHourId}")
    @Operation(summary = "Remove golden hour from reward")
    public ResponseEntity<RewardDTO> removeGoldenHour(
            @PathVariable Long rewardId,
            @PathVariable Long goldenHourId) {
        return ResponseEntity.ok(rewardService.removeGoldenHour(rewardId, goldenHourId));
    }
}