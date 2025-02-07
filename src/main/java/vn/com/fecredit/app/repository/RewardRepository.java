package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.model.Reward;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Query("""
            SELECT r FROM Reward r 
            WHERE r.isActive = true 
            AND r.remainingQuantity > 0 
            AND (r.startDate IS NULL OR r.startDate <= :currentTime)
            AND (r.endDate IS NULL OR r.endDate >= :currentTime)
            AND (r.applicableProvinces IS NULL 
                OR r.applicableProvinces = ''
                OR r.applicableProvinces LIKE %:province%)
            """)
    List<Reward> findAvailableRewardsForProvince(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("province") String province);
}