package vn.com.fecredit.app;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    long countByCustomerIdAndSpinDate(String customerId, LocalDate spinDate);
}
