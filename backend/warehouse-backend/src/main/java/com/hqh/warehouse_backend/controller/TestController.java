package com.hqh.warehouse_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        logger.info("✅ 收到 /api/test/hello 请求");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "智能仓储监控系统后端服务启动成功！");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "warehouse-backend");
        response.put("version", "1.0.0");
        
        return response;
    }

    @GetMapping("/simple")
    public Map<String, Object> simple() {
        logger.info("✅ 收到 /api/test/simple 请求");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "简单测试接口 - 连接成功！");
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        logger.info("✅ 收到 /api/test/info 请求");
        
        Map<String, Object> info = new HashMap<>();
        info.put("project", "智能仓储监控系统");
        info.put("status", "运行正常");
        info.put("timestamp", LocalDateTime.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", "3.2.4");
        info.put("environment", "development");
        
        return info;
    }
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        logger.info("🏥 收到 /api/test/health 请求");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "warehouse-backend");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", "运行中");
        health.put("database", "connected");
        
        return health;
    }
    
    @GetMapping("/time")
    public Map<String, Object> currentTime() {
        Map<String, Object> timeInfo = new HashMap<>();
        timeInfo.put("currentTime", LocalDateTime.now());
        timeInfo.put("timestamp", System.currentTimeMillis());
        timeInfo.put("timezone", "Asia/Shanghai");
        timeInfo.put("format", "YYYY-MM-DD HH:mm:ss");
        
        return timeInfo;
    }
}