package vn.com.fecredit.app;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardServiceTest {

    private final RewardRepository rewardRepository = mock(RewardRepository.class);
    private final SpinHistoryRepository spinHistoryRepository = mock(SpinHistoryRepository.class);
    private final RewardService rewardService = new RewardService(rewardRepository, spinHistoryRepository);

    @Test
    void shouldReturnPrizeWhenWinning() {
        Reward reward = Reward.builder()
                .name("Xe Máy")
                .totalQuantity(10)
                .probability(1.0)
                .goldenHourProbability(5.0)
                .build();

        when(rewardRepository.findAll()).thenReturn(Arrays.asList(reward));
        when(spinHistoryRepository.countByCustomerIdAndDate("user123", LocalDate.now())).thenReturn(0L);

        String result = rewardService.spin("user123");

        assertThat(result).contains("Bạn đã trúng Xe Máy");
    }

    @Test
    void shouldReturnMessageWhenLosing() {
        when(rewardRepository.findAll()).thenReturn(Arrays.asList());
        when(spinHistoryRepository.countByCustomerIdAndDate("user123", LocalDate.now())).thenReturn(0L);

        String result = rewardService.spin("user123");

        assertThat(result).contains("Chúc bạn may mắn lần sau");
    }

    @Test
    void shouldNotAllowMoreThanFiveSpins() {
        when(spinHistoryRepository.countByCustomerIdAndDate("user123", LocalDate.now())).thenReturn(5L);

        String result = rewardService.spin("user123");

        assertThat(result).contains("Bạn đã hết lượt quay hôm nay");
    }
}
