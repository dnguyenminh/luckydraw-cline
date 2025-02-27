package vn.com.fecredit.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.dto.common.DataResponse;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.service.RoleService;

import java.util.Set;

@Tag(name = "Role Management", description = "APIs for managing roles")
@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create new role")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DataResponse<RoleDTO.Response>> create(
            @Valid @RequestBody RoleDTO.CreateRequest request) {
        RoleDTO.Response response = roleService.create(request);
        return ResponseEntity.ok(DataResponse.of(response));
    }

    @Operation(summary = "Update role by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DataResponse<RoleDTO.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO.UpdateRequest request) {
        RoleDTO.Response response = roleService.update(id, request);
        return ResponseEntity.ok(DataResponse.of(response));
    }

    @Operation(summary = "Get role by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<RoleDTO.Response>> getById(@PathVariable Long id) {
        RoleDTO.Response response = roleService.getById(id);
        return ResponseEntity.ok(DataResponse.of(response));
    }

    @Operation(summary = "Delete role by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all roles with pagination")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Page<RoleDTO.Summary>>> findAll(
            @ModelAttribute PageRequest pageRequest) {
        Page<RoleDTO.Summary> response = roleService.findAllSummaries(pageRequest.toSpringPageRequest());
        return ResponseEntity.ok(DataResponse.of(response));
    }

    @Operation(summary = "Get all active roles")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Set<Role>>> findAllActive() {
        Set<Role> response = roleService.findAllActive();
        return ResponseEntity.ok(DataResponse.of(response));
    }

    @Operation(summary = "Check if role exists by name")
    @GetMapping("/exists")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Boolean>> existsByName(
            @RequestParam RoleName name) {
        boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(DataResponse.of(exists));
    }

    @Operation(summary = "Assign roles to participant")
    @PostMapping("/assign/participant/{participantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRolesToParticipant(
            @PathVariable Long participantId,
            @RequestBody Set<RoleName> roleNames) {
        roleService.assignRolesToParticipant(participantId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove roles from participant")
    @PostMapping("/remove/participant/{participantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> removeRolesFromParticipant(
            @PathVariable Long participantId,
            @RequestBody Set<RoleName> roleNames) {
        roleService.removeRolesFromParticipant(participantId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get participant roles")
    @GetMapping("/participant/{participantId}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Set<Role>>> getParticipantRoles(
            @PathVariable Long participantId) {
        Set<Role> roles = roleService.findByParticipantId(participantId);
        return ResponseEntity.ok(DataResponse.of(roles));
    }

    @Operation(summary = "Check if participant has role")
    @GetMapping("/participant/{participantId}/has-role")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Boolean>> hasParticipantRole(
            @PathVariable Long participantId,
            @RequestParam RoleName roleName) {
        boolean hasRole = roleService.hasParticipantRole(participantId, roleName);
        return ResponseEntity.ok(DataResponse.of(hasRole));
    }

    @Operation(summary = "Assign roles to user")
    @PostMapping("/assign/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRolesToUser(
            @PathVariable Long userId,
            @RequestBody Set<RoleName> roleNames) {
        roleService.assignRolesToUser(userId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove roles from user")
    @PostMapping("/remove/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> removeRolesFromUser(
            @PathVariable Long userId,
            @RequestBody Set<RoleName> roleNames) {
        roleService.removeRolesFromUser(userId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user roles")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Set<Role>>> getUserRoles(
            @PathVariable Long userId) {
        Set<Role> roles = roleService.findByUserId(userId);
        return ResponseEntity.ok(DataResponse.of(roles));
    }

    @Operation(summary = "Check if user has role")
    @GetMapping("/user/{userId}/has-role")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DataResponse<Boolean>> hasUserRole(
            @PathVariable Long userId,
            @RequestParam RoleName roleName) {
        boolean hasRole = roleService.hasUserRole(userId, roleName);
        return ResponseEntity.ok(DataResponse.of(hasRole));
    }
}
