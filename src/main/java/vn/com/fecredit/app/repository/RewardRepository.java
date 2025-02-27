package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Reward;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reward r WHERE r.id = :id")
    Optional<Reward> findByIdWithLock(Long id);

    @Modifying
    @Query("UPDATE Reward r SET r.remainingQuantity = r.remainingQuantity - 1 " +
           "WHERE r.id = :id AND r.remainingQuantity > 0")
    int decrementRemainingQuantity(Long id);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.isActive = true " +
           "AND r.remainingQuantity > 0 " +
           "AND r.startDate <= :now " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Reward> findAvailableRewards(Long eventId, LocalDateTime now);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.probability > 0 " +
           "AND r.remainingQuantity > 0 " +
           "AND r.isActive = true")
    List<Reward> findActiveRewardsByEventId(Long eventId);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId")
    Page<Reward> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.remainingQuantity > 0")
    long countAvailableRewards(Long eventId);

    @Query("SELECT SUM(r.remainingQuantity) FROM Reward r WHERE r.event.id = :eventId")
    Long getTotalRemainingQuantity(Long eventId);

    @Query("SELECT r FROM Reward r WHERE r.event.id = :eventId " +
           "AND r.name LIKE %:keyword% " +
           "OR r.code LIKE %:keyword% " +
           "OR r.description LIKE %:keyword%")
    Page<Reward> search(Long eventId, String keyword, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reward r LEFT JOIN FETCH r.goldenHours " +
           "WHERE r.id = :id")
    Optional<Reward> findByIdWithGoldenHours(Long id);

    boolean existsByCodeAndEventId(String code, Long eventId);
}
