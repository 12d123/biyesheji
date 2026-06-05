package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.dto.ApiResponse;
import com.hqh.warehouse_backend.entity.AlertThreshold;
import com.hqh.warehouse_backend.entity.SystemConfig;
import com.hqh.warehouse_backend.service.AlertThresholdService;
import com.hqh.warehouse_backend.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 处理系统参数配置、告警阈值配置等操作
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "http://localhost:5173")
public class SystemConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigController.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AlertThresholdService alertThresholdService;

    // ========== 系统参数配置接口 ==========

    /**
     * 获取所有系统参数配置
     */
    @GetMapping("/params")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemParams() {
        logger.info("🔧 收到获取系统参数配置请求");

        try {
            Map<String, Object> systemParams = systemConfigService.getAllSystemParams();

            logger.info("✅ 获取系统参数配置成功 - 参数数量: {}", systemParams.size());
            return ResponseEntity.ok(ApiResponse.success("获取系统参数成功", systemParams));

        } catch (Exception e) {
            logger.error("❌ 获取系统参数配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取系统参数失败: " + e.getMessage()));
        }
    }

    /**
     * 更新系统参数配置
     */
    @PostMapping("/params")
    public ResponseEntity<ApiResponse<String>> updateSystemParams(@RequestBody Map<String, Object> params) {
        logger.info("🔧 收到更新系统参数配置请求 - 参数数量: {}", params.size());

        try {
            int updatedCount = 0;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = String.valueOf(entry.getValue());

                // 根据键名确定配置类型和描述
                String configType = "SYSTEM_PARAM";
                String description = getParamDescription(key);

                systemConfigService.setConfigValue(key, value, description, configType);
                updatedCount++;
            }

            logger.info("✅ 系统参数配置更新成功 - 更新数量: {}", updatedCount);
            return ResponseEntity.ok(ApiResponse.success("系统参数配置更新成功"));

        } catch (Exception e) {
            logger.error("❌ 更新系统参数配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("更新系统参数失败: " + e.getMessage()));
        }
    }

    /**
     * 获取单个系统参数
     */
    @GetMapping("/params/{key}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSystemParam(@PathVariable String key,
                                                                           @RequestParam(required = false) String defaultValue) {
        logger.info("🔧 收到获取单个系统参数请求 - key: {}", key);

        try {
            String value = systemConfigService.getConfigValue(key, defaultValue);

            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);

            logger.info("✅ 获取单个系统参数成功 - key: {}, value: {}", key, value);
            return ResponseEntity.ok(ApiResponse.success("获取系统参数成功", result));

        } catch (Exception e) {
            logger.error("❌ 获取单个系统参数失败 - key: {}, 错误: {}", key, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取系统参数失败: " + e.getMessage()));
        }
    }

    // ========== 告警阈值配置接口 ==========

    /**
     * 获取所有告警阈值配置
     */
    @GetMapping("/thresholds")
    public ResponseEntity<ApiResponse<List<AlertThreshold>>> getAlertThresholds() {
        logger.info("🔔 收到获取告警阈值配置请求");

        try {
            List<AlertThreshold> thresholds = alertThresholdService.getAllThresholds();

            logger.info("✅ 获取告警阈值配置成功 - 配置数量: {}", thresholds.size());
            return ResponseEntity.ok(ApiResponse.success("获取告警阈值成功", thresholds));

        } catch (Exception e) {
            logger.error("❌ 获取告警阈值配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取告警阈值失败: " + e.getMessage()));
        }
    }

    /**
     * 获取启用的告警阈值配置
     */
    @GetMapping("/thresholds/enabled")
    public ResponseEntity<ApiResponse<List<AlertThreshold>>> getEnabledAlertThresholds() {
        logger.info("🔔 收到获取启用的告警阈值配置请求");

        try {
            List<AlertThreshold> thresholds = alertThresholdService.getEnabledThresholds();

            logger.info("✅ 获取启用的告警阈值配置成功 - 配置数量: {}", thresholds.size());
            return ResponseEntity.ok(ApiResponse.success("获取启用的告警阈值成功", thresholds));

        } catch (Exception e) {
            logger.error("❌ 获取启用的告警阈值配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取启用的告警阈值失败: " + e.getMessage()));
        }
    }

    /**
     * 更新告警阈值配置
     */
    @PostMapping("/thresholds")
    public ResponseEntity<ApiResponse<List<AlertThreshold>>> updateAlertThresholds(@RequestBody List<AlertThreshold> thresholds) {
        logger.info("🔔 收到更新告警阈值配置请求 - 配置数量: {}", thresholds.size());

        try {
            List<AlertThreshold> savedThresholds = alertThresholdService.saveAllThresholds(thresholds);

            logger.info("✅ 告警阈值配置更新成功 - 更新数量: {}", savedThresholds.size());
            return ResponseEntity.ok(ApiResponse.success("告警阈值配置更新成功", savedThresholds));

        } catch (Exception e) {
            logger.error("❌ 更新告警阈值配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("更新告警阈值失败: " + e.getMessage()));
        }
    }

    /**
     * 更新单个告警阈值配置
     */
    @PutMapping("/thresholds/{id}")
    public ResponseEntity<ApiResponse<AlertThreshold>> updateAlertThreshold(@PathVariable Long id, @RequestBody AlertThreshold threshold) {
        logger.info("🔔 收到更新单个告警阈值配置请求 - ID: {}", id);

        try {
            // 确保ID一致
            threshold.setId(id);
            AlertThreshold savedThreshold = alertThresholdService.saveThreshold(threshold);

            logger.info("✅ 单个告警阈值配置更新成功 - ID: {}, location: {}", id, threshold.getLocation());
            return ResponseEntity.ok(ApiResponse.success("告警阈值配置更新成功", savedThreshold));

        } catch (Exception e) {
            logger.error("❌ 更新单个告警阈值配置失败 - ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("更新告警阈值失败: " + e.getMessage()));
        }
    }

    /**
     * 启用/禁用告警阈值
     */
    @PostMapping("/thresholds/{id}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleThreshold(@PathVariable Long id, @RequestParam boolean enabled) {
        logger.info("🔔 收到{}告警阈值配置请求 - ID: {}", enabled ? "启用" : "禁用", id);

        try {
            boolean success = alertThresholdService.toggleThreshold(id, enabled);

            if (success) {
                String message = enabled ? "启用告警阈值成功" : "禁用告警阈值成功";
                logger.info("✅ {} - ID: {}", message, id);
                return ResponseEntity.ok(ApiResponse.success(message));
            } else {
                logger.warn("⚠️ 操作失败，阈值配置不存在 - ID: {}", id);
                return ResponseEntity.ok(ApiResponse.error("操作失败，配置不存在"));
            }

        } catch (Exception e) {
            logger.error("❌ 切换告警阈值状态失败 - ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("切换告警阈值状态失败: " + e.getMessage()));
        }
    }

    // ========== 系统健康状态接口 ==========

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        logger.info("🏥 收到获取系统健康状态请求");

        try {
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("timestamp", java.time.LocalDateTime.now());
            healthInfo.put("status", "healthy");

            // 检查配置服务状态
            boolean configServiceHealthy = systemConfigService.performHealthCheck();
            healthInfo.put("configService", configServiceHealthy ? "正常" : "异常");

            // 检查阈值服务状态
            boolean thresholdServiceHealthy = alertThresholdService.performHealthCheck();
            healthInfo.put("thresholdService", thresholdServiceHealthy ? "正常" : "异常");

            // 系统参数统计
            Map<String, Object> systemParams = systemConfigService.getAllSystemParams();
            healthInfo.put("systemParamsCount", systemParams.size());

            // 阈值统计
            Map<String, Object> thresholdStats = alertThresholdService.getThresholdStats();
            healthInfo.put("thresholdStats", thresholdStats);

            // 总体健康状态
            boolean overallHealth = configServiceHealthy && thresholdServiceHealthy;
            healthInfo.put("overallHealth", overallHealth ? "健康" : "异常");

            logger.info("✅ 系统健康状态检查完成 - 总体状态: {}", healthInfo.get("overallHealth"));
            return ResponseEntity.ok(ApiResponse.success("系统健康状态获取成功", healthInfo));

        } catch (Exception e) {
            logger.error("❌ 获取系统健康状态失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取系统健康状态失败: " + e.getMessage()));
        }
    }

    /**
     * 数据验证接口
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateData(@RequestBody Map<String, Object> data) {
        logger.info("🔬 收到数据验证请求 - data: {}", data);

        try {
            String location = (String) data.get("location");
            Double temperature = Double.valueOf(data.get("temperature").toString());
            Double humidity = Double.valueOf(data.get("humidity").toString());

            Map<String, Object> validationResult = alertThresholdService.validateData(location, temperature, humidity);

            logger.info("✅ 数据验证完成 - location: {}, 结果: {}", location, validationResult.get("valid"));
            return ResponseEntity.ok(ApiResponse.success("数据验证完成", validationResult));

        } catch (Exception e) {
            logger.error("❌ 数据验证失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("数据验证失败: " + e.getMessage()));
        }
    }

    // ========== 系统管理接口 ==========

    /**
     * 重置系统配置到默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetToDefaults() {
        logger.info("🔄 收到重置系统配置到默认值请求");

        try {
            // 重新初始化默认配置
            systemConfigService.initializeDefaultConfigs();
            alertThresholdService.initializeDefaultThresholds();

            logger.info("✅ 系统配置重置成功");
            return ResponseEntity.ok(ApiResponse.success("系统配置已重置到默认值"));

        } catch (Exception e) {
            logger.error("❌ 重置系统配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("重置系统配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        logger.info("📊 收到获取系统统计信息请求");

        try {
            Map<String, Object> stats = new HashMap<>();

            // 系统配置统计
            Map<String, Object> systemParams = systemConfigService.getAllSystemParams();
            stats.put("systemParamsCount", systemParams.size());

            // 阈值配置统计
            Map<String, Object> thresholdStats = alertThresholdService.getThresholdStats();
            stats.put("thresholdStats", thresholdStats);

            // 服务状态
            boolean configServiceHealthy = systemConfigService.performHealthCheck();
            boolean thresholdServiceHealthy = alertThresholdService.performHealthCheck();
            stats.put("configServiceHealthy", configServiceHealthy);
            stats.put("thresholdServiceHealthy", thresholdServiceHealthy);

            stats.put("timestamp", java.time.LocalDateTime.now());

            logger.info("✅ 获取系统统计信息成功");
            return ResponseEntity.ok(ApiResponse.success("系统统计信息获取成功", stats));

        } catch (Exception e) {
            logger.error("❌ 获取系统统计信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取系统统计信息失败: " + e.getMessage()));
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 获取参数描述
     */
    private String getParamDescription(String key) {
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
        return descriptions.getOrDefault(key, "系统参数");
    }
}