package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {
    
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.status = :status ORDER BY r.name")
    List<Role> findAllActive(@Param("status") int status);

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    Set<Role> findByNames(@Param("names") Set<String> names);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u = :user AND r.status = :status")
    Set<Role> findByUserAndStatus(
        @Param("user") User user,
        @Param("status") int status
    );

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role AND u.status = 1")
    long countActiveUsers(@Param("role") Role role);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.users WHERE r.id = :id")
    Optional<Role> findByIdWithUsers(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r " +
           "WHERE r.name = :roleName AND u.status = 1")
    long countActiveUsersByRoleName(@Param("roleName") String roleName);
}
