package io.throttle.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Throttle 라이브러리 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "api-throttle")
public class ThrottleProperties {
    
    /**
     * Throttle 기능 활성화 여부
     */
    private boolean enabled = true;
    
    /**
     * 기본 저장소 타입 (memory, redis)
     */
    private StorageType defaultStorage = StorageType.MEMORY;
    
    /**
     * Redis 설정
     */
    private Redis redis = new Redis();
    
    /**
     * 기본 Rate Limit 설정
     */
    private DefaultRateLimit defaultRateLimit = new DefaultRateLimit();
    
    /**
     * 기본 Quota 설정
     */
    private DefaultQuota defaultQuota = new DefaultQuota();
    
    public enum StorageType {
        MEMORY, REDIS
    }
    
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private int database = 0;
        private String password;
        private int timeout = 2000;
        
        // Getters and Setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public int getDatabase() { return database; }
        public void setDatabase(int database) { this.database = database; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    public static class DefaultRateLimit {
        private int requests = 100;
        private String timeUnit = "MINUTES";
        private String keyType = "IP";
        
        // Getters and Setters
        public int getRequests() { return requests; }
        public void setRequests(int requests) { this.requests = requests; }
        
        public String getTimeUnit() { return timeUnit; }
        public void setTimeUnit(String timeUnit) { this.timeUnit = timeUnit; }
        
        public String getKeyType() { return keyType; }
        public void setKeyType(String keyType) { this.keyType = keyType; }
    }
    
    public static class DefaultQuota {
        private int daily = 1000;
        private int monthly = 30000;
        
        // Getters and Setters
        public int getDaily() { return daily; }
        public void setDaily(int daily) { this.daily = daily; }
        
        public int getMonthly() { return monthly; }
        public void setMonthly(int monthly) { this.monthly = monthly; }
    }
    
    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public StorageType getDefaultStorage() { return defaultStorage; }
    public void setDefaultStorage(StorageType defaultStorage) { this.defaultStorage = defaultStorage; }
    
    public Redis getRedis() { return redis; }
    public void setRedis(Redis redis) { this.redis = redis; }
    
    public DefaultRateLimit getDefaultRateLimit() { return defaultRateLimit; }
    public void setDefaultRateLimit(DefaultRateLimit defaultRateLimit) { this.defaultRateLimit = defaultRateLimit; }
    
    public DefaultQuota getDefaultQuota() { return defaultQuota; }
    public void setDefaultQuota(DefaultQuota defaultQuota) { this.defaultQuota = defaultQuota; }
} 