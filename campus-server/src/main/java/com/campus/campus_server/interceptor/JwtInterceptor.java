package com.campus.campus_server.interceptor;

import com.campus.campus_server.common.ApiResponse;
import com.campus.campus_server.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            return unauthorized(response, "请先登录");
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtUtil.parseToken(token);

            request.setAttribute("userId", claims.getSubject());
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));

            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(response, "登录令牌无效或已过期");
        }
    }

    private boolean unauthorized(
            HttpServletResponse response,
            String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> result = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));

        return false;
    }
}