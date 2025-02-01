package vn.com.fecredit.app.reposistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.model.Reward;

import java.time.LocalDate;
import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    @Query("SELECT r FROM Reward r WHERE r.startDate <= :currentDate AND r.endDate >= :currentDate")
    List<Reward> findValidRewards(@Param("currentDate") LocalDate currentDate);}