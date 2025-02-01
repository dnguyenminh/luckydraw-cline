package vn.com.fecredit.app;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ParticipantRepository extends JpaRepository<Participant, String> { // Assuming customerId is the primary key
    List<Participant> findAll();

    long count(); //Thêm method này để đếm tổng số record
}