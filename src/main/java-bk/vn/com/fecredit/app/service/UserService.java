package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.enums.RoleName;

public interface UserService extends UserDetailsService {

    UserDTO.Response create(UserDTO.CreateRequest request);

    UserDTO.Response update(Long id, UserDTO.UpdateRequest request);

    void delete(Long id);

    void activate(Long id);

    void deactivate(Long id);

    UserDTO.Response getById(Long id);

    UserDTO.Response getByUsername(String username);

    Page<UserDTO.Response> getAll(Pageable pageable);

    List<UserDTO.Summary> getAllByStatus(EntityStatus status);

    Page<UserDTO.Summary> getAllByStatus(EntityStatus status, Pageable pageable);

    long countByStatus(EntityStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsActiveByUsername(String username);

    boolean existsActiveByEmail(String email);

    void assignRoles(Long userId, Set<RoleName> roleNames);

    void removeRoles(Long userId, Set<RoleName> roleNames);

    Set<RoleName> getUserRoles(Long userId);

    boolean hasRole(Long userId, RoleName roleName);

    boolean hasAnyRole(Long userId, Set<RoleName> roleNames);

    void updateLastLogin(Long userId);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void resetPassword(Long userId, String token, String newPassword);

    void requestPasswordReset(String email);

    void updateStatus(Long userId, EntityStatus status);

    List<UserDTO.Summary> findActiveByRole(RoleName roleName);

    List<UserDTO.Summary> findActiveByRoles(Set<RoleName> roleNames);

    Optional<UserDTO.Response> findActiveByEmail(String email);

    List<UserDTO.Summary> findAllEnabledAndActive();

    long countEnabledAndActive();

    void validatePasswordResetToken(String token);

    void lockAccount(Long userId);

    void unlockAccount(Long userId);

    void expireCredentials(Long userId);

    void renewCredentials(Long userId);
}
