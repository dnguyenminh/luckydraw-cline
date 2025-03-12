package vn.com.fecredit.app.service;

import org.springframework.security.core.userdetails.UserDetails;
import vn.com.fecredit.app.dto.UserSecurityDTO;

import java.util.Map;

/**
 * Service interface for JWT token generation and validation.
 * Handles token creation, parsing, and validation for authentication.
 */
public interface JwtService {

    /**
     * Extracts the username from a JWT token.
     * 
     * @param token The JWT token
     * @return The username extracted from the token
     */
    String extractUsername(String token);
    
    /**
     * Generates a JWT token for the specified user.
     * 
     * @param userDetails The user details
     * @return The generated JWT token
     */
    String generateToken(UserDetails userDetails);
    
    /**
     * Generates a JWT token with additional claims for the specified user.
     * 
     * @param extraClaims Additional claims to include in the token
     * @param userDetails The user details
     * @return The generated JWT token
     */
    String generateToken(Map<String, Object> extraClaims, UserSecurityDTO userDetails);
    
    /**
     * Generates a refresh token for the specified user.
     * 
     * @param userDetails The user details
     * @return The generated refresh token
     */
    String generateRefreshToken(UserSecurityDTO userDetails);
    
    /**
     * Validates if a token is valid for the specified user details.
     * 
     * @param token The JWT token
     * @param userDetails The user details
     * @return True if the token is valid, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
    
    /**
     * Validates if a token is valid for the specified user details.
     * 
     * @param token The JWT token
     * @param userDetails The user details
     * @return True if the token is valid, false otherwise
     */
    boolean isTokenValid(String token, UserSecurityDTO userDetails);
    
    /**
     * Gets the token expiration time in milliseconds.
     * 
     * @return The token expiration time
     */
    long getExpirationTime();
    
    /**
     * Extracts the username of the currently authenticated user.
     * 
     * @return The username of the current user
     */
    String extractCurrentUsername();
}
