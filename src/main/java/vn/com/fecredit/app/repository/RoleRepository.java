package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.Role;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Set<Role> findByNameIn(Set<String> names);

    boolean existsByName(String name);
}