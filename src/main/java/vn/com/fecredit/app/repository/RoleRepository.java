package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
    
    Set<Role> findByNameIn(Set<RoleName> names);
    
    boolean existsByName(RoleName name);

    @Query("SELECT r FROM Role r WHERE r.deleted = false")
    Set<Role> findAllActive();
    
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = ?1 AND r.deleted = false") 
    boolean existsActiveByName(RoleName name);

    @Query("SELECT r FROM Role r WHERE r.name IN ?1 AND r.deleted = false")
    Set<Role> findActiveByNames(Set<RoleName> names);

    @Query("""
        SELECT r FROM Role r 
        JOIN r.participants p 
        WHERE p.id = ?1 AND r.deleted = false
    """)
    Set<Role> findByParticipantId(Long participantId);

    @Query("""
        SELECT r FROM Role r 
        JOIN r.users u 
        WHERE u.id = ?1 AND r.deleted = false
    """)
    Set<Role> findByUserId(Long userId);

    @Query("""
        SELECT COUNT(r) > 0 FROM Role r 
        JOIN r.participants p 
        WHERE p.id = ?1 AND r.name = ?2 AND r.deleted = false
    """)
    boolean hasParticipantRole(Long participantId, RoleName roleName);

    @Query("""
        SELECT COUNT(r) > 0 FROM Role r 
        JOIN r.users u 
        WHERE u.id = ?1 AND r.name = ?2 AND r.deleted = false
    """)
    boolean hasUserRole(Long userId, RoleName roleName);
}
