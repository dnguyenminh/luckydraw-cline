package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fecredit.app.entity.GoldenHour;
import java.util.List;

public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {
    List<GoldenHour> findAllByEventIdAndStatus(Long eventId, String status);
}
