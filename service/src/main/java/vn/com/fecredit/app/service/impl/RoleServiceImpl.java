package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.RoleService;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleDTO.Response createRole(RoleDTO.CreateRequest request) {
        Role role = roleMapper.toEntity(request);
        role.setStatus(1); // Set active by default
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response updateRole(Long id, RoleDTO.UpdateRequest request) {
        Role role = findRoleById(id);
        roleMapper.updateEntity(role, request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO.Response getRole(Long id) {
        return roleMapper.toResponse(findRoleById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDTO.Response> findByName(RoleName name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Response> findByNames(Set<RoleName> names) {
        return roleRepository.findByNameIn(names).stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Summary> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO.Summary> getRoles(RoleName name, int status, Pageable pageable) {
        return roleRepository.findByNameAndStatus(name, status, pageable)
                .map(roleMapper::toSummary);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = findRoleById(id);
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role with assigned users");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response activateRole(Long id) {
        Role role = findRoleById(id);
        role.setStatus(1);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response deactivateRole(Long id) {
        Role role = findRoleById(id);
        role.setStatus(0);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(RoleName name) {
        return roleRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveByName(RoleName name) {
        return roleRepository.existsByNameAndStatus(name, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleDTO.Summary> getUserRoles(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return roleRepository.findByUsersId(userId).stream()
                .map(roleMapper::toSummary)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public RoleDTO.Response assignRole(RoleDTO.Assignment request) {
        Role role = findRoleById(request.getRoleId());
        User user = findUserById(request.getUserId());
        
        if (role.getStatus() == 0) {
            throw new IllegalStateException("Cannot assign inactive role");
        }
        
        roleMapper.handleRoleAssignment(role, user);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response unassignRole(RoleDTO.Assignment request) {
        Role role = findRoleById(request.getRoleId());
        User user = findUserById(request.getUserId());
        roleMapper.handleRoleUnassignment(role, user);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response updatePermissions(Long id, Set<String> permissions) {
        Role role = findRoleById(id);
        role.setPermissions(permissions);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response grantPermission(Long id, RoleDTO.PermissionUpdate request) {
        Role role = findRoleById(id);
        roleMapper.updateFromPermissionUpdate(role, request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response revokePermission(Long id, RoleDTO.PermissionUpdate request) {
        Role role = findRoleById(id);
        roleMapper.updateFromPermissionUpdate(role, request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Summary> getAssignableRoles() {
        return roleRepository.findByStatus(1).stream()
                .map(roleMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }
        return roleRepository.countUsersByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleDTO.Summary> findActiveByNames(Set<RoleName> names) {
        return roleRepository.findByNameInAndStatus(names, 1).stream()
                .map(roleMapper::toSummary)
                .collect(Collectors.toSet());
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
