package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findAllByStatus(@Param("status") int status);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findAllByStatus(@Param("status") int status, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") int status);

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") int status);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = 1")
    List<User> findActiveByRole(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.status = 1")
    List<User> findActiveByRoles(@Param("roleNames") Set<String> roleNames);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 1")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.status = 1")
    Optional<User> findActiveByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.username = :username AND u.status = 1")
    boolean existsActiveByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.email = :email AND u.status = 1")
    boolean existsActiveByEmail(@Param("email") String email);

//    @Modifying
//    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :id")
//    void updateLastLogin(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.status = 1")
    List<User> findAllActive();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActive();
}
