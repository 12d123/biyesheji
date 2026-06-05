package com.hqh.warehouse_backend.runner;

import com.hqh.warehouse_backend.util.DatabaseChecker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动运行数据库测试
 */
@Component
public class DatabaseTestRunner implements CommandLineRunner {
    
    private final DatabaseChecker databaseChecker;
    
    public DatabaseTestRunner(DatabaseChecker databaseChecker) {
        this.databaseChecker = databaseChecker;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=" .repeat(60));
        System.out.println("🚀 智能仓储系统 - 数据库连接测试");
        System.out.println("=" .repeat(60));
        
        // 执行健康检查
        boolean isHealthy = databaseChecker.performHealthCheck();
        
        if (isHealthy) {
            System.out.println("🎉 数据库状态: 健康");
            // 详细检查
            databaseChecker.checkDatabaseConnection();
        } else {
            System.err.println("💥 数据库状态: 异常 - 请检查配置");
        }
        
        System.out.println("=" .repeat(60));
        System.out.println("✅ 数据库初始化完成");
        System.out.println("=" .repeat(60));
    }
}