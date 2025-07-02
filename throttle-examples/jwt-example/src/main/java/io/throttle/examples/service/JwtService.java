package io.throttle.examples.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.throttle.examples.model.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 서비스
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@Service
public class JwtService {
    
    private static final String SECRET_KEY = "your-secret-key-here-make-it-long-enough-for-hs256";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24시간
    
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    /**
     * 사용자 정보로 JWT 토큰 생성
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", user.getUserType().name());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public String extractUserId(String token) {
        return extractClaim(token, "userId", String.class);
    }
    
    /**
     * JWT 토큰에서 사용자명 추출
     */
    public String extractUsername(String token) {
        return extractClaim(token, "username", String.class);
    }
    
    /**
     * JWT 토큰에서 사용자 타입 추출
     */
    public String extractUserType(String token) {
        return extractClaim(token, "userType", String.class);
    }
    
    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 토큰에서 특정 클레임 추출
     */
    private <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get(claimName, claimType);
    }
} 