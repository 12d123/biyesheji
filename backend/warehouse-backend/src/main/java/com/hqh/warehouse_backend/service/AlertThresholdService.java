package com.hqh.warehouse_backend.service;

import com.hqh.warehouse_backend.entity.AlertThreshold;
import com.hqh.warehouse_backend.repository.AlertThresholdRepository;
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
public class AlertThresholdService {

    private static final Logger logger = LoggerFactory.getLogger(AlertThresholdService.class);

    @Autowired
    private AlertThresholdRepository alertThresholdRepository;

    // ========== 查询方法 ==========

    /**
     * 获取所有告警阈值配置
     */
    public List<AlertThreshold> getAllThresholds() {
        logger.debug("🔍 获取所有告警阈值配置");

        List<AlertThreshold> thresholds = alertThresholdRepository.findAll();
        logger.debug("✅ 获取告警阈值配置完成 - 数量: {}", thresholds.size());

        return thresholds;
    }

    /**
     * 获取启用的告警阈值配置
     */
    public List<AlertThreshold> getEnabledThresholds() {
        logger.debug("🔍 获取启用的告警阈值配置");

        List<AlertThreshold> thresholds = alertThresholdRepository.findByEnabledTrue();
        logger.debug("✅ 获取启用的告警阈值配置完成 - 数量: {}", thresholds.size());

        return thresholds;
    }

    /**
     * 根据位置获取阈值配置
     */
    public AlertThreshold getThresholdByLocation(String location) {
        logger.debug("🔍 根据位置获取阈值配置 - location: {}", location);

        Optional<AlertThreshold> threshold = alertThresholdRepository.findByLocation(location);
        AlertThreshold result = threshold.orElse(null);

        if (result != null) {
            logger.debug("✅ 找到阈值配置 - location: {}", location);
        } else {
            logger.debug("⚠️ 未找到阈值配置 - location: {}", location);
        }

        return result;
    }

    /**
     * 根据位置列表获取阈值配置
     */
    public List<AlertThreshold> getThresholdsByLocations(List<String> locations) {
        logger.debug("🔍 根据位置列表获取阈值配置 - locations: {}", locations);

        List<AlertThreshold> thresholds = alertThresholdRepository.findByLocations(locations);
        logger.debug("✅ 根据位置列表获取阈值配置完成 - 数量: {}", thresholds.size());

        return thresholds;
    }

    // ========== 保存和更新方法 ==========

    /**
     * 创建或更新告警阈值
     */
    public AlertThreshold saveThreshold(AlertThreshold threshold) {
        logger.info("💾 保存告警阈值配置 - location: {}", threshold.getLocation());

        Optional<AlertThreshold> existing = alertThresholdRepository.findByLocation(threshold.getLocation());

        AlertThreshold savedThreshold;
        if (existing.isPresent()) {
            AlertThreshold existingThreshold = existing.get();
            existingThreshold.updateThreshold(
                    threshold.getMinTemperature(),
                    threshold.getMaxTemperature(),
                    threshold.getMinHumidity(),
                    threshold.getMaxHumidity(),
                    threshold.getDescription()
            );
            existingThreshold.setEnabled(threshold.getEnabled());
            savedThreshold = alertThresholdRepository.save(existingThreshold);
            logger.debug("🔄 更新现有阈值配置 - location: {}", threshold.getLocation());
        } else {
            threshold.setUpdatedTime(LocalDateTime.now());
            savedThreshold = alertThresholdRepository.save(threshold);
            logger.debug("🆕 创建新阈值配置 - location: {}", threshold.getLocation());
        }

        logger.info("✅ 保存告警阈值配置成功 - location: {}", threshold.getLocation());
        return savedThreshold;
    }

    /**
     * 批量保存阈值配置
     */
    public List<AlertThreshold> saveAllThresholds(List<AlertThreshold> thresholds) {
        logger.info("📦 批量保存阈值配置 - 数量: {}", thresholds.size());

        List<AlertThreshold> savedThresholds = new ArrayList<>();
        for (AlertThreshold threshold : thresholds) {
            try {
                AlertThreshold savedThreshold = saveThreshold(threshold);
                savedThresholds.add(savedThreshold);
            } catch (Exception e) {
                logger.error("❌ 保存单个阈值配置失败 - location: {}, 错误: {}", threshold.getLocation(), e.getMessage());
                // 继续处理其他配置，不中断
            }
        }

        logger.info("✅ 批量保存阈值配置完成 - 成功数量: {}", savedThresholds.size());
        return savedThresholds;
    }

    /**
     * 更新阈值范围
     */
    public boolean updateThresholdRange(Long id, Double minTemp, Double maxTemp, Double minHum, Double maxHum, String description) {
        logger.info("📐 更新阈值范围 - ID: {}, 温度: {}-{}, 湿度: {}-{}", id, minTemp, maxTemp, minHum, maxHum);

        try {
            int updated = alertThresholdRepository.updateThresholdRange(id, minTemp, maxTemp, minHum, maxHum, description, LocalDateTime.now());
            boolean success = updated > 0;

            if (success) {
                logger.info("✅ 阈值范围更新成功 - ID: {}", id);
            } else {
                logger.warn("⚠️ 阈值范围更新失败，配置不存在 - ID: {}", id);
            }

            return success;
        } catch (Exception e) {
            logger.error("❌ 阈值范围更新失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    // ========== 状态管理方法 ==========

    /**
     * 启用/禁用阈值配置
     */
    public boolean toggleThreshold(Long id, boolean enabled) {
        logger.info("🔘 {}阈值配置 - ID: {}", enabled ? "启用" : "禁用", id);

        try {
            int updated = alertThresholdRepository.updateEnabledStatus(id, enabled, LocalDateTime.now());
            boolean success = updated > 0;

            if (success) {
                logger.info("✅ {}阈值配置成功 - ID: {}", enabled ? "启用" : "禁用", id);
            } else {
                logger.warn("⚠️ {}阈值配置失败，配置不存在 - ID: {}", enabled ? "启用" : "禁用", id);
            }

            return success;
        } catch (Exception e) {
            logger.error("❌ 切换阈值配置状态失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 批量启用/禁用阈值配置
     */
    public Map<Long, Boolean> toggleThresholds(List<Long> ids, boolean enabled) {
        logger.info("🔘 批量{}阈值配置 - IDs: {}", enabled ? "启用" : "禁用", ids);

        Map<Long, Boolean> results = new HashMap<>();
        for (Long id : ids) {
            boolean success = toggleThreshold(id, enabled);
            results.put(id, success);
        }

        long successCount = results.values().stream().filter(Boolean::booleanValue).count();
        logger.info("✅ 批量{}阈值配置完成 - 总数: {}, 成功: {}", enabled ? "启用" : "禁用", ids.size(), successCount);

        return results;
    }

    // ========== 验证方法 ==========

    /**
     * 验证数据是否正常
     */
    public Map<String, Object> validateData(String location, Double temperature, Double humidity) {
        logger.debug("🔬 验证数据 - location: {}, temp: {}, humidity: {}", location, temperature, humidity);

        Map<String, Object> result = new HashMap<>();
        AlertThreshold threshold = getThresholdByLocation(location);

        if (threshold == null) {
            result.put("valid", false);
            result.put("message", "未找到该位置的阈值配置");
            result.put("level", "warning");
            return result;
        }

        if (!threshold.getEnabled()) {
            result.put("valid", true);
            result.put("message", "该位置的阈值检查已禁用");
            result.put("level", "info");
            return result;
        }

        boolean tempNormal = threshold.isTemperatureNormal(temperature);
        boolean humNormal = threshold.isHumidityNormal(humidity);

        result.put("valid", tempNormal && humNormal);
        result.put("temperatureNormal", tempNormal);
        result.put("humidityNormal", humNormal);
        result.put("threshold", threshold);

        if (!tempNormal || !humNormal) {
            List<String> messages = new ArrayList<>();
            if (!tempNormal) {
                messages.add(String.format("温度异常: %.1f℃ (范围: %.1f-%.1f℃)",
                        temperature, threshold.getMinTemperature(), threshold.getMaxTemperature()));
            }
            if (!humNormal) {
                messages.add(String.format("湿度异常: %.1f%% (范围: %.1f-%.1f%%)",
                        humidity, threshold.getMinHumidity(), threshold.getMaxHumidity()));
            }
            result.put("messages", messages);
            result.put("level", "danger");
        } else {
            result.put("message", "数据正常");
            result.put("level", "success");
        }

        logger.debug("✅ 数据验证完成 - location: {}, 结果: {}", location, result.get("valid"));
        return result;
    }

    // ========== 统计方法 ==========

    /**
     * 获取阈值统计信息
     */
    public Map<String, Object> getThresholdStats() {
        logger.debug("📊 获取阈值统计信息");

        Map<String, Object> stats = new HashMap<>();

        long totalThresholds = alertThresholdRepository.count();
        long enabledThresholds = alertThresholdRepository.countByEnabledTrue();
        List<String> allLocations = alertThresholdRepository.findAllLocations();

        stats.put("totalThresholds", totalThresholds);
        stats.put("enabledThresholds", enabledThresholds);
        stats.put("disabledThresholds", totalThresholds - enabledThresholds);
        stats.put("allLocations", allLocations);
        stats.put("locationsCount", allLocations.size());
        stats.put("timestamp", LocalDateTime.now());

        logger.debug("✅ 获取阈值统计信息完成");
        return stats;
    }

    // ========== 初始化方法 ==========

    /**
     * 初始化默认告警阈值
     */
    @PostConstruct
    public void initializeDefaultThresholds() {
        logger.info("🔔 开始初始化默认告警阈值配置...");

        try {
            // 定义默认阈值配置
            List<AlertThreshold> defaultThresholds = Arrays.asList(
                    new AlertThreshold("A区仓库", 18.0, 26.0, 40.0, 60.0, "人体最适宜环境"),
                    new AlertThreshold("B区仓库", 18.0, 26.0, 40.0, 60.0, "人体最适宜环境"),
                    new AlertThreshold("C区仓库", 18.0, 26.0, 40.0, 60.0, "人体最适宜环境"),
                    new AlertThreshold("贵重物品区", 16.0, 20.0, 30.0, 45.0, "低温干燥环境"),
                    new AlertThreshold("冷冻库", -25.0, -15.0, 20.0, 40.0, "冷冻干燥环境"),
                    new AlertThreshold("常温库", 15.0, 25.0, 30.0, 50.0, "常温干燥环境")
            );

            int createdCount = 0;
            for (AlertThreshold threshold : defaultThresholds) {
                if (!alertThresholdRepository.existsByLocation(threshold.getLocation())) {
                    alertThresholdRepository.save(threshold);
                    createdCount++;
                    logger.debug("✅ 创建默认阈值配置 - {}", threshold.getLocation());
                }
            }

            logger.info("✅ 默认告警阈值配置初始化完成 - 新建配置: {}, 总计配置: {}",
                    createdCount, defaultThresholds.size());

        } catch (Exception e) {
            logger.error("❌ 默认告警阈值配置初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    public boolean performHealthCheck() {
        logger.info("🏥 告警阈值服务健康检查...");

        try {
            long totalThresholds = alertThresholdRepository.count();
            long enabledThresholds = alertThresholdRepository.countByEnabledTrue();
            List<String> allLocations = alertThresholdRepository.findAllLocations();

            boolean healthStatus = totalThresholds > 0 && !allLocations.isEmpty();

            if (healthStatus) {
                logger.info("✅ 告警阈值服务健康检查通过 - 总配置: {}, 启用配置: {}, 位置数量: {}",
                        totalThresholds, enabledThresholds, allLocations.size());
            } else {
                logger.warn("⚠️ 告警阈值服务健康检查警告 - 总配置: {}", totalThresholds);
            }

            return healthStatus;
        } catch (Exception e) {
            logger.error("❌ 告警阈值服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}