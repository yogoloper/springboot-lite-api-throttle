package io.throttle.examples.controller;

import io.throttle.examples.model.User;
import io.throttle.examples.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인증 컨트롤러
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final JwtService jwtService;
    
    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * 로그인 API (Rate Limit 적용)
     * 1분에 5번만 로그인 시도 가능
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        // 간단한 인증 로직 (실제로는 데이터베이스에서 확인)
        User user = authenticateUser(request.getUsername(), request.getPassword());
        
        if (user != null) {
            String token = jwtService.generateToken(user);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("userType", user.getUserType().name());
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 토큰 검증 API
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        
        if (jwtService.validateToken(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", jwtService.extractUserId(token));
            response.put("username", jwtService.extractUsername(token));
            response.put("userType", jwtService.extractUserType(token));
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Invalid token");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 간단한 사용자 인증 (실제로는 데이터베이스에서 확인)
     */
    private User authenticateUser(String username, String password) {
        // 테스트용 사용자 데이터
        if ("free-user".equals(username) && "password".equals(password)) {
            return new User("1", "free-user", "free@example.com", User.UserType.FREE);
        } else if ("premium-user".equals(username) && "password".equals(password)) {
            return new User("2", "premium-user", "premium@example.com", User.UserType.PREMIUM);
        } else if ("enterprise-user".equals(username) && "password".equals(password)) {
            return new User("3", "enterprise-user", "enterprise@example.com", User.UserType.ENTERPRISE);
        }
        return null;
    }
    
    /**
     * 로그인 요청 DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
} 