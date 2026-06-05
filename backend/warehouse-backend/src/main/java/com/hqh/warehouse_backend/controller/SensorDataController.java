package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.entity.SensorData;
import com.hqh.warehouse_backend.service.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 传感器数据控制器
 * 用于接收开发板发送的传感器数据
 */
@RestController
@RequestMapping("/api/sensor/device")
public class SensorDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(SensorDataController.class);
    
    private final SensorDataService sensorDataService;
    private final WebSocketController webSocketController;
    
    public SensorDataController(SensorDataService sensorDataService, WebSocketController webSocketController) {
        this.sensorDataService = sensorDataService;
        this.webSocketController = webSocketController;
    }
    
    /**
     * 接收开发板发送的传感器数据
     */
    @PostMapping("/data")
    public void receiveSensorData(@RequestBody SensorData sensorData) {
        try {
            // 保存数据到数据库
            SensorData savedData = sensorDataService.saveSensorData(sensorData);
            logger.info("接收到传感器数据: {}", savedData);
            
            // 通过WebSocket广播数据给前端
            webSocketController.broadcastSensorData(savedData);
        } catch (Exception e) {
            logger.error("处理传感器数据失败: {}", e.getMessage());
        }
    }
}