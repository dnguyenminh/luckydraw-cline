package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.exception.BusinessException;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role adminRole;
    private Role staffRole;
    private RoleDTO.Response adminRoleResponse;
    private RoleDTO.Response staffRoleResponse;
    private RoleDTO.CreateRequest createRequest;
    private RoleDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName(RoleName.ROLE_ADMIN);
        adminRole.setDescription("Admin Role");

        staffRole = new Role();
        staffRole.setId(2L);
        staffRole.setName(RoleName.ROLE_STAFF);
        staffRole.setDescription("Staff Role");

        adminRoleResponse = RoleDTO.Response.builder()
            .id(1L)
            .name(RoleName.ROLE_ADMIN)
            .description("Admin Role")
            .build();

        staffRoleResponse = RoleDTO.Response.builder()
            .id(2L)
            .name(RoleName.ROLE_STAFF)
            .description("Staff Role")
            .build();

        createRequest = RoleDTO.CreateRequest.builder()
            .name(RoleName.ROLE_ADMIN)
            .description("Admin Role")
            .build();

        updateRequest = RoleDTO.UpdateRequest.builder()
            .description("Updated Admin Role")
            .build();
    }

    @Test
    void create_ValidRequest_ShouldCreateRole() {
        when(roleMapper.toEntity(any(RoleDTO.CreateRequest.class))).thenReturn(adminRole);
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(adminRoleResponse);

        RoleDTO.Response result = roleService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adminRole.getId());
        assertThat(result.getName()).isEqualTo(adminRole.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void create_NullRoleName_ShouldThrowException() {
        createRequest.setName(null);
        when(roleMapper.toEntity(any(RoleDTO.CreateRequest.class))).thenReturn(adminRole);

        assertThatThrownBy(() -> roleService.create(createRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Role name cannot be null");
    }

    @Test
    void findByName_ExistingRole_ShouldReturnRole() {
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> result = roleService.findByName(RoleName.ROLE_ADMIN);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleName.ROLE_ADMIN);
    }

    @Test
    void findByNames_ExistingRoles_ShouldReturnRoles() {
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_ADMIN, RoleName.ROLE_STAFF);
        when(roleRepository.findByNameIn(roleNames)).thenReturn(Set.of(adminRole, staffRole));

        Set<Role> result = roleService.findByNames(roleNames);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder(RoleName.ROLE_ADMIN, RoleName.ROLE_STAFF);
    }

    @Test
    void findAllActive_ShouldReturnActiveRoles() {
        when(roleRepository.findAllActive()).thenReturn(Set.of(adminRole, staffRole));

        Set<Role> result = roleService.findAllActive();

        assertThat(result).hasSize(2);
        verify(roleRepository).findAllActive();
    }

    @Test
    void findAllSummaries_ShouldReturnPageOfSummaries() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Role> rolePage = new PageImpl<>(List.of(adminRole, staffRole));
        RoleDTO.Summary adminSummary = RoleDTO.Summary.builder()
            .id(1L)
            .name(RoleName.ROLE_ADMIN)
            .build();
        RoleDTO.Summary staffSummary = RoleDTO.Summary.builder()
            .id(2L)
            .name(RoleName.ROLE_STAFF)
            .build();

        when(roleRepository.findAll(pageRequest)).thenReturn(rolePage);
        when(roleMapper.toSummary(adminRole)).thenReturn(adminSummary);
        when(roleMapper.toSummary(staffRole)).thenReturn(staffSummary);

        Page<RoleDTO.Summary> result = roleService.findAllSummaries(pageRequest);

        assertThat(result.getContent()).hasSize(2);
        verify(roleRepository).findAll(pageRequest);
    }

    @Test
    void existsByName_ExistingRole_ShouldReturnTrue() {
        when(roleRepository.existsByName(RoleName.ROLE_ADMIN)).thenReturn(true);

        boolean result = roleService.existsByName(RoleName.ROLE_ADMIN);

        assertThat(result).isTrue();
    }

    @Test
    void existsActiveByName_ActiveRole_ShouldReturnTrue() {
        when(roleRepository.existsActiveByName(RoleName.ROLE_ADMIN)).thenReturn(true);

        boolean result = roleService.existsActiveByName(RoleName.ROLE_ADMIN);

        assertThat(result).isTrue();
    }

    @Test
    void getHighestParticipantRole_ShouldReturnHighestRole() {
        when(roleRepository.findByParticipantId(1L)).thenReturn(Set.of(adminRole, staffRole));

        Optional<Role> result = roleService.getHighestParticipantRole(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleName.ROLE_ADMIN);
    }

    @Test
    void getHighestUserRole_ShouldReturnHighestRole() {
        when(roleRepository.findByUserId(1L)).thenReturn(Set.of(staffRole, adminRole));

        Optional<Role> result = roleService.getHighestUserRole(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleName.ROLE_ADMIN);
    }
}
