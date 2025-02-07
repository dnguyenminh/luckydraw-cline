package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.model.Participant;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long>, JpaSpecificationExecutor<Participant> {
    Optional<Participant> findByCustomerId(String customerId);
    Optional<Participant> findByCardNumber(String cardNumber);
    Optional<Participant> findByEmail(String email);
    boolean existsByCustomerId(String customerId);
    boolean existsByCardNumber(String cardNumber);
    boolean existsByEmail(String email);
}