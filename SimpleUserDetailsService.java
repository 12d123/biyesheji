package com.hqh.warehouse_backend.config;

import com.hqh.warehouse_backend.entity.User;
import com.hqh.warehouse_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * 简化的UserDetailsService实现
 */
public class SimpleUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleUserDetailsService.class);
    
    private final UserService userService;
    
    public SimpleUserDetailsService(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("🔍 加载用户详情: {}", username);
        
        Optional<User> userOptional = userService.findByUsernameOrEmail(username);
        
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        User user = userOptional.get();
        
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 创建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .disabled(!user.getEnabled())
                .build();
    }
}