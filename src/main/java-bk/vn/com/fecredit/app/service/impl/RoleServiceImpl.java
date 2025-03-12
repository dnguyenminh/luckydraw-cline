package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.RoleService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleDTO.Response createRole(RoleDTO.CreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DataIntegrityViolationException("Role name already exists: " + request.getName());
        }
        Role role = roleMapper.toEntity(request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response updateRole(Long id, RoleDTO.UpdateRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        roleMapper.updateEntity(role, request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO.Response getRole(Long id) {
        return roleRepository.findById(id)
            .map(roleMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
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
    public Page<RoleDTO.Summary> getRoles(RoleName name, EntityStatus status, Pageable pageable) {
        return roleRepository.findAllFiltered(name, status, pageable)
            .map(roleMapper::toSummary);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        if (role.getName() == RoleName.ADMIN) {
            throw new IllegalStateException("Cannot delete the ADMIN role");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response activateRole(Long id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.setStatus(EntityStatus.ACTIVE);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response deactivateRole(Long id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        if (role.getName() == RoleName.ADMIN) {
            throw new IllegalStateException("Cannot deactivate the ADMIN role");
        }
        role.setStatus(EntityStatus.INACTIVE);
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
        return roleRepository.existsActiveByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleDTO.Summary> getUserRoles(Long userId) {
        return roleRepository.findActiveByUserId(userId).stream()
            .map(roleMapper::toSummary)
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public RoleDTO.Response assignRole(RoleDTO.Assignment request) {
        Role role = roleRepository.findByName(request.getRoleName())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        
        role.addUser(user);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response unassignRole(RoleDTO.Assignment request) {
        Role role = roleRepository.findByName(request.getRoleName())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (role.getName() == RoleName.ADMIN && user.getRoles().size() == 1) {
            throw new IllegalStateException("Cannot remove the last ADMIN role from a user");
        }

        role.removeUser(user);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response updatePermissions(Long id, Set<String> permissions) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.setPermissions(permissions);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response grantPermission(Long id, RoleDTO.PermissionUpdate request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.addPermission(request.getPermission());
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response revokePermission(Long id, RoleDTO.PermissionUpdate request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.removePermission(request.getPermission());
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Summary> getAssignableRoles() {
        return roleRepository.findAllAssignable().stream()
            .map(roleMapper::toSummary)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers(Long roleId) {
        return roleRepository.countUsersByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleDTO.Summary> findActiveByNames(Set<RoleName> names) {
        return roleRepository.findActiveByNames(names).stream()
            .map(roleMapper::toSummary)
            .collect(Collectors.toSet());
    }
}
