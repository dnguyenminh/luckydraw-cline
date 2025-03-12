package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends BaseRepository<Province, Long> {

    Optional<Province> findByCode(String code);
    
    List<Province> findAllByStatus(Integer status);
    
    Page<Province> findAllByStatus(Integer status, Pageable pageable);
    
    boolean existsByCode(String code);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Participant p " +
           "WHERE p.province.id = :provinceId AND p.status = 1")
    boolean hasActiveParticipants(@Param("provinceId") Long provinceId);

    List<Province> findAllByRegionId(Long regionId);

    List<Province> findAllByRegionIdAndStatus(Long regionId, Integer status);

    @Query("SELECT p FROM Province p " +
           "LEFT JOIN FETCH p.participants " +
           "WHERE p.id = :id")
    Optional<Province> findByIdWithParticipants(@Param("id") Long id);

    @Query("SELECT p FROM Province p " +
           "LEFT JOIN FETCH p.region " +
           "WHERE p.id = :id")
    Optional<Province> findByIdWithRegion(@Param("id") Long id);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT COUNT(p) > 0 FROM Province p " +
           "WHERE p.region.id = :regionId AND p.status = 1")
    boolean hasActiveProvincesByRegionId(@Param("regionId") Long regionId);
}
