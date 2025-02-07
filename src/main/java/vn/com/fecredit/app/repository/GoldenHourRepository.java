package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.model.GoldenHour;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {
    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.isActive = true " +
           "AND gh.startTime <= :dateTime " +
           "AND gh.endTime > :dateTime")
    Optional<GoldenHour> findActiveGoldenHour(@Param("dateTime") LocalDateTime dateTime);

    Optional<GoldenHour> findByRewardIdAndIsActiveTrue(Long rewardId);
}