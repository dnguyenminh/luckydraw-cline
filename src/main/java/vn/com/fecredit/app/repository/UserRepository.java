package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.User;

import java.util.Optional;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllActive();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "JOIN u.roles r WHERE u.id = :userId AND r.name = :roleName")
    boolean hasRole(@Param("userId") Long userId, @Param("roleName") String roleName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT(:searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT(:searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}