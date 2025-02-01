package vn.com.fecredit.app;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RewardService {
    //    @Autowired
    private RewardRepository rewardRepository;

    //    @Autowired
    private SpinHistoryRepository spinHistoryRepository;
    //    @Autowired
    private GoldenHourConfigService goldenHourConfigService;

    public RewardService(RewardRepository rewardRepository, SpinHistoryRepository spinHistoryRepository, GoldenHourConfigService goldenHourConfigService) {
        this.rewardRepository = rewardRepository;
        this.spinHistoryRepository = spinHistoryRepository;
        this.goldenHourConfigService = goldenHourConfigService;
    }

    public SpinResult determineWinningReward(String customerId) {
        LocalDateTime now = LocalDateTime.now();
        List<Reward> rewards = rewardRepository.findValidRewards(now.toLocalDate());

        for (Reward reward : rewards) {
            // Tính xác suất đã điều chỉnh (cộng thêm % giờ vàng nếu có)
            double adjustedProbability = calculateAdjustedProbability(reward, now);

            // Kiểm tra xác suất và giới hạn số lượng
            if (isWinningSpin(adjustedProbability)) {
                // Lưu lịch sử quay thưởng
                saveSpinHistory(customerId, reward.getName(), true, now);

                // Giảm số lượng quà tổng
                reward.setTotalQuantity(reward.getTotalQuantity() - 1);
                rewardRepository.save(reward);

                return new SpinResult(true, reward.getName());
            }
        }

        // Nếu không trúng, lưu lịch sử và trả về kết quả
        saveSpinHistory(customerId, "Không trúng thưởng", false, now);
        return new SpinResult(false, null);
    }

    // Tính xác suất đã điều chỉnh (giờ vàng)
    public double calculateAdjustedProbability(Reward reward, LocalDateTime now) {
        double baseProbability = reward.getProbability();

        List<GoldenHourConfig> goldenHourConfigs = goldenHourConfigService.getGoldenHourConfigs(reward.getName());

        for (GoldenHourConfig config : goldenHourConfigs) {
            if (now.isAfter(config.getStartTime()) && now.isBefore(config.getEndTime())) {
                baseProbability += reward.getGoldenHourProbability();
                break; // Thoát khỏi vòng lặp sau khi tìm thấy khung giờ vàng phù hợp
            }
        }

        return baseProbability;
    }

    // Kiểm tra có phải giờ vàng không
    private boolean checkGoldenHour(LocalDateTime now) {
        int hour = now.getHour();
        return (hour >= 12 && hour < 14); // Ví dụ: Giờ vàng từ 12h-14h
    }

    // Kiểm tra xác suất trúng
    private boolean isWinningSpin(double probability) {
        return ThreadLocalRandom.current().nextDouble() * 100 < probability;
    }

    // Kiểm tra quà còn lại và giới hạn phát hành
    public boolean isRewardAvailable(Reward reward, LocalDateTime now) {
        // Kiểm tra tổng số lượng quà
        if (reward.getTotalQuantity() <= 0) {
            return false;
        }

        int issuedInPeriod = spinHistoryRepository.countByRewardNameAndSpinDateTimeBetween(
                reward.getName(),
                reward.getLimitFromDate().atStartOfDay(),
                reward.getLimitToDate().atTime(23, 59, 59)
        );

        return issuedInPeriod < reward.getMaxQuantityPerPeriod();
    }

    // Lưu lịch sử quay thưởng
    public void saveSpinHistory(String customerId, String rewardName, boolean isWinner, LocalDateTime spinDateTime) {
        SpinHistory history = SpinHistory.builder()
                .customerId(customerId)
                .rewardName(rewardName)
                .winner(isWinner)
                .spinDateTime(spinDateTime)
                .build();
        spinHistoryRepository.save(history);
    }
}
