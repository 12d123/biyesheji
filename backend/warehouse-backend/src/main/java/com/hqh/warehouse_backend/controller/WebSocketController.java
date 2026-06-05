package com.hqh.warehouse_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 广播传感器数据给所有连接的客户端
    public void broadcastSensorData(Object data) {
        try {
            messagingTemplate.convertAndSend("/topic/sensor/data", data);
            logger.info("广播传感器数据: {}", data);
        } catch (Exception e) {
            logger.error("广播传感器数据失败: {}", e.getMessage());
        }
    }

    // 处理客户端发送的消息（如果需要）
    @MessageMapping("/sensor/config")
    @SendTo("/topic/sensor/config")
    public Object handleConfigMessage(Object config) {
        logger.info("收到配置消息: {}", config);
        return config;
    }
}
