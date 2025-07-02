package io.throttle.examples.model;

/**
 * 사용자 모델
 * 
 * @author yogoloper
 * @since 1.0.0
 */
public class User {
    private String id;
    private String username;
    private String email;
    private UserType userType;

    public enum UserType {
        FREE,      // 무료 사용자
        PREMIUM,   // 프리미엄 사용자
        ENTERPRISE // 엔터프라이즈 사용자
    }

    public User() {}

    public User(String id, String username, String email, UserType userType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userType = userType;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
} 