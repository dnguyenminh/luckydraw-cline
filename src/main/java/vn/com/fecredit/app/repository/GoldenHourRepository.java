package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.model.GoldenHour;

@Repository
public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {

    boolean existsByEventIdAndName(Long eventId, String name);

    List<GoldenHour> findByEventIdAndIsActiveTrue(Long eventId);

    List<GoldenHour> findByRewardIdAndIsActiveTrue(Long rewardId);

    @Query("SELECT gh FROM GoldenHour gh " +
           "LEFT JOIN FETCH gh.reward " +
           "WHERE gh.id = :id")
    Optional<GoldenHour> findByIdWithDetails(@Param("id") Long id);

//     @Query("SELECT gh FROM GoldenHour gh " +
//            "WHERE gh.reward.id = :rewardId " +
//            "AND gh.isActive = true " +
//            "AND :hour >= gh.startHour " +
//            "AND :hour < gh.endHour")
//     Optional<GoldenHour> findActiveGoldenHourByHour(
//             @Param("rewardId") Long rewardId,
//             @Param("hour") int hour);

//     default Optional<GoldenHour> findActiveGoldenHour(Long rewardId, LocalDateTime currentTime) {
//         return findActiveGoldenHourByHour(rewardId, currentTime.getHour());
//     }

    @Query("SELECT DISTINCT gh FROM GoldenHour gh " +
           "LEFT JOIN FETCH gh.reward r " +
           "WHERE gh.isActive = true " + 
           "AND gh.reward.id = :rewardId")
    List<GoldenHour> findActiveByRewardIdWithDetails(@Param("rewardId") Long rewardId);

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.reward.id = :rewardId " +
           "AND gh.isActive = true " +
           "ORDER BY gh.startTime ASC")
    List<GoldenHour> findActiveGoldenHoursOrdered(@Param("rewardId") Long rewardId);

    @Query("SELECT COUNT(gh) > 0 FROM GoldenHour gh " +
           "WHERE gh.reward.id = :rewardId " +
           "AND gh.isActive = true " +
           "AND ((" +
             "FUNCTION('to_char', gh.startTime, 'HH24:MI') <= FUNCTION('to_char', gh.endTime, 'HH24:MI') " +
             "AND FUNCTION('to_char', cast(:currentTime as timestamp), 'HH24:MI') BETWEEN FUNCTION('to_char', gh.startTime, 'HH24:MI') AND FUNCTION('to_char', gh.endTime, 'HH24:MI')" +
           ") OR (" +
             "FUNCTION('to_char', gh.startTime, 'HH24:MI') > FUNCTION('to_char', gh.endTime, 'HH24:MI') " +
             "AND (FUNCTION('to_char', cast(:currentTime as timestamp), 'HH24:MI') >= FUNCTION('to_char', gh.startTime, 'HH24:MI') " +
             "OR FUNCTION('to_char', cast(:currentTime as timestamp), 'HH24:MI') <= FUNCTION('to_char', gh.endTime, 'HH24:MI'))" +
           "))")
    boolean isGoldenHourActive(@Param("rewardId") Long rewardId,
                               @Param("currentTime") LocalDateTime currentTime);

    default boolean isGoldenHourActive(Long rewardId, int currentHour) {
        return isGoldenHourActive(rewardId, LocalDateTime.now().withHour(currentHour).withMinute(0).withSecond(0).withNano(0));
    }
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE GoldenHour gh SET gh.isActive = :status WHERE gh.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") boolean status);

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.event.id = :eventId " +
           "AND gh.isActive = true " +
           "AND (" +
               "(:testTime BETWEEN gh.startTime AND gh.endTime) " +
               "OR (gh.startTime > gh.endTime AND (:testTime >= gh.startTime OR :testTime <= gh.endTime))" +
           ")")
    Optional<GoldenHour> findActiveGoldenHour(@Param("eventId") Long eventId, @Param("testTime") LocalDateTime testTime);
}