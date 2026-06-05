package com.hqh.warehouse_backend.service;

import com.hqh.warehouse_backend.entity.SensorData;
import com.hqh.warehouse_backend.repository.SensorDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensorDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(SensorDataService.class);
    
    @Autowired
    private SensorDataRepository sensorDataRepository;
    
    private Random random = new Random();
    
    // 保存传感器数据
    public SensorData saveSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }
    
    // 生成模拟传感器数据并保存 - 修复：使用随机位置
public SensorData generateAndSaveMockData() {
    double temperature = Math.round((20 + random.nextDouble() * 10) * 100.0) / 100.0;
    double humidity = Math.round((40 + random.nextDouble() * 40) * 100.0) / 100.0;
    double pm25 = Math.round(random.nextDouble() * 100 * 100.0) / 100.0; // 0-100 μg/m³
    double smoke = Math.round(random.nextDouble() * 1000 * 100.0) / 100.0; // 0-1000 ppm烟雾浓度

    String[] locations = {"A区仓库", "B区仓库", "C区仓库", "冷冻库", "常温库", "贵重物品区"};
    String location = locations[random.nextInt(locations.length)];

    logger.info("🎲 生成模拟数据 - 位置: {}, 温度: {:.2f}℃, 湿度: {:.2f}%, PM2.5: {:.2f}, 烟雾: {:.1f} ppm",
            location, temperature, humidity, pm25, smoke);
    
    SensorData sensorData = new SensorData(temperature, humidity, location);
    sensorData.setPm25(pm25);
    sensorData.setSmoke(smoke);
    return sensorDataRepository.save(sensorData);
}
    
    // 批量生成模拟数据 - 新增方法
    public List<SensorData> generateBatchMockData(int count) {
        List<SensorData> mockDataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(100); // 添加延迟以创建不同的时间戳
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            SensorData mockData = generateAndSaveMockData();
            mockDataList.add(mockData);
        }
        return mockDataList;
    }
    
    // 获取所有数据
    public List<SensorData> getAllData() {
        return sensorDataRepository.findAll();
    }
    
    // 获取最新数据
    public SensorData getLatestData() {
        SensorData data = sensorDataRepository.findTopByOrderByCreatedTimeDesc();
        if (data == null) {
            // 如果没有数据，生成一条模拟数据
            return generateAndSaveMockData();
        }
        return data;
    }
    
    // 获取历史数据（最近10条，所有位置）
    public List<SensorData> getRecentData() {
        return sensorDataRepository.findTop10ByOrderByCreatedTimeDesc();
    }
    
    // 获取今日数据
    public List<SensorData> getTodayData() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return sensorDataRepository.findByCreatedTimeBetween(startOfDay, endOfDay);
    }
    
    // 统计数据
    public long getDataCount() {
        return sensorDataRepository.count();
    }
    
    // 修复：方法名改为 getStatistics（去掉多余的 "s"）
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalRecords = sensorDataRepository.countTotalRecords();
        
        // 计算今日记录数
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Long todayRecords = sensorDataRepository.countTodayRecords(startOfDay, endOfDay);
        
        stats.put("totalRecords", totalRecords != null ? totalRecords : 0);
        stats.put("todayRecords", todayRecords != null ? todayRecords : 0);
        stats.put("latestUpdate", LocalDateTime.now());
        
        logger.info("📊 统计信息 - 总记录: {}, 今日记录: {}", totalRecords, todayRecords);
        
        return stats;
    }
    
    // 根据位置获取数据
    public List<SensorData> getDataByLocation(String location) {
        return sensorDataRepository.findByLocation(location);
    }
    
    // 获取异常温度数据 - 新增方法
public List<SensorData> getAbnormalData() {
    List<SensorData> allData = sensorDataRepository.findAll();
    return allData.stream()
            .filter(data ->
                data.getTemperature() < 15.0 || data.getTemperature() > 30.0 ||      // 温度异常
                data.getHumidity() < 30.0 || data.getHumidity() > 70.0 ||            // 湿度异常
                (data.getPm25() != null && data.getPm25() > 50.0) ||                 // PM2.5过高（可调阈值）
                (data.getSmoke() != null && data.getSmoke() >= 800.0)                // 烟雾浓度过高（>=800ppm）
            )
            .collect(Collectors.toList());
}
    
    // 清空所有数据 - 增强版本
    @Transactional
    public int clearAllData() {
        try {
            logger.info("开始清空所有传感器数据...");
            
            // 方法1: 使用 deleteAll (简单但可能效率较低)
            long countBefore = sensorDataRepository.count();
            sensorDataRepository.deleteAll();
            long countAfter = sensorDataRepository.count();
            
            int deletedCount = (int) (countBefore - countAfter);
            logger.info("清空数据完成，删除了 {} 条记录", deletedCount);
            
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("清空数据时发生错误: {}", e.getMessage());
            throw new RuntimeException("清空数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除单条传感器数据
     */
    @Transactional
    public boolean deleteData(Long id) {
        try {
            logger.info("尝试删除传感器数据，ID: {}", id);
            
            if (id == null) {
                logger.warn("删除数据失败: ID不能为空");
                return false;
            }
            
            // 检查数据是否存在
            if (!sensorDataRepository.existsById(id)) {
                logger.warn("删除数据失败: 数据不存在，ID: {}", id);
                return false;
            }
            
            // 执行删除
            sensorDataRepository.deleteById(id);
            logger.info("删除数据成功，ID: {}", id);
            
            return true;
            
        } catch (Exception e) {
            logger.error("删除数据时发生错误，ID: {}, 错误: {}", id, e.getMessage());
            throw new RuntimeException("删除数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量删除传感器数据
     */
    @Transactional
    public int deleteBatchData(List<Long> ids) {
        try {
            logger.info("开始批量删除传感器数据，ID列表: {}", ids);
            
            if (ids == null || ids.isEmpty()) {
                logger.warn("批量删除失败: ID列表为空");
                return 0;
            }
            
            // 过滤掉null值
            List<Long> validIds = ids.stream()
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            
            if (validIds.isEmpty()) {
                logger.warn("批量删除失败: 没有有效的ID");
                return 0;
            }
            
            // 检查哪些ID确实存在
            List<Long> existingIds = new ArrayList<>();
            for (Long id : validIds) {
                if (sensorDataRepository.existsById(id)) {
                    existingIds.add(id);
                }
            }
            
            if (existingIds.isEmpty()) {
                logger.warn("批量删除失败: 没有找到对应的数据");
                return 0;
            }
            
            // 执行批量删除
            int deletedCount = 0;
            for (Long id : existingIds) {
                try {
                    sensorDataRepository.deleteById(id);
                    deletedCount++;
                } catch (Exception e) {
                    logger.error("删除单个数据失败，ID: {}, 错误: {}", id, e.getMessage());
                    // 继续删除其他数据，不中断
                }
            }
            
            logger.info("批量删除完成，成功删除 {} 条数据", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("批量删除数据时发生错误: {}", e.getMessage());
            throw new RuntimeException("批量删除失败: " + e.getMessage(), e);
        }
    }
}