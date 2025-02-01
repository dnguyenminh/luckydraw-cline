package vn.com.fecredit.app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoldenHourConfigRepository extends JpaRepository<GoldenHourConfig, Long> {
    List<GoldenHourConfig> findByRewardName(String rewardName);
}