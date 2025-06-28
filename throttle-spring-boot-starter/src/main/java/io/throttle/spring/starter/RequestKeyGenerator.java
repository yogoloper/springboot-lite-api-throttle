package io.throttle.spring.starter;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

/**
 * 요청 키를 생성하는 유틸리티 클래스
 */
@Component
public class RequestKeyGenerator {
    
    /**
     * IP 주소를 기반으로 요청 키를 생성
     * @param exchange HTTP 요청 정보
     * @param endpoint API 엔드포인트
     * @return IP 주소 기반 키
     */
    public static String generateIpKey(ServerWebExchange exchange, String endpoint) {
        String remoteAddr = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
            .map(address -> address.getAddress().getHostAddress())
            .orElseThrow(() -> new IllegalArgumentException("Remote address not found"));
        return String.format("ip:%s:%s", remoteAddr, endpoint);
    }

    /**
     * JWT 기반 키 생성
     * @param exchange HTTP 요청 정보
     * @param endpoint API 엔드포인트
     * @param claims JWT 클레임
     * @return JWT 기반 키
     */
    public static String generateJwtKey(ServerWebExchange exchange, String endpoint, Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("JWT subject is empty");
        }
        return String.format("jwt:%s:%s", subject, endpoint);
    }

    /**
     * 헤더 기반 키 생성
     * @param exchange HTTP 요청 정보
     * @param endpoint API 엔드포인트
     * @param headerName 헤더 이름
     * @return 헤더 기반 키
     */
    public static String generateHeaderKey(ServerWebExchange exchange, String endpoint, String headerName) {
        String headerValue = exchange.getRequest().getHeaders().getFirst(headerName);
        if (headerValue == null || headerValue.isEmpty()) {
            throw new IllegalArgumentException(String.format("Header '%s' not found", headerName));
        }
        return String.format("%s:%s:%s", headerName.toLowerCase(), headerValue, endpoint);
    }

    /**
     * 기본 키 생성
     * @param exchange HTTP 요청 정보
     * @param endpoint API 엔드포인트
     * @return 기본 키
     */
    public static String generateDefaultKey(ServerWebExchange exchange, String endpoint) {
        return String.format("default:%s", endpoint);
    }
      /**
     * 키 형식: {type}:{value}:{endpoint}
     * 예:
     * - ip:192.168.0.1:/api/users
     * - jwt:12345678:/api/users
     * - x-api-key:abc123:/api/users
     * - default:/api/users
     */
    private static String formatKey(String type, String value, String endpoint) {
        return String.format("%s:%s:%s", type, value, endpoint);
    }
}
