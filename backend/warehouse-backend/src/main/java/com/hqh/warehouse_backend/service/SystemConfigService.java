package com.hqh.warehouse_backend.service;

import com.hqh.warehouse_backend.entity.SystemConfig;
import com.hqh.warehouse_backend.repository.SystemConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    // ========== 配置值获取方法 ==========

    /**
     * 获取配置值
     */
    public String getConfigValue(String key, String defaultValue) {
        logger.debug("🔍 获取配置值 - key: {}, defaultValue: {}", key, defaultValue);

        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(key);
        String value = config.map(SystemConfig::getConfigValue).orElse(defaultValue);

        logger.debug("✅ 配置值获取结果 - key: {}, value: {}", key, value);
        return value;
    }

    /**
     * 获取整型配置值
     */
    public Integer getConfigValueAsInt(String key, Integer defaultValue) {
        try {
            String value = getConfigValue(key, null);
            if (value != null) {
                Integer result = Integer.parseInt(value);
                logger.debug("🔢 整型配置值获取 - key: {}, value: {}", key, result);
                return result;
            }
        } catch (NumberFormatException e) {
            logger.warn("❌ 配置值转换失败 - key: {}, 使用默认值: {}, 错误: {}", key, defaultValue, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * 获取布尔型配置值
     */
    public Boolean getConfigValueAsBoolean(String key, Boolean defaultValue) {
        String value = getConfigValue(key, null);
        Boolean result = value != null ? Boolean.parseBoolean(value) : defaultValue;
        logger.debug("🔘 布尔型配置值获取 - key: {}, value: {}", key, result);
        return result;
    }

    /**
     * 获取浮点型配置值
     */
    public Double getConfigValueAsDouble(String key, Double defaultValue) {
        try {
            String value = getConfigValue(key, null);
            if (value != null) {
                Double result = Double.parseDouble(value);
                logger.debug("📊 浮点型配置值获取 - key: {}, value: {}", key, result);
                return result;
            }
        } catch (NumberFormatException e) {
            logger.warn("❌ 配置值转换失败 - key: {}, 使用默认值: {}, 错误: {}", key, defaultValue, e.getMessage());
        }
        return defaultValue;
    }

    // ========== 配置值设置方法 ==========

    /**
     * 设置配置值
     */
    public SystemConfig setConfigValue(String key, String value, String description, String configType) {
        logger.info("⚙️ 设置配置值 - key: {}, value: {}, type: {}", key, value, configType);

        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(key);

        SystemConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.updateValue(value);
            config.setDescription(description);
            config.setConfigType(configType);
            logger.debug("🔄 更新现有配置 - key: {}", key);
        } else {
            config = new SystemConfig(key, value, configType, description);
            logger.debug("🆕 创建新配置 - key: {}", key);
        }

        SystemConfig savedConfig = systemConfigRepository.save(config);
        logger.info("✅ 配置值设置成功 - key: {}, value: {}", key, value);

        return savedConfig;
    }

    /**
     * 批量设置配置值
     */
    public List<SystemConfig> setConfigValues(Map<String, String> configMap, String configType) {
        logger.info("📦 批量设置配置值 - 数量: {}, type: {}", configMap.size(), configType);

        List<SystemConfig> savedConfigs = new ArrayList<>();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String description = getDefaultDescription(key);

            SystemConfig savedConfig = setConfigValue(key, value, description, configType);
            savedConfigs.add(savedConfig);
        }

        logger.info("✅ 批量设置配置值完成 - 成功数量: {}", savedConfigs.size());
        return savedConfigs;
    }

    // ========== 批量操作方法 ==========

    /**
     * 批量获取配置值
     */
    public Map<String, String> getConfigValues(List<String> keys) {
        logger.debug("🔍 批量获取配置值 - keys: {}", keys);

        List<SystemConfig> configs = systemConfigRepository.findByConfigKeys(keys);
        Map<String, String> result = configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        SystemConfig::getConfigValue
                ));

        logger.debug("✅ 批量获取配置值完成 - 结果数量: {}", result.size());
        return result;
    }

    /**
     * 获取所有系统参数配置
     */
    public Map<String, Object> getAllSystemParams() {
        logger.info("🔧 获取所有系统参数配置");

        List<SystemConfig> params = systemConfigRepository.findByConfigType("SYSTEM_PARAM");
        Map<String, Object> result = new HashMap<>();

        for (SystemConfig param : params) {
            String key = param.getConfigKey();
            String value = param.getConfigValue();

            // 根据键名进行智能类型转换
            Object convertedValue = convertConfigValue(key, value);
            result.put(key, convertedValue);
        }

        logger.info("✅ 获取系统参数配置完成 - 参数数量: {}", result.size());
        return result;
    }

    /**
     * 根据配置类型获取配置
     */
    public List<SystemConfig> getConfigsByType(String configType) {
        logger.debug("🔍 根据类型获取配置 - type: {}", configType);

        List<SystemConfig> configs = systemConfigRepository.findByConfigType(configType);
        logger.debug("✅ 根据类型获取配置完成 - 数量: {}", configs.size());

        return configs;
    }

    // ========== 删除方法 ==========

    /**
     * 删除配置
     */
    public boolean deleteConfig(String key) {
        logger.info("🗑️ 删除配置 - key: {}", key);

        try {
            int deleted = systemConfigRepository.deleteByConfigKey(key);
            boolean success = deleted > 0;

            if (success) {
                logger.info("✅ 配置删除成功 - key: {}", key);
            } else {
                logger.warn("⚠️ 配置不存在，删除失败 - key: {}", key);
            }

            return success;
        } catch (Exception e) {
            logger.error("❌ 配置删除失败 - key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    // ========== 初始化方法 ==========

    /**
     * 初始化默认配置
     */
    @PostConstruct
    public void initializeDefaultConfigs() {
        logger.info("🔧 开始初始化系统默认配置...");

        try {
            // 系统参数配置
            Map<String, String> systemParams = Map.of(
                    "data.retention.days", "30",
                    "auto.refresh.interval", "30",
                    "max.history.records", "1000",
                    "alert.notification.enabled", "true",
                    "mock.data.generation", "true",
                    "system.health.check", "ok"
            );

            // 用户配置
            Map<String, String> userConfigs = Map.of(
                    "user.default.role", "VIEWER",
                    "user.auto.logout.minutes", "60",
                    "user.session.timeout", "30"
            );

            // 批量设置系统参数
            setConfigValues(systemParams, "SYSTEM_PARAM");

            // 批量设置用户配置
            setConfigValues(userConfigs, "USER_CONFIG");

            logger.info("✅ 系统默认配置初始化完成");

        } catch (Exception e) {
            logger.error("❌ 系统默认配置初始化失败: {}", e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 配置值类型转换
     */
    private Object convertConfigValue(String key, String value) {
        if (value == null) return null;

        try {
            if (key.contains("interval") || key.contains("timeout") || key.contains("retention") || key.contains("minutes")) {
                return Integer.parseInt(value);
            } else if (key.contains("enabled") || key.contains("auto") || key.contains("notification")) {
                return Boolean.parseBoolean(value);
            } else if (key.contains("percentage") || key.contains("ratio") || key.contains("rate")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            logger.warn("⚠️ 配置值类型转换失败 - key: {}, value: {}, 保持字符串类型", key, value);
        }

        return value;
    }

    /**
     * 获取默认描述
     */
    private String getDefaultDescription(String key) {
        Map<String, String> descriptions = Map.of(
                "data.retention.days", "数据保留天数",
                "auto.refresh.interval", "自动刷新间隔(秒)",
                "max.history.records", "最大历史记录数",
                "alert.notification.enabled", "启用告警通知",
                "mock.data.generation", "启用模拟数据生成",
                "user.default.role", "默认用户角色",
                "user.auto.logout.minutes", "自动注销时间(分钟)",
                "user.session.timeout", "会话超时时间(分钟)",
                "system.health.check", "系统健康检查标识"
        );
        return descriptions.getOrDefault(key, "系统配置参数");
    }

    /**
     * 健康检查
     */
    public boolean performHealthCheck() {
        logger.info("🏥 系统配置服务健康检查...");

        try {
            long configCount = systemConfigRepository.count();
            List<String> allKeys = systemConfigRepository.findAllConfigKeys();

            boolean healthStatus = configCount > 0 && !allKeys.isEmpty();

            if (healthStatus) {
                logger.info("✅ 系统配置服务健康检查通过 - 配置数量: {}, 键数量: {}", configCount, allKeys.size());
            } else {
                logger.warn("⚠️ 系统配置服务健康检查警告 - 配置数量: {}", configCount);
            }

            return healthStatus;
        } catch (Exception e) {
            logger.error("❌ 系统配置服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}