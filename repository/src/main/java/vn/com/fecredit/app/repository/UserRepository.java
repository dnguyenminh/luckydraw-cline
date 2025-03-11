package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByStatus(int status);
    Page<User> findAllByStatus(int status, Pageable pageable);
    long countByStatus(int status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.status = 1")
    boolean existsActiveByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.status = 1")
    boolean existsActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 1")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = 1")
    List<User> findActiveByRole(@Param("roleName") RoleName roleName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.status = 1")
    List<User> findActiveByRoles(@Param("roleNames") Set<RoleName> roleNames);

    @Query("SELECT u FROM User u WHERE u.status = 1")
    List<User> findAllActive();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActive();

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") int status);
}
