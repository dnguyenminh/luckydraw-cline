package vn.com.fecredit.app.reposistory;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fecredit.app.model.Participant;

import java.util.List;


public interface ParticipantRepository extends JpaRepository<Participant, String> { // Assuming customerId is the primary key
    List<Participant> findAll();

    long count(); //Thêm method này để đếm tổng số record
}