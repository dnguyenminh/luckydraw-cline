package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.mapper.UserMapper;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.UserService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new UserSecurityDTO(user);
    }

    @Override
    public UserDTO.Response create(UserDTO.CreateRequest request) {
        User user = userMapper.toEntity(request);
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(request.getRoles()));
            user.setRoles(roles);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserDTO.Response update(Long id, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        userMapper.updateEntity(user, request);
        
        if (request.getRoles() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(request.getRoles()));
            user.setRoles(roles);
        }
        
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.updateStatus(id, 0);
    }

    @Override
    public void activate(Long id) {
        userRepository.updateStatus(id, 1);
    }

    @Override
    public void deactivate(Long id) {
        userRepository.updateStatus(id, 0);
    }

    @Override
    public UserDTO.Response getById(Long id) {
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    @Override
    public UserDTO.Response getByUsername(String username) {
        return userMapper.toResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    @Override
    public Page<UserDTO.Response> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public List<UserDTO.Summary> getAllByStatus(int status) {
        return userMapper.toSummaryList(userRepository.findAllByStatus(status));
    }

    @Override
    public Page<UserDTO.Summary> getAllByStatus(int status, Pageable pageable) {
        return userRepository.findAllByStatus(status, pageable).map(userMapper::toSummary);
    }

    @Override
    public long countByStatus(int status) {
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
    public void assignRoles(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(roleNames));
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public void removeRoles(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getRoles().removeIf(role -> roleNames.contains(role.getName()));
        userRepository.save(user);
    }

    @Override
    public Set<RoleName> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    @Override
    public boolean hasAnyRole(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getRoles().stream()
                .anyMatch(role -> roleNames.contains(role.getName()));
    }

    @Override 
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setMetadata("lastLogin=" + LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Long userId, String token, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // TODO: Validate reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // TODO: Generate and send reset token
    }

    @Override
    public void updateStatus(Long userId, int status) {
        userRepository.updateStatus(userId, status);
    }

    @Override
    public List<UserDTO.Summary> findActiveByRole(RoleName roleName) {
        return userMapper.toSummaryList(
            userRepository.findActiveByRole(roleName)
        );
    }

    @Override
    public List<UserDTO.Summary> findActiveByRoles(Set<RoleName> roleNames) {
        return userMapper.toSummaryList(
            userRepository.findActiveByRoles(roleNames)
        );
    }

    @Override
    public Optional<UserDTO.Response> findActiveByEmail(String email) {
        return userRepository.findActiveByEmail(email)
                .map(userMapper::toResponse);
    }

    @Override
    public List<UserDTO.Summary> findAllEnabledAndActive() {
        return userMapper.toSummaryList(userRepository.findAllActive());
    }

    @Override
    public long countEnabledAndActive() {
        return userRepository.countActive();
    }

    @Override
    public void validatePasswordResetToken(String token) {
        // TODO: Implement password reset token validation
    }

    @Override
    public void lockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.lockAccount(null); // Lock indefinitely
        userRepository.save(user);
    }

    @Override
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.unlockAccount();
        userRepository.save(user);
    }

    @Override
    public void expireCredentials(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setCredentialsNonExpired(false);
        userRepository.save(user);
    }

    @Override
    public void renewCredentials(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }
}
