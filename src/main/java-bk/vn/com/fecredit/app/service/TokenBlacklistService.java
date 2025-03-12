package vn.com.fecredit.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import vn.com.fecredit.app.entity.BlacklistedToken;

import java.util.List;

public interface TokenBlacklistService {
    
    /**
     * Add a token to the blacklist
     * @param token The token to blacklist
     * @param isRefreshToken Whether this is a refresh token
     * @param expirationTime Token expiration time in milliseconds
     */
    void blacklist(String token, boolean isRefreshToken, Long expirationTime);
    
    /**
     * Add a token to the blacklist with additional metadata
     * @param token The token to blacklist
     * @param isRefreshToken Whether this is a refresh token
     * @param expirationTime Token expiration time in milliseconds
     * @param revokedBy Username of the user who revoked the token
     * @param reason Reason for blacklisting
     */
    void blacklist(String token, boolean isRefreshToken, Long expirationTime, String revokedBy, String reason);
    
    /**
     * Check if a token is blacklisted
     * @param token The token to check
     * @return true if the token is blacklisted and not expired
     */
    boolean isBlacklisted(String token);
    
    /**
     * Remove a token from the blacklist
     * @param token The token to remove
     * @return true if the token was removed
     */
    boolean removeFromBlacklist(String token);
    
    /**
     * Get all blacklisted tokens that expire within a time range
     * @param startTime Start of time range in milliseconds
     * @param endTime End of time range in milliseconds
     * @return List of blacklisted tokens
     */
    List<BlacklistedToken> getTokensExpiringBetween(Long startTime, Long endTime);
    
    /**
     * Get tokens blacklisted by a specific user
     * @param username Username of the user
     * @return List of token hashes
     */
    List<String> getTokensRevokedBy(String username);
    
    /**
     * Count total blacklisted tokens by type
     * @param isRefreshToken Whether to count refresh tokens or access tokens
     * @return Number of blacklisted tokens
     */
    long countByType(boolean isRefreshToken);
    
    /**
     * Get list of expired tokens
     * @return List of expired blacklisted tokens
     */
    List<BlacklistedToken> getExpiredTokens();
    
    /**
     * Clean up expired tokens from the blacklist
     * Runs automatically on schedule defined in application.yml
     * @return Number of tokens removed
     */
    @Scheduled(cron = "${token.blacklist.cleanup-cron:0 0 * * * *}")
    int cleanupExpiredTokens();
    
    /**
     * Hash a token for storage
     * @param token The token to hash
     * @return Hashed token value
     */
    String hashToken(String token);
}
