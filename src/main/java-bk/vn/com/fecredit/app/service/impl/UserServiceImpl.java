package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.UserMapper;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO.Response create(UserDTO.CreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidOperationException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidOperationException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Set<Role> roles = roleRepository.findByNameIn(request.getRoles());
        user.getRoles().addAll(roles);
        
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO.Response update(Long id, UserDTO.UpdateRequest request) {
        User user = findUserById(id);
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findUserById(id);
        user.setStatus(EntityStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        User user = findUserById(id);
        user.activate();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        User user = findUserById(id);
        user.deactivate();
        userRepository.save(user);
    }

    @Override
    public UserDTO.Response getById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Override
    public UserDTO.Response getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    public Page<UserDTO.Response> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public List<UserDTO.Summary> getAllByStatus(EntityStatus status) {
        return userRepository.findAllByStatus(status).stream()
                .map(userMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO.Summary> getAllByStatus(EntityStatus status, Pageable pageable) {
        return userRepository.findAllByStatus(status, pageable).map(userMapper::toSummary);
    }

    @Override
    public long countByStatus(EntityStatus status) {
        return userRepository.countByStatus(status);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsActiveByUsername(String username) {
        return userRepository.existsActiveByUsername(username);
    }

    @Override
    public boolean existsActiveByEmail(String email) {
        return userRepository.existsActiveByEmail(email);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, Set<RoleName> roleNames) {
        User user = findUserById(userId);
        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        user.getRoles().addAll(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRoles(Long userId, Set<RoleName> roleNames) {
        User user = findUserById(userId);
        Set<Role> rolesToRemove = user.getRoles().stream()
                .filter(role -> roleNames.contains(role.getName()))
                .collect(Collectors.toSet());
        user.getRoles().removeAll(rolesToRemove);
        userRepository.save(user);
    }

    @Override
    public Set<RoleName> getUserRoles(Long userId) {
        User user = findUserById(userId);
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasRole(Long userId, RoleName roleName) {
        User user = findUserById(userId);
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    @Override
    public boolean hasAnyRole(Long userId, Set<RoleName> roleNames) {
        User user = findUserById(userId);
        return user.getRoles().stream()
                .anyMatch(role -> roleNames.contains(role.getName()));
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String token, String newPassword) {
        User user = findUserById(userId);
        // TODO: Implement password reset token validation
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        // TODO: Implement password reset request logic
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, EntityStatus status) {
        User user = findUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public List<UserDTO.Summary> findActiveByRole(RoleName roleName) {
        return userRepository.findActiveByRole(roleName).stream()
                .map(userMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO.Summary> findActiveByRoles(Set<RoleName> roleNames) {
        return userRepository.findActiveByRoles(roleNames).stream()
                .map(userMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO.Response> findActiveByEmail(String email) {
        return userRepository.findActiveByEmail(email)
                .map(userMapper::toResponse);
    }

    @Override
    public List<UserDTO.Summary> findAllEnabledAndActive() {
        return userRepository.findAllEnabledAndActive().stream()
                .map(userMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public long countEnabledAndActive() {
        return userRepository.countEnabledAndActive();
    }

    @Override
    public void validatePasswordResetToken(String token) {
        // TODO: Implement token validation logic
    }

    @Override
    @Transactional
    public void lockAccount(Long userId) {
        User user = findUserById(userId);
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        User user = findUserById(userId);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void expireCredentials(Long userId) {
        User user = findUserById(userId);
        user.setCredentialsNonExpired(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void renewCredentials(Long userId) {
        User user = findUserById(userId);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
}
