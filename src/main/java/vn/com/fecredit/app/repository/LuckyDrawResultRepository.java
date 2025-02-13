package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.LuckyDrawResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public interface LuckyDrawResultRepository extends JpaRepository<LuckyDrawResult, Long> {

    List<LuckyDrawResult> findByParticipantId(Long participantId);

    @Query("SELECT r FROM LuckyDrawResult r " +
           "WHERE r.participant.id = :participantId " +
           "AND r.reward.id = :rewardId " +
           "AND r.isClaimed = false")
    List<LuckyDrawResult> findUnclaimedRewards(
            @Param("participantId") Long participantId,
            @Param("rewardId") Long rewardId);

    @Query("SELECT r FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId " +
           "AND r.packNumber = :packNumber " +
           "AND r.isClaimed = false")
    Optional<LuckyDrawResult> findUnclaimedByRewardIdAndPackNumber(
            @Param("rewardId") Long rewardId,
            @Param("packNumber") Integer packNumber);

    @Query("SELECT COUNT(r) FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId " +
           "AND r.isClaimed = false " +
           "AND r.winTime >= :startTime " +
           "AND r.winTime < :endTime")
    long countUnclaimedByRewardIdAndWinTimeBetween(
            @Param("rewardId") Long rewardId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(r) FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId " +
           "AND r.packNumber = :packNumber")
    long countByRewardIdAndPackNumber(
            @Param("rewardId") Long rewardId,
            @Param("packNumber") Integer packNumber);

    @Query("SELECT DISTINCT r.packNumber FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId " +
           "ORDER BY r.packNumber")
    Set<Integer> findPackNumbersByRewardId(@Param("rewardId") Long rewardId);

    @Query("SELECT r FROM LuckyDrawResult r " +
           "WHERE r.participant.event.id = :eventId " +
           "AND r.winTime >= :startTime " +
           "AND r.winTime < :endTime " +
           "ORDER BY r.winTime DESC")
    List<LuckyDrawResult> findByEventIdAndTimeRange(
            @Param("eventId") Long eventId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query(value = "WITH RECURSIVE number_sequence AS (" +
           "  SELECT 1 as n " +
           "  UNION ALL " +
           "  SELECT n + 1 FROM number_sequence WHERE n < :maxPacks " +
           "), available_packs AS (" +
           "  SELECT n FROM number_sequence " +
           "  WHERE n NOT IN (SELECT pack_number FROM lucky_draw_results " +
           "                  WHERE reward_id = :rewardId) " +
           "  LIMIT :limit" +
           ") " +
           "SELECT n FROM available_packs",
           nativeQuery = true)
    List<Integer> findAvailablePacksForReward(
            @Param("rewardId") Long rewardId,
            @Param("maxPacks") Long maxPacks,
            @Param("limit") Long limit);

    @Modifying
    @Transactional
    @Query("UPDATE LuckyDrawResult r " +
           "SET r.isClaimed = true, " +
           "    r.claimedAt = CURRENT_TIMESTAMP, " +
           "    r.claimedBy = :claimedBy, " +
           "    r.claimNotes = :notes " +
           "WHERE r.id = :id " +
           "AND r.isClaimed = false")
    int claimReward(
            @Param("id") Long id,
            @Param("claimedBy") String claimedBy,
            @Param("notes") String notes);

    @Query("SELECT r FROM LuckyDrawResult r " +
           "LEFT JOIN FETCH r.participant p " +
           "LEFT JOIN FETCH r.reward " +
           "WHERE r.spinHistory.id = :spinHistoryId")
    Optional<LuckyDrawResult> findBySpinHistoryIdWithDetails(
            @Param("spinHistoryId") Long spinHistoryId);

    @Query("SELECT COUNT(r) FROM LuckyDrawResult r " +
           "WHERE r.participant.event.id = :eventId " +
           "AND r.reward.id = :rewardId")
    long countRewardWins(
            @Param("eventId") Long eventId,
            @Param("rewardId") Long rewardId);

    @Query("SELECT MAX(r.packNumber) FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId")
    Optional<Integer> findMaxPackNumber(@Param("rewardId") Long rewardId);

    @Query("SELECT COUNT(r) > 0 FROM LuckyDrawResult r " +
           "WHERE r.reward.id = :rewardId " +
           "AND r.packNumber = :packNumber")
    boolean existsByRewardIdAndPackNumber(
            @Param("rewardId") Long rewardId,
            @Param("packNumber") Integer packNumber);
}
