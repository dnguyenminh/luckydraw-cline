package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends BaseRepository<Region, Long> {

    Optional<Region> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Region> findAllByStatus(Integer status);
    
    Page<Region> findAllByStatus(Integer status, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Region r " +
           "LEFT JOIN FETCH r.provinces p " +
           "LEFT JOIN p.participants pt " +
           "WHERE SIZE(pt) > 0")
    List<Region> findRegionsWithParticipants();

    @Query("SELECT DISTINCT r FROM Region r " +
           "LEFT JOIN FETCH r.eventLocations el " +
           "WHERE SIZE(el) > 0")
    List<Region> findRegionsWithLocations();

    @Query("SELECT r FROM Region r " +
           "LEFT JOIN FETCH r.provinces " +
           "WHERE r.id = :id")
    Optional<Region> findByIdWithProvinces(@Param("id") Long id);

    @Query("SELECT r FROM Region r " +
           "LEFT JOIN FETCH r.eventLocations " +
           "WHERE r.id = :id")
    Optional<Region> findByIdWithLocations(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Province p " +
           "WHERE p.region.id = :regionId AND p.status = 1")
    boolean hasActiveProvinces(@Param("regionId") Long regionId);

    @Query("SELECT CASE WHEN COUNT(el) > 0 THEN true ELSE false END " +
           "FROM EventLocation el " +
           "WHERE el.region.id = :regionId AND el.status = 1")
    boolean hasActiveEventLocations(@Param("regionId") Long regionId);

    boolean existsByCodeAndIdNot(String code, Long id);
}
