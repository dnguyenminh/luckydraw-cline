package vn.com.fecredit.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.service.RoleService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<RoleDTO.Response> createRole(
            @Valid @RequestBody RoleDTO.CreateRequest request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing role")
    public ResponseEntity<RoleDTO.Response> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO.UpdateRequest request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a role")
    public ResponseEntity<RoleDTO.Response> activateRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.activateRole(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a role")
    public ResponseEntity<RoleDTO.Response> deactivateRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deactivateRole(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleDTO.Response> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRole(id));
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Find role by name")
    public ResponseEntity<RoleDTO.Response> findByName(
            @PathVariable RoleName name) {
        return roleService.findByName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all roles with pagination")
    public ResponseEntity<Page<RoleDTO.Summary>> getRoles(
            @Parameter(description = "Role name filter") @RequestParam(required = false) RoleName name,
            @Parameter(description = "Status filter") @RequestParam(required = false) EntityStatus status,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(roleService.getRoles(name, status, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all roles without pagination")
    public ResponseEntity<List<RoleDTO.Summary>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/assignable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all assignable roles")
    public ResponseEntity<List<RoleDTO.Summary>> getAssignableRoles() {
        return ResponseEntity.ok(roleService.getAssignableRoles());
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<RoleDTO.Response> assignRole(
            @Valid @RequestBody RoleDTO.Assignment request) {
        return ResponseEntity.ok(roleService.assignRole(request));
    }

    @PostMapping("/unassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unassign role from user")
    public ResponseEntity<RoleDTO.Response> unassignRole(
            @Valid @RequestBody RoleDTO.Assignment request) {
        return ResponseEntity.ok(roleService.unassignRole(request));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role permissions")
    public ResponseEntity<RoleDTO.Response> updatePermissions(
            @PathVariable Long id,
            @RequestBody Set<String> permissions) {
        return ResponseEntity.ok(roleService.updatePermissions(id, permissions));
    }

    @PostMapping("/{id}/permissions/grant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Grant permission to role")
    public ResponseEntity<RoleDTO.Response> grantPermission(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO.PermissionUpdate request) {
        return ResponseEntity.ok(roleService.grantPermission(id, request));
    }

    @PostMapping("/{id}/permissions/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke permission from role")
    public ResponseEntity<RoleDTO.Response> revokePermission(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO.PermissionUpdate request) {
        return ResponseEntity.ok(roleService.revokePermission(id, request));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user roles")
    public ResponseEntity<Set<RoleDTO.Summary>> getUserRoles(
            @PathVariable Long userId) {
        return ResponseEntity.ok(roleService.getUserRoles(userId));
    }

    @GetMapping("/{roleId}/users/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Count users with role")
    public ResponseEntity<Long> countUsers(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.countUsers(roleId));
    }
}
