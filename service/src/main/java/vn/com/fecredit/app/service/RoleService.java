package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleService {

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response createRole(RoleDTO.CreateRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response updateRole(Long id, RoleDTO.UpdateRequest request);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    RoleDTO.Response getRole(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    Optional<RoleDTO.Response> findByName(RoleName name);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    List<RoleDTO.Response> findByNames(Set<RoleName> names);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    List<RoleDTO.Summary> getAllRoles();

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    Page<RoleDTO.Summary> getRoles(
        RoleName name,
        int status,
        Pageable pageable
    );

    @PreAuthorize("hasRole('ADMIN')")
    void deleteRole(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response activateRole(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response deactivateRole(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    boolean existsByName(RoleName name);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    boolean existsActiveByName(RoleName name);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    Set<RoleDTO.Summary> getUserRoles(Long userId);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response assignRole(RoleDTO.Assignment request);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response unassignRole(RoleDTO.Assignment request);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response updatePermissions(Long id, Set<String> permissions);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response grantPermission(Long id, RoleDTO.PermissionUpdate request);

    @PreAuthorize("hasRole('ADMIN')")
    RoleDTO.Response revokePermission(Long id, RoleDTO.PermissionUpdate request);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    List<RoleDTO.Summary> getAssignableRoles();

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    long countUsers(Long roleId);

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    Set<RoleDTO.Summary> findActiveByNames(Set<RoleName> names);
}
