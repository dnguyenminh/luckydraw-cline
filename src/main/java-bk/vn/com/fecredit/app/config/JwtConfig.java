package vn.com.fecredit.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {

    private String secret;
    private long expiration = 86400000; // 24 hours in milliseconds
    private long refreshTokenExpiration = 604800000; // 7 days in milliseconds
    private String issuer = "FECredit";
    private String audience = "FECredit App";
    private String type = "type";
    private String accessToken = "ACCESS";
    private String refreshToken = "REFRESH";
    private String authorities = "authorities";
    private long refreshThreshold = 300000; // 5 minutes in milliseconds
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";

    public String getTokenPrefix() {
        return tokenPrefix.trim();
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    
    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }
}
