package com.hqh.warehouse_backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT令牌工具类
 * 负责Token的生成、验证和解析
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    // 从配置文件读取密钥
    @Value("${jwt.secret:warehouse-backend-secret-key-2024-10-16-1234567890abcdef}")
    private String secretKey;
    
    // Token有效期：24小时
    @Value("${jwt.expiration:86400000}")
    private long JWT_EXPIRATION;
    
    // 懒加载生成SecretKey
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 从token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 从token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 提取所有声明
     */
    private Claims extractAllClaims(String token) {
        try {
            logger.debug("🔍 开始解析JWT Token...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.debug("✅ JWT Token解析成功");
            return claims;
        } catch (Exception e) {
            logger.error("❌ 解析JWT Token失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 为用户生成token
     */
    public String generateToken(String username) {
        logger.debug("🔐 为用户生成JWT Token: {}", username);
        
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, username);
        
        logger.debug("✅ JWT Token生成成功");
        return token;
    }
    
    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
            
            if (isValid) {
                logger.debug("✅ JWT Token验证成功 - 用户: {}", username);
            } else {
                logger.debug("❌ JWT Token验证失败 - 用户: {}", username);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("❌ JWT Token验证异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证token是否有效（不指定用户名）
     */
    public Boolean validateToken(String token) {
        try {
            boolean isValid = !isTokenExpired(token);
            
            if (isValid) {
                logger.debug("✅ JWT Token格式验证成功");
            } else {
                logger.debug("❌ JWT Token已过期");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("❌ JWT Token验证异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取token剩余有效时间（毫秒）
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            
            logger.debug("⏰ JWT Token剩余有效时间: {}秒", (remaining / 1000));
            return remaining;
        } catch (Exception e) {
            logger.error("❌ 获取JWT Token剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 刷新token（重新生成）
     */
    public String refreshToken(String token) {
        logger.debug("🔄 刷新JWT Token");
        
        try {
            final String username = extractUsername(token);
            String newToken = generateToken(username);
            
            logger.debug("✅ JWT Token刷新成功");
            return newToken;
        } catch (Exception e) {
            logger.error("❌ JWT Token刷新失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 完整的Token验证检查
     */
    public boolean performTokenHealthCheck(String token) {
        logger.debug("🏥 开始JWT Token健康检查...");
        
        try {
            if (token == null || token.trim().isEmpty()) {
                logger.error("❌ Token为空");
                return false;
            }
            
            String username = extractUsername(token);
            boolean isValid = validateToken(token, username);
            
            if (isValid) {
                long remainingTime = getRemainingTime(token);
                logger.debug("✅ JWT Token健康检查通过 - 用户: {}, 剩余时间: {}秒", username, (remainingTime / 1000));
            } else {
                logger.error("❌ JWT Token健康检查失败");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("❌ JWT Token健康检查异常: {}", e.getMessage());
            return false;
        }
    }
}