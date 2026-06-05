package com.hqh.warehouse_backend.config;

import com.hqh.warehouse_backend.service.UserService;
import com.hqh.warehouse_backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截请求并验证JWT令牌
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String requestPath = request.getServletPath();
        final String authHeader = request.getHeader("Authorization");
        
        logger.info("🛡️ JWT过滤器 - 请求路径: {}", requestPath);
        
        // 如果是认证相关的请求，直接放行
        if (requestPath.startsWith("/api/auth/") || requestPath.startsWith("/api/test/")) {
            logger.info("✅ 放行认证相关请求: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查Authorization头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("⚠️ 请求未包含Bearer令牌: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 提取token
        String token = authHeader.substring(7);
        logger.info("🔍 提取到JWT Token，长度: {}", token.length());
        
        try {
            // 验证token
            String username = jwtUtil.extractUsername(token);
            logger.info("👤 从Token中提取用户名: {}", username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 验证token有效性
                if (jwtUtil.validateToken(token)) {
                    logger.info("✅ JWT Token验证成功 - 用户: {}", username);
                    
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("🔐 用户认证设置完成: {}", username);
                } else {
                    logger.warn("❌ JWT Token验证失败");
                }
            }
        } catch (Exception e) {
            logger.error("💥 JWT Token处理异常: {}", e.getMessage());
            // 继续过滤器链，让后续的异常处理器处理
        }
        
        filterChain.doFilter(request, response);
    }
}