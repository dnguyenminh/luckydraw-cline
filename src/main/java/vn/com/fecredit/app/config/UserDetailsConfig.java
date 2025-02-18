package vn.com.fecredit.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.com.fecredit.app.service.UserService;

/**
 * Configuration class for UserDetailsService.
 * Creates a primary UserDetailsService bean that delegates to UserService.
 */
@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UserService userService;

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return username -> userService.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}