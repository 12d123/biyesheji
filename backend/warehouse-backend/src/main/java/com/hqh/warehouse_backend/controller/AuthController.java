package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.dto.ApiResponse;
import com.hqh.warehouse_backend.dto.LoginRequest;
import com.hqh.warehouse_backend.dto.LoginResponse;
import com.hqh.warehouse_backend.entity.User;
import com.hqh.warehouse_backend.service.UserService;
import com.hqh.warehouse_backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证控制器
 * 处理用户登录、注销、令牌刷新等操作
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
   @PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
    logger.info("🔐 用户登录请求: {}", loginRequest.getUsername());
    
    try {
        // 验证用户登录
        boolean isValid = userService.validateLogin(loginRequest.getUsername(), loginRequest.getPassword());
        
        if (!isValid) {
            logger.warn("❌ 登录失败: {}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
        }
        
        // 获取用户信息
        Optional<User> userOptional = userService.findByUsernameOrEmail(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        }
        
        User user = userOptional.get();
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername());
        long expiresIn = jwtUtil.getRemainingTime(token);
        
        // 构建响应 - 确保直接返回 LoginResponse
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSuccess(true);
        loginResponse.setMessage("登录成功");
        loginResponse.setToken(token);
        loginResponse.setUsername(user.getUsername());
        loginResponse.setRole(user.getRole());
        loginResponse.setFullName(user.getFullName());
        loginResponse.setExpiresIn(expiresIn);
        
        logger.info("✅ 登录成功: {} ({})", user.getUsername(), user.getRole());
        
        // 返回 ApiResponse<LoginResponse>，不要再次嵌套
        return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
        
    } catch (Exception e) {
        logger.error("❌ 登录处理异常: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error("登录处理异常: " + e.getMessage()));
    }
}
    
    /**
     * 验证令牌
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("🔍 验证令牌请求");
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.error("令牌格式错误"));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", isValid);
            
            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                data.put("username", username);
                data.put("expiresIn", jwtUtil.getRemainingTime(token));
                
                // 获取用户信息
                Optional<User> userOptional = userService.findByUsername(username);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    data.put("role", user.getRole());
                    data.put("fullName", user.getFullName());
                }
                
                logger.info("✅ 令牌验证成功: {}", username);
                return ResponseEntity.ok(ApiResponse.success("令牌有效", data));
            } else {
                logger.warn("❌ 令牌验证失败");
                return ResponseEntity.ok(ApiResponse.error("令牌无效或已过期", data));
            }
            
        } catch (Exception e) {
            logger.error("❌ 令牌验证异常: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("令牌验证异常: " + e.getMessage()));
        }
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("🔄 刷新令牌请求");
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.error("令牌格式错误"));
            }
            
            String token = authHeader.substring(7);
            String newToken = jwtUtil.refreshToken(token);
            
            Map<String, String> data = new HashMap<>();
            data.put("token", newToken);
            data.put("expiresIn", String.valueOf(jwtUtil.getRemainingTime(newToken)));
            
            logger.info("✅ 令牌刷新成功");
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", data));
            
        } catch (Exception e) {
            logger.error("❌ 令牌刷新异常: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("令牌刷新异常: " + e.getMessage()));
        }
    }
    
    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        logger.info("🚪 用户注销请求");
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                logger.info("✅ 用户注销: {}", username);
            }
            
            return ResponseEntity.ok(ApiResponse.success("注销成功"));
            
        } catch (Exception e) {
            logger.error("❌ 注销处理异常: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("注销处理异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        logger.info("👤 获取当前用户信息请求");
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.error("未提供令牌"));
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("用户不存在"));
            }
            
            User user = userOptional.get();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("createTime", user.getCreateTime());
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            
            logger.info("✅ 获取用户信息成功: {}", username);
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userInfo));
            
        } catch (Exception e) {
            logger.error("❌ 获取用户信息异常: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取用户信息异常: " + e.getMessage()));
        }
    }
    
    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        logger.info("🏥 认证服务健康检查");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("service", "认证服务");
        healthInfo.put("status", "正常");
        healthInfo.put("timestamp", System.currentTimeMillis());
        
        // 检查用户服务健康
        boolean userServiceHealth = userService.performUserHealthCheck();
        healthInfo.put("userService", userServiceHealth ? "正常" : "异常");
        
        // 检查默认管理员账户
        boolean defaultAdminExists = userService.createDefaultAdminIfNotExists();
        healthInfo.put("defaultAdmin", defaultAdminExists ? "已创建" : "已存在");
        
        return ResponseEntity.ok(ApiResponse.success("认证服务运行正常", healthInfo));
    }
}