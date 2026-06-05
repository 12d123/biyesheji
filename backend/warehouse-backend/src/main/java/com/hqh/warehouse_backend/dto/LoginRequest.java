package com.hqh.warehouse_backend.dto;

/**
 * 登录请求数据传输对象
 */
public class LoginRequest {
    
    private String username;
    private String password;
    private Boolean rememberMe = false;
    
    // ========== 构造函数 ==========
    
    public LoginRequest() {
    }
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public LoginRequest(String username, String password, Boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }
    
    // ========== Getter 和 Setter ==========
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    @Override
    public String toString() {
        return String.format("LoginRequest{username='%s', rememberMe=%s}", username, rememberMe);
    }
}