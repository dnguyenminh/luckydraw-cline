package vn.com.fecredit.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.model.GoldenHourConfig;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinResult;
import vn.com.fecredit.app.reposistory.RewardRepository;
import vn.com.fecredit.app.reposistory.SpinHistoryRepository;
import vn.com.fecredit.app.service.GoldenHourConfigService;
import vn.com.fecredit.app.service.RewardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private SpinHistoryRepository spinHistoryRepository;
    @Mock
    private GoldenHourConfigService goldenHourConfigService;
    @InjectMocks
    private RewardService rewardService;

    @Test
    void determineWinningReward_noWin() {

        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(0.0) // set probability to 0%
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .limitFromDate(LocalDate.now())
                .limitToDate(LocalDate.now().plusDays(1))
                .build();
        when(rewardRepository.findValidRewards(any())).thenReturn(List.of(reward));
        SpinResult result = rewardService.determineWinningReward("user123");
        assertFalse(result.isWinner());
        assertNull(result.getRewardName());
        verify(spinHistoryRepository, times(1)).save(any());
    }

    @Test
    void determineWinningReward_win() {
        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(100.0)
                .goldenHourProbability(0.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .limitFromDate(LocalDate.now())
                .limitToDate(LocalDate.now().plusDays(1))
                .build();

        when(rewardRepository.findValidRewards(any())).thenReturn(List.of(reward));
        when(goldenHourConfigService.getGoldenHourConfigs(any())).thenReturn(List.of());
//        when(spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
//                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
//        )).thenReturn(0); // Giả lập chưa phát quà nào

        SpinResult result = rewardService.determineWinningReward("user123");
        assertTrue(result.isWinner());
        assertEquals("Voucher 20K", result.getRewardName());

        verify(spinHistoryRepository, times(1)).save(any());
        verify(rewardRepository, times(1)).save(any());
    }


    @Test
    void calculateAdjustedProbability_InGoldenHour_AddsBonus() {
        Reward reward = Reward.builder()
                .probability(10.0)
                .goldenHourProbability(20.0)
                .name("Test Reward")
                .build();

        GoldenHourConfig goldenHourConfig = GoldenHourConfig.builder()
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .rewardName("Test Reward")
                .build();

        when(goldenHourConfigService.getGoldenHourConfigs("Test Reward")).thenReturn(List.of(goldenHourConfig));

        double adjustedProbability = rewardService.calculateAdjustedProbability(reward, LocalDateTime.now());

        assertEquals(30.0, adjustedProbability);

    }

    @Test
    void isRewardAvailable_quantityZero_returnsFalse() {

        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(100.0)
                .totalQuantity(0)
                .maxQuantityPerPeriod(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .limitFromDate(LocalDate.now())
                .limitToDate(LocalDate.now().plusDays(1))
                .build();

        boolean result = rewardService.isRewardAvailable(reward, LocalDateTime.now());
        assertFalse(result);
    }

    @Test
    void isRewardAvailable_maxQuantityReached_returnsFalse() {


        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(100.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .limitFromDate(LocalDate.now())
                .limitToDate(LocalDate.now().plusDays(1))
                .build();
        when(spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(5);
        boolean result = rewardService.isRewardAvailable(reward, LocalDateTime.now());
        assertFalse(result);
    }


    @Test
    void isRewardAvailable_available_returnsTrue() {
        Reward reward = Reward.builder()
                .name("Voucher 20K")
                .probability(100.0)
                .totalQuantity(10)
                .maxQuantityPerPeriod(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .limitFromDate(LocalDate.now())
                .limitToDate(LocalDate.now().plusDays(1))
                .build();

        when(spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(0);


        boolean result = rewardService.isRewardAvailable(reward, LocalDateTime.now());
        assertTrue(result);
    }

    @Test
    void determineWinningReward_noValidRewards() {
        when(rewardRepository.findValidRewards(any())).thenReturn(List.of()); // Danh sách rỗng

        SpinResult result = rewardService.determineWinningReward("user123");

        assertFalse(result.isWinner());
        assertNull(result.getRewardName());
        verify(spinHistoryRepository, times(1)).save(argThat(history ->
                history.getCustomerId().equals("user123") &&
                        history.getRewardName().equals("Không trúng thưởng") &&
                        !history.isWinner()
        ));
    }


}