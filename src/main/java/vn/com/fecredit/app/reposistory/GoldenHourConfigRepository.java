package vn.com.fecredit.app.reposistory;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fecredit.app.model.GoldenHourConfig;

import java.util.List;

public interface GoldenHourConfigRepository extends JpaRepository<GoldenHourConfig, Long> {
    List<GoldenHourConfig> findByRewardName(String rewardName);
}