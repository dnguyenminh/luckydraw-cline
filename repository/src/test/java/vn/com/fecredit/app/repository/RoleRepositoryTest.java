package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;
    private Role adminRole;
    private User user;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .name(RoleName.USER)
                .code("USER")
                .status(1)
                .priority(1)
                .permissions(new HashSet<>())
                .build();

        adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .code("ADMIN")
                .status(1)
                .priority(2)
                .permissions(new HashSet<>())
                .build();

        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .status(1)
                .roles(new HashSet<>())
                .build();

        entityManager.persist(userRole);
        entityManager.persist(adminRole);
        entityManager.persist(user);
        entityManager.flush();
    }

    @Nested
    class BasicQueries {
        @Test
        void findByName_WhenExists_ShouldReturnRole() {
            Optional<Role> found = roleRepository.findByName(RoleName.USER);
            
            assertThat(found).isPresent();
            assertThat(found.get().getCode()).isEqualTo("USER");
        }

        @Test
        void findByName_WhenNotExists_ShouldReturnEmpty() {
            Optional<Role> found = roleRepository.findByName(RoleName.MANAGER);
            
            assertThat(found).isEmpty();
        }

        @Test
        void findByNameIn_WithEmptySet_ShouldReturnEmptyList() {
            List<Role> roles = roleRepository.findByNameIn(Collections.emptySet());
            
            assertThat(roles).isEmpty();
        }

        @Test
        void existsByName_WhenNotExists_ShouldReturnFalse() {
            boolean exists = roleRepository.existsByName(RoleName.MANAGER);
            
            assertThat(exists).isFalse();
        }
    }

    @Nested
    class StatusBasedQueries {
        @Test
        void findByStatus_WhenInactive_ShouldReturnEmpty() {
            List<Role> inactiveRoles = roleRepository.findByStatus(0);
            
            assertThat(inactiveRoles).isEmpty();
        }

        @Test
        void existsByNameAndStatus_WhenInactive_ShouldReturnFalse() {
            userRole.setStatus(0);
            entityManager.persist(userRole);
            entityManager.flush();

            boolean exists = roleRepository.existsByNameAndStatus(RoleName.USER, 1);
            
            assertThat(exists).isFalse();
        }

        @Test
        void findByNameInAndStatus_WithInvalidData_ShouldReturnEmpty() {
            List<Role> roles = roleRepository.findByNameInAndStatus(
                Set.of(RoleName.MANAGER), 1);
            
            assertThat(roles).isEmpty();
        }
    }

    @Nested
    class PaginationQueries {
        @Test
        void findByNameAndStatus_WithEmptyPage_ShouldReturnEmptyResult() {
            Pageable pageable = PageRequest.of(1, 10); // Second page
            Page<Role> page = roleRepository.findByNameAndStatus(RoleName.USER, 1, pageable);
            
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isEqualTo(1);
        }

        @Test
        void findByNameAndStatus_WithInvalidStatus_ShouldReturnEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Role> page = roleRepository.findByNameAndStatus(RoleName.USER, 0, pageable);
            
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isZero();
        }
    }

    @Nested
    class UserRelatedQueries {
        @Test
        void findByUsersId_WhenUserHasNoRoles_ShouldReturnEmpty() {
            List<Role> roles = roleRepository.findByUsersId(user.getId());
            
            assertThat(roles).isEmpty();
        }

        @Test
        void countUsersByRoleId_WhenNoUsers_ShouldReturnZero() {
            long count = roleRepository.countUsersByRoleId(userRole.getId());
            
            assertThat(count).isZero();
        }

        @Test
        void findByUsersId_WithInvalidUserId_ShouldReturnEmpty() {
            List<Role> roles = roleRepository.findByUsersId(999L);
            
            assertThat(roles).isEmpty();
        }
    }

    @Nested
    class OptimizedQueries {
        @Test
        void findByNameAndStatusWithUsers_WhenNoUsers_ShouldReturnRoleWithEmptyUsers() {
            Optional<Role> found = roleRepository.findByNameAndStatusWithUsers(RoleName.USER, 1);
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsers()).isEmpty();
        }

        @Test
        void findByIdWithPermissions_WhenNoPermissions_ShouldReturnRoleWithEmptyPermissions() {
            Optional<Role> found = roleRepository.findByIdWithPermissions(userRole.getId());
            
            assertThat(found).isPresent();
            assertThat(found.get().getPermissions()).isEmpty();
        }

        @Test
        void findByNamesWithPermissions_WithEmptyNames_ShouldReturnEmpty() {
            List<Role> roles = roleRepository.findByNamesWithPermissions(Collections.emptySet());
            
            assertThat(roles).isEmpty();
        }
    }

    @Nested
    class OrderingQueries {
        @Test
        void findByStatusOrderByPriorityDescNameAsc_WhenSamePriority_ShouldOrderByName() {
            adminRole.setPriority(1); // Same priority as userRole
            entityManager.persist(adminRole);
            entityManager.flush();

            List<Role> roles = roleRepository.findByStatusOrderByPriorityDescNameAsc(1);
            
            assertThat(roles).hasSize(2);
            assertThat(roles.get(0).getName()).isEqualTo(RoleName.ADMIN); // ADMIN comes before USER
            assertThat(roles.get(1).getName()).isEqualTo(RoleName.USER);
        }

        @Test
        void findByStatusOrderByPriorityDescNameAsc_WithInactiveRoles_ShouldExclude() {
            userRole.setStatus(0);
            entityManager.persist(userRole);
            entityManager.flush();

            List<Role> roles = roleRepository.findByStatusOrderByPriorityDescNameAsc(1);
            
            assertThat(roles).hasSize(1)
                           .extracting("name")
                           .containsExactly(RoleName.ADMIN);
        }
    }

    @Nested
    class CountQueries {
        @Test
        void countActiveUsers_WhenNoActiveUsers_ShouldReturnZero() {
            long count = roleRepository.countActiveUsers(1);
            
            assertThat(count).isZero();
        }

        @Test
        void countByStatus_WhenInactive_ShouldReturnZero() {
            long count = roleRepository.countByStatus(0);
            
            assertThat(count).isZero();
        }
    }
}
