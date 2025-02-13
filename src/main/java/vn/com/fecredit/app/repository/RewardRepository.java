package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.Reward;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByEventIdAndIsActiveTrue(Long eventId);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND r.remainingQuantity > 0")
    List<Reward> findActiveRewardsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND r.remainingQuantity > 0 " +
           "AND (r.startDate IS NULL OR r.startDate <= :currentTime) " +
           "AND (r.endDate IS NULL OR r.endDate > :currentTime)")
    List<Reward> findAvailableRewards(
            @Param("eventId") Long eventId,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND r.applicableProvinces IS NOT NULL " +
           "AND lower(r.applicableProvinces) like lower(concat('%', :province, '%'))")
    List<Reward> findByEventIdAndProvinceAndIsActiveTrue(
            @Param("eventId") Long eventId,
            @Param("province") String province);

    @Modifying
    @Transactional
    @Query("UPDATE Reward r " +
           "SET r.remainingQuantity = :quantity, " +
           "    r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :id")
    int updateRemainingQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.event.id = :eventId " +
           "AND (r.startDate >= :startDate AND r.startDate < :endDate " +
           "     OR r.endDate > :startDate AND r.endDate <= :endDate)")
    List<Reward> findCurrentRewards(
            @Param("eventId") Long eventId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) > 0 FROM Reward r " +
           "WHERE r.id = :id " +
           "AND r.remainingQuantity > 0")
    boolean hasAvailableQuantity(@Param("id") Long id);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND r.remainingQuantity > 0 " +
           "ORDER BY r.probability DESC")
    List<Reward> findAvailableRewardsOrderByProbability(@Param("eventId") Long eventId);
}