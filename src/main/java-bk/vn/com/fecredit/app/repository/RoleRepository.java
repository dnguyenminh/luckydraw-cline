package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @Query("SELECT r FROM Role r WHERE r.name = :name AND r.status = 'ACTIVE'")
    Optional<Role> findActiveByName(@Param("name") RoleName name);

    Optional<Role> findByName(RoleName name);

    List<Role> findByNameIn(Set<RoleName> names);

    @Query("SELECT r FROM Role r WHERE r.status = :status")
    List<Role> findAllByStatus(@Param("status") EntityStatus status);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.users WHERE r.status = 'ACTIVE'")
    List<Role> findAllActiveWithUsers();

    boolean existsByName(RoleName name);

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name AND r.status = 'ACTIVE'")
    boolean existsActiveByName(@Param("name") RoleName name);

    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.users u " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.id = :id")
    Optional<Role> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT r FROM Role r WHERE " +
           "(:name IS NULL OR r.name = :name) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Role> findAllFiltered(
        @Param("name") RoleName name,
        @Param("status") EntityStatus status,
        Pageable pageable
    );

    @Query("SELECT r FROM Role r " +
           "WHERE r.status = 'ACTIVE' " +
           "AND r.name <> 'ADMIN' " +
           "ORDER BY r.name")
    List<Role> findAllAssignable();

    @Query("SELECT r FROM Role r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId AND r.status = 'ACTIVE'")
    Set<Role> findActiveByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                   "JOIN user_roles ur ON u.id = ur.user_id " +
                   "WHERE ur.role_id = :roleId",
           nativeQuery = true)
    long countUsersByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT r FROM Role r WHERE " +
           "r.status = 'ACTIVE' AND " +
           "r.name IN :names")
    Set<Role> findActiveByNames(@Param("names") Set<RoleName> names);
}
