package com.hqh.warehouse_backend.runner;

import com.hqh.warehouse_backend.controller.WebSocketController;
import com.hqh.warehouse_backend.entity.SensorData;
import com.hqh.warehouse_backend.service.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 传感器数据模拟器
 * 定期生成模拟的传感器数据，并通过WebSocket推送给前端
 *
 * 注意：使用mqtt.enabled=false禁用此模拟器，使用真实传感器数据
 */
@Component
public class SensorDataSimulator {
    
    private static final Logger logger = LoggerFactory.getLogger(SensorDataSimulator.class);

    @Value("${mqtt.enabled:true}")
    private boolean mqttEnabled;

    private final SensorDataService sensorDataService;
    private final WebSocketController webSocketController;
    private final Random random = new Random();
    
    public SensorDataSimulator(SensorDataService sensorDataService, WebSocketController webSocketController) {
        this.sensorDataService = sensorDataService;
        this.webSocketController = webSocketController;
    }
    
    /**
     * 每10秒生成一次模拟传感器数据
     * 当mqtt.enabled=false时运行，用于测试前端功能
     * 当mqtt.enabled=true时禁用，使用真实传感器数据
     */
    @Scheduled(fixedRate = 10000)
    public void simulateSensorData() {
        try {
            // 如果MQTT启用，则不生成模拟数据
            if (mqttEnabled) {
                logger.debug("MQTT已启用，跳过模拟数据生成");
                return;
            }
            // 生成模拟数据
            double temperature = 20 + random.nextDouble() * 10; // 20-30度
            double humidity = 40 + random.nextDouble() * 30; // 40-70%
            double pm25 = 20 + random.nextDouble() * 100; // 20-120 ug/m3
            double smoke = random.nextDouble() * 1000; // 0-1000 ppm烟雾浓度
            
            // 创建传感器数据实体
            SensorData sensorData = new SensorData(
                    temperature,
                    humidity,
                    "A区仓库"
            );
            sensorData.setPm25(pm25);
            sensorData.setSmoke(smoke); // 现在是Double类型，表示烟雾浓度ppm
            sensorData.setSensorType("DHT11");
            
            // 保存数据到数据库
            SensorData savedData = sensorDataService.saveSensorData(sensorData);
            logger.info("生成模拟传感器数据: {}", savedData);
            
            // 通过WebSocket广播数据给前端
            webSocketController.broadcastSensorData(savedData);
        } catch (Exception e) {
            logger.error("生成模拟传感器数据失败: {}", e.getMessage());
        }
    }
}