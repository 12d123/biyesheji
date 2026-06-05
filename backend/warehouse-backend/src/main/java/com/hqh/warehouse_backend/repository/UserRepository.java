package com.hqh.warehouse_backend.repository;

import com.hqh.warehouse_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ===================== 原有核心方法 =====================
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    
    // 根据用户名或邮箱查找用户
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    // 检查用户名是否存在
    boolean existsByUsername(String username);
    
    // 检查邮箱是否存在
    boolean existsByEmail(String email);
    
    // 根据角色查找用户（列表）
    List<User> findByRole(String role);
    
    // 根据角色统计用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);
    
    // 修复参数重复问题：根据用户名、姓名、邮箱模糊搜索（忽略大小写）并匹配角色
    @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND u.role = :role")
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRole(
        @Param("keyword") String keyword,
        @Param("role") String role);
    
    // 修复参数重复问题：根据用户名、姓名、邮箱模糊搜索（忽略大小写）
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        @Param("keyword") String keyword);
    
    // 更新最后登录时间
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :lastLoginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);
    
    // 统计总用户数（保留自定义查询，兼容原有代码）
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    
    // 根据ID列表查找用户
    List<User> findByIdIn(List<Long> ids);

    // ===================== 新增分页查询方法（适配UserService） =====================
    // 1. 仅按用户名模糊查询（分页）
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<User> findByUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);
    
    // 2. 仅按角色查询（分页）
    Page<User> findByRole(String role, Pageable pageable);
    
    // 3. 仅按账户状态查询（分页）
    Page<User> findByEnabled(Boolean enabled, Pageable pageable);
    
    // 4. 用户名+角色（分页）
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) AND u.role = :role")
    Page<User> findByUsernameContainingIgnoreCaseAndRole(
        @Param("username") String username, 
        @Param("role") String role, 
        Pageable pageable);
    
    // 5. 用户名+账户状态（分页）
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) AND u.enabled = :enabled")
    Page<User> findByUsernameContainingIgnoreCaseAndEnabled(
        @Param("username") String username, 
        @Param("enabled") Boolean enabled, 
        Pageable pageable);
    
    // 6. 角色+账户状态（分页）
    Page<User> findByRoleAndEnabled(String role, Boolean enabled, Pageable pageable);
    
    // 7. 用户名+角色+账户状态（分页）
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) " +
           "AND u.role = :role AND u.enabled = :enabled")
    Page<User> findByUsernameContainingIgnoreCaseAndRoleAndEnabled(
        @Param("username") String username, 
        @Param("role") String role, 
        @Param("enabled") Boolean enabled, 
        Pageable pageable);
}