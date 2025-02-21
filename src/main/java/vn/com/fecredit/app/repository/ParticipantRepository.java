package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.model.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long>, JpaSpecificationExecutor<Participant> {
    
    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.isActive = true")
    List<Participant> findByEvent_IdAndIsActiveTrue(@Param("eventId") Long eventId);
    
    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.user.id = :userId")
    Optional<Participant> findByEvent_IdAndUser_Id(@Param("eventId") Long eventId, @Param("userId") Long userId);
    
    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);
    
    Optional<Participant> findByCustomerId(String customerId);
    
    boolean existsByCustomerId(String customerId);
    
    boolean existsByCardNumber(String cardNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.event.id = :eventId AND p.isActive = true")
    boolean existsByEvent_IdAndIsActiveTrue(@Param("eventId") Long eventId);
    
    Page<Participant> findAll(Specification<Participant> spec, Pageable pageable);
}