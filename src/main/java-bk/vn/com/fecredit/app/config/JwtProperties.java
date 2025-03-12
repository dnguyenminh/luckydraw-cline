package vn.com.fecredit.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessExpiration;
    private long refreshExpiration;
    private String issuer;
    private String audience;
    private boolean httpOnly;
    private boolean secure;
    private String path;
    private String sameSite;
}
