package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.Permission;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private Role role;
    private RoleDTO.Response roleResponse;
    private RoleDTO.CreateRequest createRequest;
    private RoleDTO.UpdateRequest updateRequest;
    private User user;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .code("USER")
                .status(1)
                .users(new HashSet<>())
                .permissions(new HashSet<>())
                .build();

        roleResponse = RoleDTO.Response.builder()
                .id(1L)
                .name(RoleName.USER)
                .code("USER")
                .status(1)
                .permissions(new HashSet<>())
                .build();

        createRequest = RoleDTO.CreateRequest.builder()
                .name(RoleName.USER)
                .code("USER")
                .permissions(new HashSet<>())
                .build();

        updateRequest = RoleDTO.UpdateRequest.builder()
                .name(RoleName.USER)
                .permissions(new HashSet<>())
                .build();

        user = User.builder()
                .id(1L)
                .username("testUser")
                .roles(new HashSet<>())
                .build();
    }

    @Nested
    class CreateOperations {
        @Test
        void createRole_WithNullPermissions_ShouldCreateRoleWithEmptyPermissions() {
            createRequest.setPermissions(null);
            when(roleMapper.toEntity(createRequest)).thenReturn(role);
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.createRole(createRequest);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getPermissions()).isEmpty();
        }

        @Test
        void createRole_ShouldSetActiveStatus() {
            when(roleMapper.toEntity(createRequest)).thenReturn(role);
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.createRole(createRequest);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getStatus()).isEqualTo(1);
        }
    }

    @Nested
    class UpdateOperations {
        @Test
        void updateRole_WithNullFields_ShouldNotUpdateExistingValues() {
            updateRequest.setName(null);
            updateRequest.setPermissions(null);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.updateRole(1L, updateRequest);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getName()).isEqualTo(RoleName.USER);
        }

        @Test
        void updateRole_ShouldNotChangeStatus() {
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.updateRole(1L, updateRequest);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getStatus()).isEqualTo(1);
        }
    }

    @Nested
    class StatusOperations {
        @Test
        void activateRole_WhenAlreadyActive_ShouldNotModify() {
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.activateRole(1L);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getStatus()).isEqualTo(1);
        }

        @Test
        void deactivateRole_WithAssignedUsers_ShouldStillDeactivate() {
            role.getUsers().add(user);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.deactivateRole(1L);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getStatus()).isEqualTo(0);
        }
    }

    @Nested
    class QueryOperations {
        @Test
        void getRoles_WithEmptyResult_ShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Role> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(roleRepository.findByNameAndStatus(RoleName.USER, 1, pageable)).thenReturn(emptyPage);

            Page<RoleDTO.Summary> result = roleService.getRoles(RoleName.USER, 1, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void findActiveByNames_WithEmptySet_ShouldReturnEmptySet() {
            Set<RoleName> names = Collections.emptySet();

            Set<RoleDTO.Summary> result = roleService.findActiveByNames(names);

            assertThat(result).isEmpty();
            verify(roleRepository, never()).findByNameInAndStatus(any(), anyInt());
        }

        @Test
        void findActiveByNames_WithValidNames_ShouldReturnRoles() {
            Set<RoleName> names = Set.of(RoleName.USER);
            when(roleRepository.findByNameInAndStatus(eq(names), eq(1)))
                .thenReturn(List.of(role));
            when(roleMapper.toSummary(role))
                .thenReturn(RoleDTO.Summary.builder().build());

            Set<RoleDTO.Summary> result = roleService.findActiveByNames(names);

            assertThat(result).hasSize(1);
            verify(roleRepository).findByNameInAndStatus(names, 1);
        }
    }

    @Nested
    class UserOperations {
        @Test
        void assignRole_WhenUserNotFound_ShouldThrowException() {
            RoleDTO.Assignment request = new RoleDTO.Assignment(1L, 1L);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.assignRole(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 1");
        }

        @Test
        void countUsers_WhenRoleNotFound_ShouldThrowException() {
            when(roleRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> roleService.countUsers(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 1");
        }
    }

    @Nested
    class PermissionOperations {
        @Test
        void updatePermissions_WithNullPermissions_ShouldClearExistingPermissions() {
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            role.setPermissions(Set.of("EXISTING_PERMISSION"));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.updatePermissions(1L, null);

            verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getPermissions()).isEmpty();
        }

        @Test
        void grantPermission_WithEmptyPermissions_ShouldClearPermissions() {
            RoleDTO.PermissionUpdate request = new RoleDTO.PermissionUpdate(Collections.emptySet());
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any(Role.class))).thenReturn(role);
            when(roleMapper.toResponse(role)).thenReturn(roleResponse);

            roleService.grantPermission(1L, request);

            verify(roleMapper).updateFromPermissionUpdate(eq(role), eq(request));
        }
    }
}
