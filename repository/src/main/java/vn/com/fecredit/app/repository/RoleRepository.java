package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.enums.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleName name);
    
    List<Role> findByNameIn(Set<RoleName> names);
    
    boolean existsByName(RoleName name);
    
    boolean existsByNameAndStatus(RoleName name, int status);

    Page<Role> findByNameAndStatus(RoleName name, int status, Pageable pageable);
    
    List<Role> findByStatus(int status);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUsersId(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) FROM Role r JOIN r.users u WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);

    List<Role> findByNameInAndStatus(Set<RoleName> names, int status);

    // Additional useful queries
    
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.users u " +
           "WHERE r.name = :name AND r.status = :status")
    Optional<Role> findByNameAndStatusWithUsers(
            @Param("name") RoleName name, 
            @Param("status") int status);

    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.name IN :names")
    List<Role> findByNamesWithPermissions(@Param("names") Set<RoleName> names);

    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);
    
    @Query("SELECT r FROM Role r " +
           "WHERE r.status = :status " +
           "ORDER BY r.priority DESC, r.name ASC")
    List<Role> findByStatusOrderByPriorityDescNameAsc(@Param("status") int status);

    @Query("SELECT COUNT(DISTINCT u) FROM Role r JOIN r.users u " +
           "WHERE r.status = :status")
    long countActiveUsers(@Param("status") int status);

    @Query("SELECT COUNT(DISTINCT r) FROM Role r " +
           "WHERE r.status = :status")
    long countByStatus(@Param("status") int status);
}
