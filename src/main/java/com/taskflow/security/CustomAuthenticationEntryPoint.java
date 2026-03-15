package com.taskflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (response.isCommitted()) {
            return;
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("UNAUTHORIZED")
                .message("Unauthorized")
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .timestamp(LocalDateTime.now())
                .traceId(request.getHeader("X-Trace-Id"))
                .errors(null)
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
