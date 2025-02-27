package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.User.UserStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test role
        userRole = Role.builder()
            .name(RoleName.ROLE_PARTICIPANT)
            .description("Normal user role")
            .build();
        userRole = roleRepository.save(userRole);

        // Create test user
        testUser = User.builder()
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .fullName("Test User")
            .phoneNumber("+84123456789")
            .isActive(true)
            .status(UserStatus.ACTIVE)
            .build();
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);
    }

    @Test
    void findByUsername_ExistingUser_ShouldReturnUser() {
        Optional<User> found = userRepository.findByUsername(testUser.getUsername());
        
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(found.get().getRoles()).contains(userRole);
    }

    @Test
    void findByUsername_NonExistingUser_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_ExistingUser_ShouldReturnUser() {
        Optional<User> found = userRepository.findByEmail(testUser.getEmail());
        
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void findByEmail_NonExistingUser_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_ExistingUser_ShouldReturnTrue() {
        boolean exists = userRepository.existsByUsername(testUser.getUsername());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_NonExistingUser_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ExistingUser_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail(testUser.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingUser_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void save_WithRole_ShouldSaveUserAndRole() {
        User newUser = User.builder()
            .username("newuser")
            .password("password")
            .email("new@example.com")
            .fullName("New User")
            .phoneNumber("+84987654321")
            .isActive(true)
            .status(UserStatus.ACTIVE)
            .build();
        newUser.addRole(userRole);

        User saved = userRepository.save(newUser);
        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRoles()).hasSize(1);
        assertThat(found.getRoles().iterator().next().getName()).isEqualTo(RoleName.ROLE_PARTICIPANT);
    }

    @Test
    void delete_ShouldRemoveUser() {
        userRepository.delete(testUser);
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    void userStatus_ShouldBePersisted() {
        testUser.setStatus(UserStatus.LOCKED);
        userRepository.save(testUser);

        User found = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(UserStatus.LOCKED);
    }
}
