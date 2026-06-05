package com.hqh.warehouse_backend.runner;

import com.hqh.warehouse_backend.util.DatabaseHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时运行数据库检查
 */
@Component
@Order(1)
public class ApplicationStartupRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupRunner.class);
    
    private final DatabaseHealthCheck databaseHealthCheck;
    
    public ApplicationStartupRunner(DatabaseHealthCheck databaseHealthCheck) {
        this.databaseHealthCheck = databaseHealthCheck;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=" .repeat(60));
        logger.info("🚀 智能仓储监控系统后端启动中...");
        logger.info("=" .repeat(60));
        
        // 先执行基础健康检查
        boolean isHealthy = databaseHealthCheck.performHealthCheck();
        
        if (isHealthy) {
            logger.info("🎉 数据库连接正常");
        } else {
            logger.warn("⚠️ 数据库连接可能有问题");
        }
        
        logger.info("=" .repeat(60));
        
        // 延迟插入测试数据，确保表已创建
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 等待5秒让Hibernate创建表
                databaseHealthCheck.insertTestData();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}