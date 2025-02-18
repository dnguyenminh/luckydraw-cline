package vn.com.fecredit.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import vn.com.fecredit.app.dto.AuthRequest;
import vn.com.fecredit.app.dto.AuthResponse;
import vn.com.fecredit.app.model.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.mapper.UserMapper;

/**
 * Controller handling authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // Create authentication token from request
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        );

        // Authenticate using AuthenticationManager
        authentication = authenticationManager.authenticate(authentication);

        // If we get here, authentication was successful
        User user = (User) authentication.getPrincipal();

        // Create response with token and user details
        AuthResponse response = AuthResponse.builder()
            .token(jwtService.generateToken(user))
            .tokenType("Bearer")
            .user(userMapper.toDto(user))
            .build();

        return ResponseEntity.ok(response);
    }
}
