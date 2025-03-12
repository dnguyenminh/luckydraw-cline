package vn.com.fecredit.app.converter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.converter.EntityStatusConverterIntegrationTest.TestEntity;

import java.util.List;

public interface TestEntityRepository extends 
    JpaRepository<TestEntity, Long>,
    JpaSpecificationExecutor<TestEntity> {
    
    // Using the enum directly in method name
    List<TestEntity> findByStatus(EntityStatus status);
    
    // Using native query with string status
    @Query(value = "SELECT * FROM test_entities WHERE status = :status", nativeQuery = true)
    List<TestEntity> findByStatusNative(@Param("status") String status);
    
    // Using JPQL with enum parameter
    @Query("SELECT e FROM TestEntity e WHERE e.status = :status")
    List<TestEntity> findByStatusJPQL(@Param("status") EntityStatus status);
    
    // Find entities with status in a list of statuses
    List<TestEntity> findByStatusIn(List<EntityStatus> statuses);
    
    // Count by status
    long countByStatus(EntityStatus status);
}
