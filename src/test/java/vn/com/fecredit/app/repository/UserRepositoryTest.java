package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
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
import vn.com.fecredit.app.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User activeUser;
    private User inactiveUser;
    private Role adminRole;
    private Role userRole;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Clear existing users and roles and flush changes to ensure a clean state
        userRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.flush();
        roleRepository.flush();

        adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        adminRole = roleRepository.save(adminRole);

        userRole = Role.builder()
                .name("ROLE_USER")
                .build();
        userRole = roleRepository.save(userRole);

        activeUser = User.builder()
                .username("active.user")
                .email("active@test.com")
                .password("password")
                .enabled(true)
                .roles(Set.of(adminRole, userRole))
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .lastModifiedBy("system")
                .build();
        activeUser = userRepository.save(activeUser);

        inactiveUser = User.builder()
                .username("inactive.user")
                .email("inactive@other.com")
                .password("password")
                .enabled(false)
                .roles(Set.of(userRole))
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .lastModifiedBy("system")
                .build();
        inactiveUser = userRepository.save(inactiveUser);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findByUsername("active.user");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("active.user");
        assertThat(found.get().getRoles()).hasSize(2);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findByEmail("active@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("active@test.com");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        boolean exists = userRepository.existsByUsername("active.user");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        boolean exists = userRepository.existsByEmail("active@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void findAllActive_ShouldReturnOnlyActiveUsers() {
        List<User> activeUsers = userRepository.findAllActive();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getUsername()).isEqualTo("active.user");
    }

    @Test
    void updateEnabled_ShouldUpdateUserStatus() {
        int updated = userRepository.updateEnabled(activeUser.getId(), false);
        Optional<User> found = userRepository.findById(activeUser.getId());
        assertThat(updated).isEqualTo(1);
        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isFalse();
    }

    @Test
    void findByUsernameWithRoles_ShouldReturnUserWithRoles() {
        Optional<User> found = userRepository.findByUsernameWithRoles("active.user");

        assertThat(found).isPresent();
        User user = found.get();
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRoles()).extracting("name")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void hasRole_ShouldReturnTrue_WhenUserHasRole() {
        boolean hasRole = userRepository.hasRole(activeUser.getId(), "ROLE_ADMIN");

        assertThat(hasRole).isTrue();
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        boolean hasRole = userRepository.hasRole(inactiveUser.getId(), "ROLE_ADMIN");

        assertThat(hasRole).isFalse();
    }

    @Test
    void searchUsers_ShouldFindByUsername() {
        List<User> users = userRepository.searchUsers("active.user");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("active.user");
    }

    @Test
    void searchUsers_ShouldFindByEmail() {
        List<User> users = userRepository.searchUsers("active@test.com");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("active@test.com");
    }
}