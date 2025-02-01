package vn.com.fecredit.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @InjectMocks
    private RewardService rewardService;

    @Test
    void determineWinningReward_WhenNotWinning_ReturnsLoss() {
        // Giả lập phần thưởng có xác suất 0%
        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(0.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .build();

        when(rewardRepository.findAll()).thenReturn(List.of(reward));

        // Gọi hàm
        SpinResult result = rewardService.determineWinningReward("user123");

        // Kiểm tra kết quả
        assertFalse(result.isWinner());
        verify(spinHistoryRepository).save(argThat(history ->
                history.getCustomerId().equals("user123") &&
                        !history.isWinner()
        ));
    }

    @Test
    void determineWinningReward_WhenWinning_ReturnsWin() {
        // Giả lập phần thưởng có xác suất 100%
        Reward reward = Reward.builder()
                .name("Voucher 50K")
                .probability(100.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .build();

        when(rewardRepository.findAll()).thenReturn(List.of(reward));
        when(spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(0); // Giả lập chưa phát quà nào trong ngày

        // Gọi hàm
        SpinResult result = rewardService.determineWinningReward("user123");

        // Kiểm tra kết quả
        assertTrue(result.isWinner());
        assertEquals("Voucher 50K", result.getRewardName());
        verify(rewardRepository).save(argThat(r -> r.getTotalQuantity() == 9));
        verify(spinHistoryRepository).save(argThat(history ->
                history.isWinner() &&
                        history.getRewardName().equals("Voucher 50K")
        ));
    }

    @Test
    void calculateAdjustedProbability_InGoldenHour_AddsBonus() {
        Reward reward = Reward.builder()
                .probability(10.0)
                .goldenHourProbability(20.0)
                .build();

        // Giả lập thời gian trong giờ vàng (12:00 - 14:00)
        LocalDateTime goldenHourTime = LocalDateTime.of(2024, 4, 30, 12, 30);
        double probability = rewardService.calculateAdjustedProbability(reward, goldenHourTime);

        assertEquals(30.0, probability); // 10% + 20% = 30%
    }

    @Test
    void determineWinningReward_WhenDailyLimitExceeded_ReturnsLoss() {
        Reward reward = Reward.builder()
                .name("Voucher 100K")
                .probability(100.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .build();

        when(rewardRepository.findAll()).thenReturn(List.of(reward));
        when(spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(5); // Đã phát 5 quà (đạt giới hạn)

        // Gọi hàm
        SpinResult result = rewardService.determineWinningReward("user123");

        // Kiểm tra kết quả
        assertFalse(result.isWinner());
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void determineWinningReward_WhenTotalQuantityZero_ReturnsLoss() {
        Reward reward = Reward.builder()
                .name("Xe Máy Viaro")
                .probability(100.0)
                .totalQuantity(0) // Đã hết quà
                .maxQuantityPerPeriod(1)
                .build();

        when(rewardRepository.findAll()).thenReturn(List.of(reward));

        // Gọi hàm
        SpinResult result = rewardService.determineWinningReward("user123");

        // Kiểm tra kết quả
        assertFalse(result.isWinner());
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void saveSpinHistory_Always_SavesCorrectData() {
        LocalDateTime now = LocalDateTime.now();
        rewardService.saveSpinHistory("user123", "Voucher 200K", true, now);

        verify(spinHistoryRepository).save(argThat(history ->
                history.getCustomerId().equals("user123") &&
                        history.getRewardName().equals("Voucher 200K") &&
                        history.isWinner() &&
                        history.getSpinDateTime().equals(now)
        ));
    }
}
