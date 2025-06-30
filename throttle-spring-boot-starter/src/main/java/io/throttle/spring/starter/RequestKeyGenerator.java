package io.throttle.spring.starter;

import io.throttle.spring.config.ThrottleProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

/**
 * 요청 키를 생성하는 유틸리티 클래스
 */
@Component
@RequiredArgsConstructor
public class RequestKeyGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(RequestKeyGenerator.class);
    
    private final ThrottleProperties throttleProperties;
    
    /**
     * 현재 요청에서 자동으로 키를 생성
     * 설정에 따라 IP, JWT, 헤더 등을 우선순위에 따라 시도
     */
    public String generateKey() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("No request context found, using default key");
                return "default:no-context";
            }
            
            HttpServletRequest request = attributes.getRequest();
            String endpoint = request.getRequestURI();
            
            // JWT 토큰이 설정되어 있고 요청에 JWT가 있으면 JWT 기반 키 생성
            if (throttleProperties.getJwtHeader() != null && !throttleProperties.getJwtHeader().isEmpty() 
                && throttleProperties.getJwtSecret() != null && !throttleProperties.getJwtSecret().isEmpty()) {
                String jwtToken = request.getHeader(throttleProperties.getJwtHeader());
                if (jwtToken != null && !jwtToken.isEmpty()) {
                    try {
                        // Bearer 토큰에서 실제 토큰 추출
                        if (jwtToken.startsWith(throttleProperties.getJwt().getPrefix())) {
                            jwtToken = jwtToken.substring(throttleProperties.getJwt().getPrefix().length());
                        }
                        
                        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(throttleProperties.getJwtSecret().getBytes())
                            .build()
                            .parseClaimsJws(jwtToken)
                            .getBody();
                        return generateJwtKey(request, endpoint, claims);
                    } catch (Exception e) {
                        log.debug("Failed to parse JWT token: {}", e.getMessage());
                    }
                }
            }
            
            // API 키 헤더가 설정되어 있으면 헤더 기반 키 생성
            if (throttleProperties.getApiKeyHeader() != null && !throttleProperties.getApiKeyHeader().isEmpty()) {
                String apiKey = request.getHeader(throttleProperties.getApiKeyHeader());
                if (apiKey != null && !apiKey.isEmpty()) {
                    return generateHeaderKey(request, endpoint, throttleProperties.getApiKeyHeader());
                }
            }
            
            // IP 기반 키 생성 (기본값)
            return generateIpKey(request, endpoint);
            
        } catch (Exception e) {
            log.warn("Failed to generate request key: {}", e.getMessage());
            return "default:error";
        }
    }
    
    /**
     * IP 주소를 기반으로 요청 키를 생성
     */
    public String generateIpKey(HttpServletRequest request, String endpoint) {
        String remoteAddr = getClientIpAddress(request);
        return formatKey("ip", remoteAddr, endpoint);
    }

    /**
     * JWT 기반 키 생성
     */
    public String generateJwtKey(HttpServletRequest request, String endpoint, Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("JWT subject is empty");
        }
        return formatKey("jwt", subject, endpoint);
    }

    /**
     * 헤더 기반 키 생성
     */
    public String generateHeaderKey(HttpServletRequest request, String endpoint, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue == null || headerValue.isEmpty()) {
            throw new IllegalArgumentException(String.format("Header '%s' not found", headerName));
        }
        return formatKey(headerName.toLowerCase(), headerValue, endpoint);
    }

    /**
     * 기본 키 생성
     */
    public String generateDefaultKey(HttpServletRequest request, String endpoint) {
        return formatKey("default", "anonymous", endpoint);
    }
    
    /**
     * 클라이언트 IP 주소를 안전하게 추출
     * 프록시 환경을 고려하여 X-Forwarded-For, X-Real-IP 헤더도 확인
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        
        return "unknown";
    }
    
    /**
     * 키 형식: {type}:{value}:{endpoint}
     * 예:
     * - ip:192.168.0.1:/api/users
     * - jwt:12345678:/api/users
     * - x-api-key:abc123:/api/users
     * - default:anonymous:/api/users
     */
    private String formatKey(String type, String value, String endpoint) {
        return String.format("%s:%s:%s", type, value, endpoint);
    }
}
