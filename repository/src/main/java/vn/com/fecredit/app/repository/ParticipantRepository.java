package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface ParticipantRepository extends BaseRepository<Participant, Long> {

//    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.id = :participantId")
//    Optional<Participant> findByEventIdAndId(@Param("eventId") Long eventId, @Param("participantId") Long participantId);

    @Query("SELECT p FROM Participant p WHERE p.account = :account")
    Optional<Participant> findByCustomerId(@Param("account") String account);

    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.account = :account")
    boolean existsByAccount(@Param("account") String account);

//    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.cardNumber = :cardNumber")
//    boolean existsByCardNumber(@Param("cardNumber") String cardNumber);

    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.email = :email")
    boolean existsByEmail(@Param("email") String email);

//    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId")
//    List<Participant> findByEventId(@Param("eventId") Long eventId);
//
//    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.active = true")
//    List<Participant> findActiveByEventId(@Param("eventId") Long eventId);

//    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId")
//    long countByEventId(@Param("eventId") Long eventId);
//
//    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId AND p.active = true")
//    long countActiveByEventId(@Param("eventId") Long eventId);

//    @Query("SELECT DISTINCT p.customerId FROM Participant p WHERE p.event.id = :eventId")
//    List<String> findDistinctCustomerIdsByEventId(@Param("eventId") Long eventId);

//    @Query("SELECT DISTINCT p.cardNumber FROM Participant p WHERE p.event.id = :eventId")
//    List<String> findDistinctCardNumbersByEventId(@Param("eventId") Long eventId);
}
