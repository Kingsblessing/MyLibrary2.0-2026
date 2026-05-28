package com.example.library.config;

import com.example.library.common.ApiResponse;
import com.example.library.common.ErrorCodes;
import com.example.library.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Allow preflight CORS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("X-Token");
        if (token == null || token.isBlank()) {
            writeError(response, ErrorCodes.UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        Long userId = UserServiceImpl.getUserIdFromToken(token);
        if (userId == null) {
            writeError(response, ErrorCodes.UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        request.setAttribute("userId", userId);
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
