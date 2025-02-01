package vn.com.fecredit.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    // Đếm số lần phần thưởng đã được phát trong ngày (từ 00:00 đến 23:59)
    @Query("SELECT COUNT(s) FROM SpinHistory s WHERE s.rewardName = :rewardName AND s.spinDateTime BETWEEN :startOfDay AND :endOfDay")
    int countByRewardNameAndSpinDateTimeBetween(
            @Param("rewardName") String rewardName,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
