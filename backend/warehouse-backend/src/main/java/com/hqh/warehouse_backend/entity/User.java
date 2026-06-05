package com.hqh.warehouse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "role", nullable = false, length = 20)
    private String role = "VIEWER"; // 角色：VIEWER/OPERATOR/ADMIN/SUPER_ADMIN
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // 账户是否启用
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime; // 创建时间
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime; // 最后登录时间
    
    @Column(name = "full_name", length = 100)
    private String fullName; // 真实姓名
    
    @Column(name = "phone", length = 20)
    private String phone; // 手机号
    
    // ========== 构造函数 ==========
    
    /**
     * 空构造函数（JPA 必需）
     */
    public User() {
        this.createTime = LocalDateTime.now();
    }
    
    /**
     * 基础构造函数
     */
    public User(String username, String password, String email, String role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }
    
    /**
     * 完整构造函数（含真实姓名）
     */
    public User(String username, String password, String email, String role, String fullName) {
        this(username, password, email, role);
        this.fullName = fullName;
    }
    
    // ========== Getter 和 Setter ==========
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
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
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getRole() { 
        return role; 
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
    
    public Boolean getEnabled() { 
        return enabled; 
    }
    
    public void setEnabled(Boolean enabled) { 
        this.enabled = enabled; 
    }
    
    public LocalDateTime getCreateTime() { 
        return createTime; 
    }
    
    public void setCreateTime(LocalDateTime createTime) { 
        this.createTime = createTime; 
    }
    
    public LocalDateTime getLastLoginTime() { 
        return lastLoginTime; 
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) { 
        this.lastLoginTime = lastLoginTime; 
    }
    
    public String getFullName() { 
        return fullName; 
    }
    
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }
    
    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', enabled=%s, createTime=%s}",
                id, username, role, enabled, createTime);
    }
}