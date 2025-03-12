package vn.com.fecredit.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.service.TokenBlacklistService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tokens/blacklist")
@RequiredArgsConstructor
@Tag(name = "Token Blacklist", description = "Token blacklist management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class TokenBlacklistController {

    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Get tokens expiring soon", description = "Get all blacklisted tokens that will expire within the next hours")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved expiring tokens"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/expiring")
    public ResponseEntity<vn.com.fecredit.app.config.ApiResponse<List<BlacklistedToken>>> getExpiringTokens(
            @Parameter(description = "Hours until expiration", example = "24")
            @RequestParam(defaultValue = "24") int hours) {
        long now = Instant.now().toEpochMilli();
        long until = now + (hours * 3600000L);
        List<BlacklistedToken> tokens = tokenBlacklistService.getTokensExpiringBetween(now, until);

        return ResponseEntity.ok(
                vn.com.fecredit.app.config.ApiResponse.success("Successfully retrieved expiring tokens", tokens)
        );
    }

    @Operation(summary = "Get tokens by user", description = "Get all tokens revoked by a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user's revoked tokens"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/by-user/{username}")
    public ResponseEntity<vn.com.fecredit.app.config.ApiResponse<List<String>>> getTokensByUser(
            @Parameter(description = "Username who revoked the tokens", example = "admin")
            @PathVariable String username) {
        List<String> tokens = tokenBlacklistService.getTokensRevokedBy(username);

        return ResponseEntity.ok(
                vn.com.fecredit.app.config.ApiResponse.success(
                        "Successfully retrieved tokens revoked by " + username, 
                        tokens
                )
        );
    }

    @Operation(summary = "Get token count by type", description = "Get count of blacklisted tokens by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved token count"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/count")
    public ResponseEntity<vn.com.fecredit.app.config.ApiResponse<Long>> getTokenCount(
            @Parameter(description = "Whether to count refresh tokens or access tokens", example = "true")
            @RequestParam(defaultValue = "false") boolean refreshTokens) {
        long count = tokenBlacklistService.countByType(refreshTokens);

        return ResponseEntity.ok(
                vn.com.fecredit.app.config.ApiResponse.success(
                        "Successfully retrieved token count", 
                        count
                )
        );
    }

    @Operation(summary = "Remove token from blacklist", description = "Remove a specific token from the blacklist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed token"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Token not found in blacklist")
    })
    @DeleteMapping("/{token}")
    public ResponseEntity<vn.com.fecredit.app.config.ApiResponse<Void>> removeToken(
            @Parameter(description = "Token to remove from blacklist")
            @PathVariable String token) {
        boolean removed = tokenBlacklistService.removeFromBlacklist(token);

        return ResponseEntity.ok(
                vn.com.fecredit.app.config.ApiResponse.<Void>success(
                        removed ? "Token removed from blacklist" : "Token not found in blacklist",
                        null
                )
        );
    }

    @Operation(summary = "Clean up expired tokens", description = "Remove all expired tokens from the blacklist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully cleaned up expired tokens"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/expired")
    public ResponseEntity<vn.com.fecredit.app.config.ApiResponse<Integer>> cleanupExpiredTokens() {
        int count = tokenBlacklistService.cleanupExpiredTokens();

        return ResponseEntity.ok(
                vn.com.fecredit.app.config.ApiResponse.success(
                        "Successfully cleaned up expired tokens",
                        count
                )
        );
    }
}
