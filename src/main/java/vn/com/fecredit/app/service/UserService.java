package vn.com.fecredit.app.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import vn.com.fecredit.app.entity.User;

public interface UserService extends UserDetailsService {
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    User save(User user);
    
    void updatePassword(User user, String newPassword);
    
    void updateLastLoginDate(User user);
}
