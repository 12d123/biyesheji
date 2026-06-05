package com.hqh.warehouse_backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库健康检查工具
 */
@Component
public class DatabaseHealthCheck {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthCheck.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 执行完整的数据库健康检查
     */
    public boolean performHealthCheck() {
        logger.info("🔍 开始数据库健康检查...");
        
        // 1. 检查数据库连接
        if (!checkConnection()) {
            return false;
        }
        
        // 2. 检查表是否存在
        if (!checkTableExists()) {
            logger.info("⏳ 表不存在，等待Hibernate创建...");
            return true; // 返回true，因为Hibernate会自动创建表
        }
        
        // 3. 检查数据
        checkData();
        
        logger.info("✅ 数据库健康检查完成");
        return true;
    }
    
    /**
     * 检查数据库连接
     */
    private boolean checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                var metaData = connection.getMetaData();
                logger.info("✅ 数据库连接成功 - {} {}", 
                    metaData.getDatabaseProductName(), 
                    metaData.getDatabaseProductVersion());
                return true;
            } else {
                logger.error("❌ 数据库连接无效");
                return false;
            }
        } catch (SQLException e) {
            logger.error("❌ 数据库连接失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean checkTableExists() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sensor_data'", 
                Integer.class
            );
            
            return tableCount != null && tableCount > 0;
        } catch (Exception e) {
            logger.debug("检查表存在性失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查数据
     */
    private void checkData() {
        try {
            Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sensor_data", Integer.class);
            if (recordCount != null && recordCount > 0) {
                logger.info("📊 传感器数据记录数: {}", recordCount);
            } else {
                logger.info("📝 表为空，等待数据插入...");
                // 不在这里插入数据，等待应用完全启动
            }
        } catch (Exception e) {
            logger.debug("检查数据失败: {}", e.getMessage());
        }
    }
    
    /**
     * 插入测试数据（在应用完全启动后调用）
     */
    public void insertTestData() {
        try {
            if (!checkTableExists()) {
                logger.warn("⚠️ 表不存在，无法插入测试数据");
                return;
            }
            
            logger.info("🎯 插入测试数据...");
            
            // 使用简单的插入语句
            String[] sqls = {
                "INSERT IGNORE INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES (25.5, 65.0, 'A区仓库', NOW() - INTERVAL 2 HOUR, 'DHT11')",
                "INSERT IGNORE INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES (23.8, 70.2, 'B区仓库', NOW() - INTERVAL 90 MINUTE, 'DHT11')",
                "INSERT IGNORE INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES (26.1, 62.5, 'C区仓库', NOW() - INTERVAL 1 HOUR, 'DHT11')",
                "INSERT IGNORE INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES (24.2, 68.3, '冷藏区', NOW() - INTERVAL 45 MINUTE, 'DS18B20')",
                "INSERT IGNORE INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES (22.7, 55.8, '贵重物品区', NOW() - INTERVAL 30 MINUTE, 'DHT22')"
            };
            
            int totalInserted = 0;
            for (String sql : sqls) {
                try {
                    int affected = jdbcTemplate.update(sql);
                    totalInserted += affected;
                } catch (Exception e) {
                    logger.debug("插入数据失败: {}", e.getMessage());
                }
            }
            
            if (totalInserted > 0) {
                logger.info("✅ 成功插入 {} 条测试数据", totalInserted);
            } else {
                logger.info("📝 测试数据已存在");
            }
            
        } catch (Exception e) {
            logger.error("❌ 插入测试数据失败: {}", e.getMessage());
        }
    }
}