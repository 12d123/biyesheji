package com.hqh.warehouse_backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接检查工具
 */
@Component
public class DatabaseChecker {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 检查数据库连接并打印状态
     */
    public void checkDatabaseConnection() {
        System.out.println("🔍 开始检查数据库连接...");
        
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ 数据库连接成功!");
            
            // 获取数据库信息
            var metaData = connection.getMetaData();
            System.out.println("🗄️ 数据库信息:");
            System.out.println("  - 数据库: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("  - 驱动: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            System.out.println("  - URL: " + metaData.getURL());
            
            // 检查传感器数据表
            checkSensorDataTable();
            
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查传感器数据表
     */
    private void checkSensorDataTable() {
        try {
            // 检查表是否存在
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sensor_data'", 
                Integer.class
            );
            
            if (tableCount != null && tableCount > 0) {
                System.out.println("✅ sensor_data 表存在");
                
                // 检查记录数
                Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sensor_data", Integer.class);
                System.out.println("📊 传感器数据记录数: " + recordCount);
                
                // 显示最新几条数据
                if (recordCount != null && recordCount > 0) {
                    System.out.println("📈 最新数据示例:");
                    jdbcTemplate.query("SELECT location, temperature, humidity, created_time FROM sensor_data ORDER BY created_time DESC LIMIT 3", 
                        (rs, rowNum) -> {
                            System.out.println("  - " + 
                                rs.getString("location") + ": " + 
                                rs.getDouble("temperature") + "°C, " + 
                                rs.getDouble("humidity") + "%, " +
                                rs.getTimestamp("created_time"));
                            return null;
                        });
                } else {
                    System.out.println("⚠️  sensor_data 表为空，将插入测试数据...");
                    insertTestData();
                }
            } else {
                System.out.println("❌ sensor_data 表不存在，请检查 Hibernate 配置");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 检查传感器数据表失败: " + e.getMessage());
        }
    }
    
    /**
     * 插入测试数据
     */
    public void insertTestData() {
        try {
            System.out.println("🎯 开始插入测试数据...");
            
            String sql = "INSERT INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES " +
                "(25.5, 65.0, 'A区仓库', NOW() - INTERVAL 2 HOUR, 'DHT11'), " +
                "(23.8, 70.2, 'B区仓库', NOW() - INTERVAL 90 MINUTE, 'DHT11'), " +
                "(26.1, 62.5, 'C区仓库', NOW() - INTERVAL 1 HOUR, 'DHT11'), " +
                "(24.2, 68.3, '冷藏区', NOW() - INTERVAL 45 MINUTE, 'DS18B20'), " +
                "(22.7, 55.8, '贵重物品区', NOW() - INTERVAL 30 MINUTE, 'DHT22')";
            
            int affectedRows = jdbcTemplate.update(sql);
            System.out.println("✅ 成功插入 " + affectedRows + " 条测试数据");
            
        } catch (Exception e) {
            System.err.println("❌ 插入测试数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 完整的数据库健康检查
     */
    public boolean performHealthCheck() {
        System.out.println("🏥 开始数据库健康检查...");
        
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                System.err.println("❌ 数据库连接无效");
                return false;
            }
            
            // 检查表是否存在
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sensor_data'", 
                Integer.class
            );
            
            if (tableCount == null || tableCount == 0) {
                System.err.println("❌ sensor_data 表不存在");
                return false;
            }
            
            // 检查是否有数据
            Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sensor_data", Integer.class);
            if (recordCount == null || recordCount == 0) {
                System.out.println("⚠️  表存在但无数据，将插入测试数据");
                insertTestData();
            }
            
            System.out.println("✅ 数据库健康检查通过");
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ 数据库健康检查失败: " + e.getMessage());
            return false;
        }
    }
}