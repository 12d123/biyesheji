package com.hqh.warehouse_backend.controller;

import com.hqh.warehouse_backend.service.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 风扇控制控制器
 * 处理前端发送的风扇控制指令，转发到MQTT
 */
@RestController
@RequestMapping("/api/fan")
public class FanController {

    private static final Logger logger = LoggerFactory.getLogger(FanController.class);

    @Autowired
    private MqttService mqttService;

    /**
     * REST API：设置风扇状态
     * @param request 包含state字段的请求体
     * @return 响应结果
     */
    @PostMapping("/control")
    public ResponseEntity<Map<String, Object>> setFanState(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("FanController.setFanState() 方法被调用");
            System.out.println("收到的请求数据: " + request);

            Object stateObj = request.get("state");
            boolean fanState = false;

            if (stateObj instanceof Boolean) {
                fanState = (Boolean) stateObj;
            } else if (stateObj instanceof String) {
                fanState = Boolean.parseBoolean((String) stateObj);
            }

            System.out.println("解析后的风扇状态: " + fanState);

            // 将状态转为JSON格式并发布到MQTT
            // 使用QoS 1确保消息至少送达一次
            String message = "{\"on\":" + fanState + ",\"mode\":\"manual\"}";
            System.out.println("准备发送MQTT消息: topic=test1/fan, payload=" + message + ", qos=1");

            mqttService.publishWithQos("test1/fan", message, 1);
            System.out.println("风扇控制 - 状态: " + fanState + ", MQTT消息已发送到 test1/fan (QoS 1)");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", fanState ? "风扇已开启" : "风扇已关闭");
            response.put("state", fanState);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("处理风扇控制请求失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理风扇控制请求失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * REST API：设置风扇模式
     * @param request 包含mode字段的请求体（auto/manual）
     * @return 响应结果
     */
    @PostMapping("/mode")
    public ResponseEntity<Map<String, Object>> setFanMode(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("FanController.setFanMode() 方法被调用");
            System.out.println("收到的请求数据: " + request);

            Object modeObj = request.get("mode");
            String mode = "auto";

            if (modeObj instanceof String) {
                mode = (String) modeObj;
            }

            System.out.println("解析后的风扇模式: " + mode);

            // 构建消息：如果只有mode，则只切换模式；如果有on字段，则同时控制风扇
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("{\"mode\":\"").append(mode).append("\"");

            Object onObj = request.get("on");
            if (onObj != null) {
                boolean fanOn = false;
                if (onObj instanceof Boolean) {
                    fanOn = (Boolean) onObj;
                } else if (onObj instanceof String) {
                    fanOn = Boolean.parseBoolean((String) onObj);
                }
                messageBuilder.append(",\"on\":").append(fanOn);
            }

            messageBuilder.append("}");

            String message = messageBuilder.toString();
            System.out.println("准备发送MQTT消息: topic=test1/fan, payload=" + message);

            mqttService.publish("test1/fan", message);
            System.out.println("风扇模式控制 - 模式: " + mode + ", MQTT消息已发送到 test1/fan");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "模式已切换为: " + mode);
            response.put("mode", mode);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("处理风扇模式请求失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理风扇模式请求失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * WebSocket：处理风扇控制消息（保留作为备选）
     * @param fanState 风扇状态字符串（"true" 或 "false"）
     */
    @MessageMapping("/fan/control")
    public void handleFanControl(String fanState) {
        try {
            logger.info("📥 收到WebSocket风扇控制消息: fanState='{}'", fanState);
            logger.info("📥 fanState 类型: {}", fanState.getClass().getName());

            // 将字符串转为布尔值，然后构建正确的JSON格式
            boolean state = Boolean.parseBoolean(fanState);
            String message = "{\"on\":" + state + "}";
            logger.info("📤 准备发送MQTT消息: topic=test1/fan, payload={}", message);

            mqttService.publish("test1/fan", message);
            logger.info("✅ 风扇控制 - 状态: {}, MQTT消息已发送到 test1/fan", state);
        } catch (Exception e) {
            logger.error("❌ 处理风扇控制消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试方法：验证控制器是否被正确注册
     * @return 测试响应
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("FanController.test() 方法被调用");
        return ResponseEntity.ok("FanController test endpoint");
    }

    /**
     * 调试方法：测试MQTT消息发送
     * 用于诊断MQTT连接和消息发送问题
     */
    @GetMapping("/debug/mqtt")
    public ResponseEntity<Map<String, Object>> debugMqtt() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 检查MQTT客户端状态
            boolean isConnected = mqttService.isConnected();
            System.out.println("MQTT客户端连接状态: " + isConnected);

            response.put("mqttConnected", isConnected);

            if (isConnected) {
                // 发送测试消息（高QoS，确保送达）
                String testMessage = "{\"on\":true,\"mode\":\"manual\",\"test\":true}";
                System.out.println("发送MQTT测试消息: " + testMessage);

                // 使用QoS 1确保消息送达
                mqttService.publishWithQos("test1/fan", testMessage, 1);

                response.put("testMessageSent", true);
                response.put("testMessage", testMessage);
                response.put("message", "MQTT测试消息已发送（QoS 1）");
            } else {
                response.put("testMessageSent", false);
                response.put("message", "MQTT客户端未连接，无法发送测试消息");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("调试MQTT失败: " + e.getMessage());
            e.printStackTrace();

            response.put("error", e.getMessage());
            response.put("message", "调试MQTT失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}