package com.hqh.warehouse_backend.service;

import com.hqh.warehouse_backend.entity.User;
import com.hqh.warehouse_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    // 默认重置密码
    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private UserRepository userRepository;

    // 注入独立配置的PasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录验证
     */
    public boolean validateLogin(String username, String password) {
        logger.info("🔐 用户登录验证: {}", username);

        Optional<User> userOptional = userRepository.findByUsernameOrEmail(username);

        if (userOptional.isEmpty()) {
            logger.warn("❌ 用户不存在: {}", username);
            return false;
        }

        User user = userOptional.get();

        if (!user.getEnabled()) {
            logger.warn("❌ 用户账户已被禁用: {}", username);
            return false;
        }

        // 验证密码（注意参数顺序：明文密码 + 加密密码）
        boolean passwordValid = passwordEncoder.matches(password, user.getPassword());

        if (passwordValid) {
            logger.info("✅ 用户登录成功: {}", username);
            // 更新最后登录时间
            updateLastLoginTime(user.getId());
        } else {
            logger.warn("❌ 密码错误: {}", username);
        }

        return passwordValid;
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        logger.debug("🔍 根据用户名查找用户: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * 根据用户名或邮箱查找用户
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        logger.debug("🔍 根据用户名或邮箱查找用户: {}", usernameOrEmail);
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * 创建新用户
     */
    public User createUser(User user) {
        logger.info("👤 创建新用户: {}", user.getUsername());

        // 检查用户名是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 检查邮箱是否存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("✅ 用户创建成功: {}", savedUser.getUsername());

        return savedUser;
    }

    /**
     * 优化：更新用户信息（支持部分字段更新，接收Map参数）
     */
    public User updateUser(Long id, Map<String, Object> userParams) {
        logger.info("✏️ 更新用户ID: {}", id);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        User user = existingUser.get();
        // 禁止修改默认管理员的用户名
        if ("admin".equals(user.getUsername()) && userParams.containsKey("username")) {
            throw new RuntimeException("禁止修改默认管理员的用户名");
        }

        // 逐个更新字段（支持部分更新）
        if (userParams.containsKey("fullName")) {
            user.setFullName((String) userParams.get("fullName"));
        }
        if (userParams.containsKey("email")) {
            String newEmail = (String) userParams.get("email");
            // 检查邮箱是否重复（排除自身）
            if (newEmail != null && !newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("邮箱已存在: " + newEmail);
            }
            user.setEmail(newEmail);
        }
        if (userParams.containsKey("phone")) {
            user.setPhone((String) userParams.get("phone"));
        }
        if (userParams.containsKey("role")) {
            user.setRole((String) userParams.get("role"));
        }
        if (userParams.containsKey("enabled")) {
            Boolean enabled = (Boolean) userParams.get("enabled");
            // 禁止禁用默认管理员
            if ("admin".equals(user.getUsername()) && !enabled) {
                throw new RuntimeException("禁止禁用默认管理员账户");
            }
            user.setEnabled(enabled);
        }

        User updatedUser = userRepository.save(user);
        logger.info("✅ 用户更新成功: {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * 原有更新方法（兼容旧代码）
     */
    public User updateUser(User user) {
        logger.info("✏️ 更新用户: {}", user.getUsername());

        Optional<User> existingUser = userRepository.findById(user.getId());
        if (existingUser.isEmpty()) {
            throw new RuntimeException("用户不存在: " + user.getId());
        }

        User existing = existingUser.get();
        // 禁止修改默认管理员的用户名
        if ("admin".equals(existing.getUsername()) && !existing.getUsername().equals(user.getUsername())) {
            throw new RuntimeException("禁止修改默认管理员的用户名");
        }

        // 检查邮箱是否重复（排除自身）
        if (user.getEmail() != null && !user.getEmail().equals(existing.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }

        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        // 禁止禁用默认管理员
        if ("admin".equals(existing.getUsername()) && !user.getEnabled()) {
            throw new RuntimeException("禁止禁用默认管理员账户");
        }
        existing.setEnabled(user.getEnabled());

        return userRepository.save(existing);
    }

    /**
     * 删除用户（优化：禁止删除默认管理员）
     */
    public void deleteUser(Long id) {
        logger.info("🗑️ 删除用户ID: {}", id);

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        User user = userOptional.get();
        // 禁止删除默认管理员
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("禁止删除默认管理员账户");
        }

        userRepository.deleteById(id);
        logger.info("✅ 用户删除成功: {}", id);
    }

    /**
     * 批量删除用户
     */
    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        logger.info("🗑️ 批量删除用户: {} 个", ids.size());

        for (Long id : ids) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 禁止删除默认管理员
                if ("admin".equals(user.getUsername())) {
                    logger.warn("禁止删除默认管理员，跳过ID: {}", id);
                    continue;
                }
                userRepository.deleteById(id);
                logger.debug("删除用户ID: {}", id);
            } else {
                logger.warn("用户不存在，跳过删除: {}", id);
            }
        }
    }

    /**
     * 更新用户状态（优化：禁止禁用默认管理员）
     */
    public User updateUserStatus(Long id, Boolean enabled) {
        logger.info("🔄 更新用户状态: {} -> {}", id, enabled);

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        User user = userOptional.get();
        // 禁止禁用默认管理员
        if ("admin".equals(user.getUsername()) && !enabled) {
            throw new RuntimeException("禁止禁用默认管理员账户");
        }

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        logger.info("✅ 用户状态更新成功: {} -> {}", id, enabled);
        return updatedUser;
    }

    /**
     * 批量更新用户状态
     */
    @Transactional
    public void batchUpdateUserStatus(List<Long> ids, Boolean enabled) {
        logger.info("🔄 批量更新用户状态: {} 个 -> {}", ids.size(), enabled);

        for (Long id : ids) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 禁止禁用默认管理员
                if ("admin".equals(user.getUsername()) && !enabled) {
                    logger.warn("禁止禁用默认管理员，跳过ID: {}", id);
                    continue;
                }
                user.setEnabled(enabled);
                userRepository.save(user);
                logger.debug("更新用户状态 ID: {} -> {}", id, enabled);
            } else {
                logger.warn("用户不存在，跳过更新: {}", id);
            }
        }
    }

    /**
     * 搜索用户（核心修复：参数数量匹配Repository）
     */
    public List<User> searchUsers(String keyword, String role) {
        logger.info("🔍 搜索用户 - 关键词: {}, 角色: {}", keyword, role);

        if (keyword != null && !keyword.trim().isEmpty() && role != null && !role.trim().isEmpty()) {
            // 修复：仅传递1个keyword参数（Repository已改为单参数）
            return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRole(
                    keyword, role);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 修复：仅传递1个keyword参数
            return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword);
        } else if (role != null && !role.trim().isEmpty()) {
            // 只按角色搜索
            return userRepository.findByRole(role);
        } else {
            // 无搜索条件，返回所有
            return userRepository.findAll();
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(Long id, String newPassword) {
        logger.info("🔑 修改用户密码: {}", id);

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        User user = userOptional.get();
        // 加密密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("✅ 用户密码修改成功: {}", id);
    }

    /**
     * 新增：重置密码为默认值（123456）
     */
    public void resetPassword(Long id) {
        logger.info("🔑 重置用户密码为默认值: {}", id);
        // 调用修改密码方法，使用默认密码
        changePassword(id, DEFAULT_PASSWORD);
        logger.info("✅ 用户密码重置成功（默认密码：123456）: {}", id);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * 新增：分页查询用户（支持条件过滤）
     */
    public Page<User> getUserList(int page, int size, String username, String role, Boolean enabled) {
        logger.info("📃 分页查询用户 - 页码: {}, 每页条数: {}, 用户名: {}, 角色: {}, 状态: {}",
                page, size, username, role, enabled);

        // 排序：按ID降序
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 条件查询（根据传入的参数动态拼接）
        Page<User> userPage;
        if (StringUtils.hasText(username) && StringUtils.hasText(role) && enabled != null) {
            // 用户名+角色+状态
            userPage = userRepository.findByUsernameContainingIgnoreCaseAndRoleAndEnabled(username, role, enabled, pageable);
        } else if (StringUtils.hasText(username) && StringUtils.hasText(role)) {
            // 用户名+角色
            userPage = userRepository.findByUsernameContainingIgnoreCaseAndRole(username, role, pageable);
        } else if (StringUtils.hasText(username) && enabled != null) {
            // 用户名+状态
            userPage = userRepository.findByUsernameContainingIgnoreCaseAndEnabled(username, enabled, pageable);
        } else if (StringUtils.hasText(role) && enabled != null) {
            // 角色+状态
            userPage = userRepository.findByRoleAndEnabled(role, enabled, pageable);
        } else if (StringUtils.hasText(username)) {
            // 仅用户名
            userPage = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else if (StringUtils.hasText(role)) {
            // 仅角色
            userPage = userRepository.findByRole(role, pageable);
        } else if (enabled != null) {
            // 仅状态
            userPage = userRepository.findByEnabled(enabled, pageable);
        } else {
            // 无条件，分页查询所有
            userPage = userRepository.findAll(pageable);
        }

        return userPage;
    }

    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * 获取用户总数
     */
    public long getTotalUserCount() {
        // 兼容：优先使用自定义countTotalUsers，无则用JPA默认count()
        try {
            return userRepository.countTotalUsers();
        } catch (Exception e) {
            return userRepository.count();
        }
    }

    /**
     * 根据角色获取用户数量
     */
    public long getUserCountByRole(String role) {
        return userRepository.countByRole(role);
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(String username) {
        boolean exists = userRepository.existsByUsername(username);
        logger.debug("🔍 检查用户是否存在: {} -> {}", username, exists);
        return exists;
    }

    /**
     * 更新用户最后登录时间
     */
    @Transactional // 新增：修改操作需事务
    private void updateLastLoginTime(Long userId) {
        try {
            userRepository.updateLastLoginTime(userId, LocalDateTime.now());
            logger.debug("⏰ 更新用户最后登录时间，用户ID: {}", userId);
        } catch (Exception e) {
            logger.error("❌ 更新最后登录时间失败，用户ID: {}, 错误: {}", userId, e.getMessage());
        }
    }

    /**
     * 用户健康检查
     */
    public boolean performUserHealthCheck() {
        logger.info("🏥 开始用户服务健康检查...");

        try {
            long userCount = userRepository.count();
            logger.info("📊 系统总用户数: {}", userCount);

            // 检查是否有启用的管理员用户
            boolean hasAdmin = userRepository.findByRole("ADMIN").stream().anyMatch(User::getEnabled);

            if (hasAdmin) {
                logger.info("✅ 用户服务健康检查通过 - 存在启用的管理员账户");
            } else {
                logger.warn("⚠️ 用户服务健康检查警告 - 没有启用的管理员账户");
            }

            return hasAdmin;

        } catch (Exception e) {
            logger.error("❌ 用户服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建默认管理员账户（如果不存在）
     */
    public boolean createDefaultAdminIfNotExists() {
        logger.info("👨‍💼 检查默认管理员账户...");

        try {
            Optional<User> adminUser = userRepository.findByUsername("admin");

            if (adminUser.isEmpty()) {
                logger.info("🎯 创建默认管理员账户...");

                User defaultAdmin = new User();
                defaultAdmin.setUsername("admin");
                defaultAdmin.setPassword(passwordEncoder.encode("admin123")); // 加密默认密码
                defaultAdmin.setEmail("admin@warehouse.com");
                defaultAdmin.setRole("ADMIN");
                defaultAdmin.setFullName("系统管理员");
                defaultAdmin.setEnabled(true);
                defaultAdmin.setCreateTime(LocalDateTime.now());

                userRepository.save(defaultAdmin);
                logger.info("✅ 默认管理员账户创建成功 - 用户名: admin, 密码: admin123");
                return true;
            } else {
                logger.info("✅ 默认管理员账户已存在");
                return false;
            }

        } catch (Exception e) {
            logger.error("❌ 创建默认管理员账户失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}