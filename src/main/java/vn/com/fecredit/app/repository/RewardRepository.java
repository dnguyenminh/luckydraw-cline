package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.model.Reward;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    List<Reward> findByEventId(Long eventId);
    
    List<Reward> findByEventIdAndIsActiveTrue(Long eventId);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId AND r.isActive = true")
    List<Reward> findActiveRewardsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND (r.startDate IS NULL OR r.startDate <= :now) " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Reward> findAvailableRewards(@Param("eventId") Long eventId, @Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM rewards r WHERE r.event_id = :eventId " +
           "AND :province = ANY (r.applicable_provinces) " +
           "AND r.is_active = true", nativeQuery = true)
    List<Reward> findByEventIdAndProvinceAndIsActiveTrue(
            @Param("eventId") Long eventId, 
            @Param("province") String province);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "ORDER BY r.probability DESC")
    List<Reward> findAvailableRewardsOrderByProbability(@Param("eventId") Long eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "WITH locked AS (" +
                  "  SELECT id FROM rewards " +
                  "  WHERE id = :id AND remaining_quantity > 0 " +
                  "  FOR UPDATE" +
                  ") " +
                  "UPDATE rewards r " +
                  "SET remaining_quantity = remaining_quantity - 1 " +
                  "WHERE r.id IN (SELECT id FROM locked)",
           nativeQuery = true)
    int decrementRemainingQuantity(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Reward r SET r.remainingQuantity = :quantity WHERE r.id = :id")
    int updateRemainingQuantity(@Param("id") Long id, @Param("quantity") int quantity);
}