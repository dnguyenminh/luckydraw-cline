package vn.com.fecredit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.common.BaseResponse;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
            AuthenticationException authException) throws IOException, ServletException {
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String message = "Authentication failed";
        String errorCode = "AUTH001";

        if (authException instanceof BadCredentialsException) {
            message = "Invalid credentials";
            errorCode = "AUTH002";
        } else if (authException instanceof InsufficientAuthenticationException) {
            message = "Authentication token not provided or invalid";
            errorCode = "AUTH003";
        }

        response.setStatus(status);
        
        BaseResponse errorResponse = BaseResponse.error(message, errorCode);
        
        try (OutputStream out = response.getOutputStream()) {
            objectMapper.writeValue(out, errorResponse);
            out.flush();
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null) {
            url = url + pathInfo;
        }
        
        return url;
    }
}
