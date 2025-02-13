package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.Participant;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ParticipantRepository extends JpaRepository<Participant, Long>, JpaSpecificationExecutor<Participant> {

    Optional<Participant> findByEventIdAndEmployeeId(Long eventId, String employeeId);

    List<Participant> findByEventIdAndIsActive(Long eventId, boolean isActive);

    boolean existsByEventIdAndEmployeeId(Long eventId, String employeeId);

    boolean existsByCustomerId(String customerId);

    boolean existsByCardNumber(String cardNumber);

    boolean existsByEmail(String email);

    long countByEventId(Long eventId);

    Optional<Participant> findByCustomerId(String customerId);

    List<Participant> findByEventIdAndSpinsRemainingGreaterThan(Long eventId, Long spins);

    @Modifying
    @Transactional
    @Query("UPDATE Participant p " +
           "SET p.spinsRemaining = :spinsRemaining, " +
           "    p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id")
    int updateSpinsRemaining(@Param("id") Long id, @Param("spinsRemaining") Long spinsRemaining);

    @Query("SELECT p FROM Participant p " +
           "WHERE p.event.id = :eventId " +
           "AND p.isActive = true " +
           "AND p.spinsRemaining > 0")
    List<Participant> findEligibleParticipants(@Param("eventId") Long eventId);

    @Query("select p from Participant p " +
           "where p.event.id = :eventId " +
           "and p.isActive = true " +
           "and (lower(p.name) like lower(concat('%', :searchText, '%')) " +
           "or lower(p.email) like lower(concat('%', :searchText, '%')) " +
           "or lower(p.customerId) like lower(concat('%', :searchText, '%')) " +
           "or lower(p.cardNumber) like lower(concat('%', :searchText, '%')) " +
           "or lower(p.employeeId) like lower(concat('%', :searchText, '%')))")
    List<Participant> searchParticipants(@Param("eventId") Long eventId, @Param("searchText") String searchText);

    @Modifying
    @Transactional
    @Query("UPDATE Participant p " +
           "SET p.isActive = :status, " +
           "    p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id")
    int updateParticipantStatus(@Param("id") Long id, @Param("status") boolean status);

    @Query("SELECT COUNT(p) > 0 FROM Participant p " +
           "WHERE p.event.id = :eventId " +
           "AND p.isActive = true " +
           "AND p.spinsRemaining > 0")
    boolean hasEligibleParticipants(@Param("eventId") Long eventId);
}