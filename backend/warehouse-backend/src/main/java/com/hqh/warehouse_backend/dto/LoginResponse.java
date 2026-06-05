package com.hqh.warehouse_backend.dto;

/**
 * 登录响应数据传输对象
 */
public class LoginResponse {
    
    private Boolean success;
    private String message;
    private String token;
    private String username;
    private String role;
    private String fullName;
    private Long expiresIn;
    
    // ========== 构造函数 ==========
    
    public LoginResponse() {
    }
    
    public LoginResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public LoginResponse(Boolean success, String message, String token, String username, String role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.username = username;
        this.role = role;
    }
    
    // ========== Getter 和 Setter ==========
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    // ========== 静态工厂方法 ==========
    
public static LoginResponse success(String token, String username, String role, String fullName, long expiresIn) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("登录成功");
        response.setToken(token);
        response.setUsername(username);
        response.setRole(role);
        response.setFullName(fullName);
        response.setExpiresIn(expiresIn);
        return response;
    }
}