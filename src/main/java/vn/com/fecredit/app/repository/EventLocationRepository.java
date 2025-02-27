package vn.com.fecredit.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.EventLocation;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {

    List<EventLocation> findByEventId(Long eventId);
    
    Page<EventLocation> findByEventId(Long eventId, Pageable pageable);
    
    List<EventLocation> findByActiveTrue();
    
    long countByActiveTrue();
    
    List<EventLocation> findByProvince(String province);
    
    List<EventLocation> findByDistrict(String district);
}
