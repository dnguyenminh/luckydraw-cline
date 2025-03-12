package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.enums.RoleName;

class RoleMapperTest {

    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RoleMapper();
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        RoleDTO.CreateRequest request = RoleDTO.CreateRequest.builder()
                .name(RoleName.ADMIN)
                .description("Administrator Role")
                .build();

        // When
        Role role = roleMapper.toEntity(request);

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(request.getName());
        assertThat(role.getDescription()).isEqualTo(request.getDescription());
    }

    @Test
    void toEntity_WhenRequestIsNull_ShouldReturnNull() {
        assertThat(roleMapper.toEntity(null)).isNull();
    }

    @Test
    void updateEntity_ShouldUpdateFields() {
        // Given
        Role role = Role.builder()
                .name(RoleName.ADMIN)
                .description("Old Description")
                .build();

        RoleDTO.UpdateRequest request = RoleDTO.UpdateRequest.builder()
                .description("New Description")
                .build();

        // When
        roleMapper.updateEntity(request, role);

        // Then
        assertThat(role.getDescription()).isEqualTo("New Description");
        assertThat(role.getName()).isEqualTo(RoleName.ADMIN); // Should not change
    }

    @Test
    void updateEntity_WhenRequestOrRoleIsNull_ShouldDoNothing() {
        Role role = Role.builder().name(RoleName.ADMIN).build();
        roleMapper.updateEntity(null, role); // Should not throw
        roleMapper.updateEntity(new RoleDTO.UpdateRequest(), null); // Should not throw
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.ADMIN)
                .description("Administrator Role")
                .version(1L)
                .build();
        role.setStatus(EntityStatus.ACTIVE);
        role.activate();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setCreatedBy(1L);
        role.setUpdatedBy(2L);

        // When
        RoleDTO.Response response = roleMapper.toResponse(role);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(role.getId());
        assertThat(response.getName()).isEqualTo(role.getName());
        assertThat(response.getDescription()).isEqualTo(role.getDescription());
        assertThat(response.getStatus()).isEqualTo(role.getStatus());
        assertThat(response.isActive()).isTrue();
        assertThat(response.getVersion()).isEqualTo(role.getVersion());
        assertThat(response.getCreatedAt()).isEqualTo(role.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(role.getUpdatedAt());
        assertThat(response.getCreatedBy()).isEqualTo("1");
        assertThat(response.getUpdatedBy()).isEqualTo("2");
    }

    @Test
    void toResponse_WhenRoleIsNull_ShouldReturnNull() {
        assertThat(roleMapper.toResponse(null)).isNull();
    }

    @Test
    void toSummary_ShouldMapAllFields() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.ADMIN)
                .description("Administrator Role")
                .build();
        role.setStatus(EntityStatus.ACTIVE);
        role.activate();

        // When
        RoleDTO.Summary summary = roleMapper.toSummary(role);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(role.getId());
        assertThat(summary.getName()).isEqualTo(role.getName());
        assertThat(summary.getDescription()).isEqualTo(role.getDescription());
        assertThat(summary.getStatus()).isEqualTo(role.getStatus());
        assertThat(summary.isActive()).isTrue();
    }

    @Test
    void toPrivileges_ShouldMapCorrectly() {
        // Given
        Role adminRole = Role.builder().name(RoleName.ADMIN).build();
        Role userRole = Role.builder().name(RoleName.USER).build();

        // When
        RoleDTO.Privileges adminPrivileges = roleMapper.toPrivileges(adminRole);
        RoleDTO.Privileges userPrivileges = roleMapper.toPrivileges(userRole);

        // Then
        assertThat(adminPrivileges.isCanCreateUsers()).isTrue();
        assertThat(adminPrivileges.isCanManageRoles()).isTrue();
        assertThat(adminPrivileges.isCanAccessAdmin()).isTrue();

        assertThat(userPrivileges.isCanCreateUsers()).isFalse();
        assertThat(userPrivileges.isCanManageRoles()).isFalse();
        assertThat(userPrivileges.isCanViewReports()).isTrue();
    }

    @Test
    void toResponseWithUserCount_ShouldIncludeUserCount() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.ADMIN)
                .description("Administrator Role")
                .build();
        role.setStatus(EntityStatus.ACTIVE);
        role.activate();

        // When
        RoleDTO.Response response = roleMapper.toResponseWithUserCount(role, 5);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserCount()).isEqualTo(5);
    }

    @Test
    void toSummaryWithUserCount_ShouldIncludeUserCount() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.ADMIN)
                .description("Administrator Role")
                .build();
        role.setStatus(EntityStatus.ACTIVE);
        role.activate();

        // When
        RoleDTO.Summary summary = roleMapper.toSummaryWithUserCount(role, 5);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getUserCount()).isEqualTo(5);
    }
}
