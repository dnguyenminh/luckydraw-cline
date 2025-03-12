package vn.com.fecredit.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.service.JwtService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation and validation.
 * Handles token creation, parsing, and validation for authentication.
 */
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Extracts the username from a JWT token.
     * 
     * @param token The JWT token
     * @return The username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using the provided claims resolver function.
     * 
     * @param <T> The type of the claim to extract
     * @param token The JWT token
     * @param claimsResolver The function to extract the desired claim
     * @return The extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the specified user.
     * 
     * @param userDetails The user details
     * @return The generated JWT token
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        if (userDetails instanceof UserSecurityDTO) {
            return generateToken(new HashMap<>(), (UserSecurityDTO) userDetails);
        }
        throw new IllegalArgumentException("UserDetails must be of type UserSecurityDTO");
    }
    
    public String generateToken(UserSecurityDTO userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with additional claims for the specified user.
     * 
     * @param extraClaims Additional claims to include in the token
     * @param userDetails The user details
     * @return The generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserSecurityDTO userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generates a refresh token for the specified user.
     * 
     * @param userDetails The user details
     * @return The generated refresh token
     */
    public String generateRefreshToken(UserSecurityDTO userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Builds a JWT token with the specified claims, user details, and expiration time.
     * 
     * @param extraClaims Additional claims to include in the token
     * @param userDetails The user details
     * @param expiration The token expiration time in milliseconds
     * @return The built JWT token
     */
    private String buildToken(Map<String, Object> extraClaims, UserSecurityDTO userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a token is valid for the specified user details.
     * 
     * @param token The JWT token
     * @param userDetails The user details
     * @return True if the token is valid, false otherwise
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    @Override
    public boolean isTokenValid(String token, UserSecurityDTO userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if a token is expired.
     * 
     * @param token The JWT token
     * @return True if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token The JWT token
     * @return The expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token The JWT token
     * @return All claims from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Gets the signing key for JWT token verification.
     * 
     * @return The signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }
    
    @Override
    public String extractCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}