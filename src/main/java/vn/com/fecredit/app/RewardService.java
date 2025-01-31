package vn.com.fecredit.app;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
public class RewardService {
    private final RewardRepository rewardRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private static final int MAX_SPINS_PER_DAY = 5;

    public RewardService(RewardRepository rewardRepository, SpinHistoryRepository spinHistoryRepository) {
        this.rewardRepository = rewardRepository;
        this.spinHistoryRepository = spinHistoryRepository;
    }

    public String spin(String customerId) {
        if (!canSpin(customerId)) {
            return "Bạn đã hết lượt quay hôm nay!";
        }

        List<Reward> rewards = rewardRepository.findAll();
        boolean isGoldenHour = checkGoldenHour();

        for (Reward reward : rewards) {
            double probability = isGoldenHour ? reward.getProbability() + reward.getGoldenHourProbability() : reward.getProbability();
            if (new Random().nextDouble() * 100 < probability) {
                saveSpinHistory(customerId, reward.getName(), true);
                return "Chúc mừng " + customerId + "! Bạn đã trúng " + reward.getName();
            }
        }

        saveSpinHistory(customerId, "Không trúng", false);
        return "Chúc bạn may mắn lần sau!";
    }

    private boolean checkGoldenHour() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0));
    }

    private boolean canSpin(String customerId) {
        long todaySpins = spinHistoryRepository.countByCustomerIdAndSpinDate(customerId, java.time.LocalDate.now());
        return todaySpins < MAX_SPINS_PER_DAY;
    }

    private void saveSpinHistory(String customerId, String rewardName, boolean isWinner) {
        SpinHistory history = SpinHistory.builder()
                .customerId(customerId)
                .rewardName(rewardName)
                .winner(isWinner)
                .spinDate(java.time.LocalDate.now())
                .build();
        spinHistoryRepository.save(history);
    }
}
