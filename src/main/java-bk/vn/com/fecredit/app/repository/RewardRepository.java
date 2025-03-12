package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.Reward;
import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    List<Reward> findAllByEventIdAndStatus(Long eventId, String status);
    
    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId AND r.status = :status AND r.remainingQuantity > 0")
    List<Reward> findAllAvailableByEventId(@Param("eventId") Long eventId, @Param("status") String status);
    
    @Modifying
    @Query("UPDATE Reward r SET r.remainingQuantity = :quantity WHERE r.id = :id")
    void updateRemainingQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Reward r SET r.remainingQuantity = r.remainingQuantity - 1 WHERE r.id = :id AND r.remainingQuantity > 0")
    void decrementRemainingQuantityById(@Param("id") Long id);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reward r WHERE r.id = :id AND r.remainingQuantity > 0")
    boolean hasAvailableQuantity(@Param("id") Long id);

    @Query("SELECT r FROM Reward r LEFT JOIN FETCH r.goldenHours WHERE r.id = :id")
    Reward findByIdWithGoldenHours(@Param("id") Long id);
}
