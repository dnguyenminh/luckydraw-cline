package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.model.LuckyDrawResult;

import java.time.LocalDateTime;

@Repository
public interface LuckyDrawResultRepository extends JpaRepository<LuckyDrawResult, Long> {

    @Query("SELECT COUNT(l) FROM LuckyDrawResult l WHERE l.reward.id = :rewardId " +
            "AND l.winTime BETWEEN :startDate AND :endDate")
    long countByRewardIdAndWinTimeBetween(
            @Param("rewardId") Long rewardId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}