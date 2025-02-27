package vn.com.fecredit.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("Incoming request: {} {} from {}", 
                request.getMethod(), 
                request.getRequestURI(),
                getClientIP(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object handler, Exception ex) {
        log.debug("Completed request: {} {} with status {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                response.getStatus());
        if (ex != null) {
            log.error("Request failed with exception: {}", ex.getMessage(), ex);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getMaskedToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (token.length() <= 10) {
            return "*".repeat(token.length());
        }
        return token.substring(0, 5) + "*".repeat(token.length() - 10) + token.substring(token.length() - 5);
    }
}
