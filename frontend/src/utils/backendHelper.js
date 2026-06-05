/**
 * 后端问题诊断和修复助手
 */

// 生成后端修复建议
export function generateBackendFixSuggestions(diagnosisResult) {
  const suggestions = []
  
  // 基础连接正常但传感器API失败
  if (diagnosisResult.includes('传感器API异常') || diagnosisResult.includes('传感器端点都不可用')) {
    suggestions.push(
      '🔧 **问题诊断**: 后端服务运行正常，但传感器API无法访问',
      '',
      '📋 **可能的原因**:',
      '1. **数据库连接失败** - 检查application.yml中的数据库配置',
      '2. **数据表不存在** - sensor_data表没有自动创建',
      '3. **服务层代码异常** - SensorDataService中有未处理的异常',
      '4. **没有初始数据** - 数据库为空，getLatestData()返回null',
      '',
      '🛠️ **修复步骤**:',
      '**步骤1: 检查数据库配置**',
      '```yaml',
      '# 在 application.yml 中检查:',
      'spring:',
      '  datasource:',
      '    url: jdbc:mysql://localhost:3306/warehouse',
      '    username: your_username',
      '    password: your_password',
      '  jpa:',
      '    hibernate:',
      '      ddl-auto: update  # 确保是create或update',
      '```',
      '',
      '**步骤2: 检查数据表**',
      '```sql',
      '-- 在MySQL中执行:',
      'USE warehouse;',
      'SHOW TABLES LIKE \"sensor_data\";',
      'DESC sensor_data;',
      '```',
      '',
      '**步骤3: 添加初始数据**',
      '```sql',
      '-- 如果表为空，插入测试数据:',
      'INSERT INTO sensor_data (temperature, humidity, location, created_time, sensor_type)',
      'VALUES (25.5, 65.0, \"A区仓库\", NOW(), \"DHT11\");',
      '```',
      '',
      '**步骤4: 检查后端日志**',
      '查看控制台是否有Hibernate或数据库相关的错误信息'
    )
  }
  
  return suggestions.length > 0 ? suggestions.join('\n') : '暂无具体修复建议'
}

// 生成快速测试SQL
export function generateTestSQL() {
  return `
-- 1. 检查数据库和表
SHOW DATABASES;
USE warehouse;
SHOW TABLES;
DESC sensor_data;

-- 2. 检查现有数据
SELECT * FROM sensor_data ORDER BY created_time DESC LIMIT 5;

-- 3. 插入测试数据（如果表为空）
INSERT INTO sensor_data (temperature, humidity, location, created_time, sensor_type) VALUES
(25.5, 65.0, 'A区仓库', NOW(), 'DHT11'),
(23.8, 70.2, 'B区仓库', NOW(), 'DHT11'),
(26.1, 62.5, 'C区仓库', NOW(), 'DHT11');

-- 4. 验证数据插入
SELECT * FROM sensor_data ORDER BY created_time DESC;
`
}

// 生成application.yml配置示例
export function generateApplicationYmlExample() {
  return `spring:
  datasource:
    url: jdbc:mysql://localhost:3306/warehouse
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

# 日志配置
logging:
  level:
    com.hqh.warehouse_backend: DEBUG
    org.hibernate: DEBUG`
}