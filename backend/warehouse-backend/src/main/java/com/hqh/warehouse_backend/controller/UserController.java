package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.dto.ApiResponse;
import com.hqh.warehouse_backend.entity.User;
import com.hqh.warehouse_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户控制器
 * 接口路径：/api/users
 * 权限：仅管理员可访问
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问该控制器的所有接口
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户（原有分页接口，保留）
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @param username 用户名（模糊查询，可选）
     * @param role 角色（可选）
     * @param enabled 账户状态（可选）
     * @return 分页用户数据
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled) {
        try {
            // 调用服务层的分页查询方法（page-1适配JPA的0起始页码）
            Page<User> userPage = userService.getUserList(page - 1, size, username, role, enabled);
            
            // 构造分页返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("records", userPage.getContent()); // 当前页数据
            result.put("total", userPage.getTotalElements()); // 总条数
            result.put("page", page); // 当前页码
            result.put("size", size); // 每页条数
            result.put("pages", userPage.getTotalPages()); // 总页数
            
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有用户（原有接口，保留）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取用户（原有接口，保留）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("获取用户成功", userOptional.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建用户（原有接口，保留）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("用户创建成功", createdUser));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("用户创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户（原有接口，优化：支持部分字段更新）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> userParams) { // 接收Map，支持部分字段更新
        try {
            User updatedUser = userService.updateUser(id, userParams);
            return ResponseEntity.ok(ApiResponse.success("用户更新成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("用户更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除用户（原有接口，优化：禁止删除默认管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("用户删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除用户（新增：适配前端批量删除操作）
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<String>> batchDeleteUsers(@RequestBody List<Long> ids) {
        try {
            // 调用服务层批量删除方法
            userService.batchDeleteUsers(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除用户成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批量删除用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修改用户状态（原有接口，保留）
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            User user = userService.updateUserStatus(id, enabled);
            return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", user));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("用户状态更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量更新用户状态（新增：适配前端批量启用/禁用操作）
     */
    @PostMapping("/batch-status")
    public ResponseEntity<ApiResponse<String>> batchUpdateUserStatus(
            @RequestBody Map<String, Object> request) {
        try {
            // 解析请求参数
            List<Long> ids = (List<Long>) request.get("ids");
            Boolean enabled = (Boolean) request.get("enabled");
            
            // 调用服务层批量更新状态方法
            userService.batchUpdateUserStatus(ids, enabled);
            
            String msg = enabled ? "批量启用用户成功" : "批量禁用用户成功";
            return ResponseEntity.ok(ApiResponse.success(msg));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批量更新用户状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索用户（原有接口，保留）
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            List<User> users = userService.searchUsers(keyword, role);
            return ResponseEntity.ok(ApiResponse.success("搜索成功", users));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修改密码（原有接口，保留）
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            userService.changePassword(id, password);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("密码修改失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置密码为默认值（新增：默认密码 123456）
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@PathVariable Long id) {
        try {
            userService.resetPassword(id);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功，默认密码：123456"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("密码重置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户统计（原有接口，保留）
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", userService.getTotalUserCount());
            stats.put("admin", userService.getUserCountByRole("ADMIN"));
            stats.put("operator", userService.getUserCountByRole("OPERATOR"));
            stats.put("viewer", userService.getUserCountByRole("VIEWER"));
            return ResponseEntity.ok(ApiResponse.success("获取统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户健康检查（原有接口，保留）
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("service", "用户服务");
            healthInfo.put("status", "正常");
            healthInfo.put("totalUsers", userService.getTotalUserCount());
            healthInfo.put("timestamp", System.currentTimeMillis());
            
            // 检查默认管理员账户
            boolean defaultAdminExists = userService.createDefaultAdminIfNotExists();
            healthInfo.put("defaultAdmin", defaultAdminExists ? "已创建" : "已存在");
            
            return ResponseEntity.ok(ApiResponse.success("用户服务运行正常", healthInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("用户服务健康检查失败: " + e.getMessage()));
        }
    }
}