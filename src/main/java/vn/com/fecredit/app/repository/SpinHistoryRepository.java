package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.model.SpinHistory;

import java.util.Optional;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    Optional<SpinHistory> findFirstByParticipantIdOrderBySpinTimeDesc(Long participantId);
}