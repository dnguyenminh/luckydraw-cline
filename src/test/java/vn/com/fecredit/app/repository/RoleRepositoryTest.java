package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Role;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAllInBatch();
        
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);
    }

    @Test
    void findByName_ShouldReturnRole_WhenExists() {
        Optional<Role> found = roleRepository.findByName("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNotExists() {
        Optional<Role> found = roleRepository.findByName("ROLE_NOT_EXISTS");

        assertThat(found).isEmpty();
    }

    @Test
    void findByNameIn_ShouldReturnMatchingRoles() {
        Set<Role> roles = roleRepository.findByNameIn(Set.of("ROLE_ADMIN", "ROLE_USER"));

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting("name")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void findByNameIn_ShouldReturnEmpty_WhenNoMatches() {
        Set<Role> roles = roleRepository.findByNameIn(Set.of("ROLE_NOT_EXISTS"));

        assertThat(roles).isEmpty();
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenExists() {
        boolean exists = roleRepository.existsByName("ROLE_ADMIN");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNotExists() {
        boolean exists = roleRepository.existsByName("ROLE_NOT_EXISTS");

        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldCreateNewRole() {
        Role newRole = Role.builder()
                .name("ROLE_NEW")
                .build();
        
        Role saved = roleRepository.save(newRole);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<Role> found = roleRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_NEW");
    }

    @Test
    void delete_ShouldRemoveRole() {
        roleRepository.delete(userRole);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<Role> found = roleRepository.findById(userRole.getId());
        assertThat(found).isEmpty();
    }
}