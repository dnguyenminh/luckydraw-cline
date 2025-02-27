package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
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
        if (roleRepository.existsByName(request.getName())) {
            throw new InvalidOperationException("Role already exists: " + request.getName());
        }

        Role role = roleMapper.toEntity(request);
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
    public List<RoleDTO.Response> getAllRoles() {
        return roleMapper.toResponseList(roleRepository.findAll());
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = findRoleById(id);
        if (!role.getUsers().isEmpty()) {
            throw new InvalidOperationException("Cannot delete role with assigned users");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDTO.Response assignRolesToUser(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Set<Role> roles = roleNames.stream()
                .map(this::findOrCreateRole)
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user = userRepository.save(user);
        
        return roleMapper.toResponse(user.getRoles().iterator().next());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Summary> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return roleMapper.toSummaryList(user.getRoles().stream().toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, RoleName roleName) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == roleName))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyRole(Long userId, Set<RoleName> roleNames) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> roleNames.contains(role.getName())))
                .orElse(false);
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
    }

    private Role findOrCreateRole(RoleName name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    private boolean isHigherRole(RoleName role1, RoleName role2) {
        return role1.ordinal() < role2.ordinal();
    }
}
