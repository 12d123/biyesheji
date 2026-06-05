package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.entity.SensorData;
import com.hqh.warehouse_backend.service.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensor")
public class SensorController {
    
    private static final Logger logger = LoggerFactory.getLogger(SensorController.class);
    
    @Autowired
    private SensorDataService sensorDataService;
    
    // 获取当前传感器数据
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentData() {
        logger.info("🔍 收到 /sensor/current 请求");
        
        try {
            SensorData latestData = sensorDataService.getLatestData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("temperature", latestData.getTemperature());
            response.put("humidity", latestData.getHumidity());
            response.put("location", latestData.getLocation());
            response.put("timestamp", latestData.getCreatedTime());
            response.put("sensorType", latestData.getSensorType());
            response.put("message", "数据获取成功");
            
            logger.info("✅ 返回最新数据: {} - {:.1f}°C, {:.1f}%", 
                latestData.getLocation(), latestData.getTemperature(), latestData.getHumidity());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 获取当前数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 手动添加传感器数据（用于测试）
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> addSensorData(@RequestBody Map<String, Object> data) {
    logger.info("🔍 收到 /sensor/data 请求: {}", data);
    
    try {
        Double temperature = Double.valueOf(data.get("temperature").toString());
        Double humidity = Double.valueOf(data.get("humidity").toString());
        String location = data.getOrDefault("location", "A区仓库").toString();
        String sensorType = data.getOrDefault("sensorType", "DHT11").toString();
        
        // 提取新字段
        Double pm25 = null;
        if (data.containsKey("pm25")) {
            pm25 = Double.valueOf(data.get("pm25").toString());
        }
        Double smoke = null;
        if (data.containsKey("smoke")) {
            Object smokeObj = data.get("smoke");
            if (smokeObj instanceof Number) {
                smoke = ((Number) smokeObj).doubleValue();
            } else {
                smoke = Double.valueOf(smokeObj.toString());
            }
        }

        SensorData sensorData = new SensorData(temperature, humidity, location, sensorType);
        sensorData.setPm25(pm25);
        sensorData.setSmoke(smoke);

        SensorData savedData = sensorDataService.saveSensorData(sensorData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", savedData);
        response.put("message", "数据添加成功");

        logger.info("✅ 添加数据成功: {} - {:.1f}°C, {:.1f}%, PM2.5: {}, 烟雾: {:.1f} ppm",
            location, temperature, humidity, pm25, smoke);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        logger.error("❌ 添加数据失败: {}", e.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "添加数据失败: " + e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
    // 生成模拟数据
    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> generateMockData() {
        logger.info("🔍 收到 /sensor/mock 请求");
        
        try {
            SensorData mockData = sensorDataService.generateAndSaveMockData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", mockData);
            response.put("message", "模拟数据生成成功");
            
            logger.info("✅ 生成模拟数据成功: {} - {:.1f}°C, {:.1f}%", 
                mockData.getLocation(), mockData.getTemperature(), mockData.getHumidity());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 生成模拟数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "生成模拟数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 批量生成模拟数据
    @PostMapping("/mock/batch")
    public ResponseEntity<Map<String, Object>> generateBatchMockData(@RequestParam(defaultValue = "5") int count) {
        logger.info("🔍 收到 /sensor/mock/batch 请求，数量: {}", count);
        
        try {
            List<SensorData> mockData = sensorDataService.generateBatchMockData(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", mockData.size());
            response.put("data", mockData);
            response.put("message", "批量模拟数据生成成功");
            
            logger.info("✅ 批量生成 {} 条模拟数据成功", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 批量生成模拟数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "批量生成模拟数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 获取历史数据
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistoryData() {
        logger.info("🔍 收到 /sensor/history 请求");
        
        try {
            List<SensorData> recentData = sensorDataService.getRecentData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", recentData.size());
            response.put("data", recentData);
            response.put("message", "历史数据获取成功");
            
            logger.info("📊 返回历史数据，共 {} 条记录", recentData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 获取历史数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取历史数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 获取今日数据
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayData() {
        logger.info("🔍 收到 /sensor/today 请求");
        
        try {
            List<SensorData> todayData = sensorDataService.getTodayData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", todayData.size());
            response.put("data", todayData);
            response.put("message", "今日数据获取成功");
            
            logger.info("📅 返回今日数据，共 {} 条记录", todayData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 获取今日数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取今日数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 获取统计信息
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        logger.info("🔍 收到 /sensor/stats 请求");
        
        try {
            Map<String, Object> stats = sensorDataService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", stats);
            response.put("message", "统计信息获取成功");
            
            logger.info("📈 返回统计信息: {}", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 获取统计信息失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 获取所有数据（用于调试）
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllData() {
        logger.info("🔍 收到 /sensor/all 请求");
        
        try {
            List<SensorData> allData = sensorDataService.getAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", allData.size());
            response.put("data", allData);
            response.put("message", "所有数据获取成功");
            
            logger.info("📋 返回所有数据，共 {} 条记录", allData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 获取所有数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取所有数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 获取异常数据
@GetMapping("/abnormal")
public ResponseEntity<Map<String, Object>> getAbnormalData() {
    logger.info("🔍 收到 /sensor/abnormal 请求");
    
    try {
        List<SensorData> abnormalData = sensorDataService.getAbnormalData(); // 调用新方法
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", abnormalData.size());
        response.put("data", abnormalData);
        response.put("message", "异常数据获取成功");
        
        logger.info("⚠️ 返回异常数据，共 {} 条记录", abnormalData.size());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        logger.error("❌ 获取异常数据失败: {}", e.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "获取异常数据失败: " + e.getMessage());
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
    
    // 清空数据（仅用于测试）
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        logger.warn("🗑️ 收到 /sensor/clear 请求");
        
        try {
            int deletedCount = sensorDataService.clearAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "所有数据已清空");
            response.put("deletedCount", deletedCount);
            
            logger.warn("✅ 数据清空完成，删除 {} 条记录", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 清空数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "清空数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // POST 方式清空所有数据（备选方案）
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllDataPost() {
        return clearAllData();
    }
    
    // 删除单条传感器数据
    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable Long id) {
        logger.info("🗑️ 收到删除单条数据请求，ID: {}", id);
        
        try {
            boolean deleted = sensorDataService.deleteData(id);
            
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("status", "success");
                response.put("message", "数据删除成功");
                response.put("deletedId", id);
                logger.info("✅ 删除数据成功，ID: {}", id);
            } else {
                response.put("status", "error");
                response.put("message", "数据不存在或删除失败");
                logger.warn("⚠️ 删除数据失败，数据不存在，ID: {}", id);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 删除数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "删除数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 批量删除传感器数据
    @PostMapping("/delete-batch")
    public ResponseEntity<Map<String, Object>> deleteBatchData(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        logger.info("🗑️ 收到批量删除请求，数据ID列表: {}", ids);
        
        try {
            if (ids == null || ids.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "请提供要删除的数据ID列表");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            int deletedCount = sensorDataService.deleteBatchData(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "成功删除 " + deletedCount + " 条数据");
            response.put("deletedCount", deletedCount);
            
            logger.info("✅ 批量删除成功，共删除 {} 条数据", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 批量删除失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "批量删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}