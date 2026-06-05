package com.hqh.warehouse_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hqh.warehouse_backend.controller.WebSocketController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    
    @Value("${mqtt.broker:tcp://192.168.1.100:1883}")
    private String broker;

    @Value("${mqtt.clientId:warehouse-backend}")
    private String clientId;

    @Value("${mqtt.topic.data:warehouse/data}")
    private String dataTopic;

    @Value("${mqtt.topic.config:warehouse/config}")
    private String configTopic;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    private MqttClient mqttClient;
    private ExecutorService executorService;
    private ObjectMapper objectMapper;
    
    @Autowired
    private WebSocketController webSocketController;

    @PostConstruct
    public void init() {
        System.out.println("MqttService.init() 方法被调用");
        logger.info("开始初始化MQTT客户端...");
        logger.info("MQTT broker: {}", broker);
        logger.info("MQTT clientId: {}", clientId);
        logger.info("MQTT dataTopic: {}", dataTopic);
        logger.info("WebSocketController 是否初始化: {}", webSocketController != null);
        
        try {
            // 初始化ObjectMapper
            objectMapper = new ObjectMapper();
            logger.info("ObjectMapper初始化完成");
            
            // 创建MQTT客户端
            logger.info("准备创建MQTT客户端...");
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            logger.info("MQTT客户端创建成功");
            
            // 设置回调
            logger.info("设置MQTT回调...");
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT连接丢失: {}", cause.getMessage());
                    // 尝试重连
                    reconnect();
                }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
                    logger.info("========== 收到MQTT消息 ==========");
                    logger.info("主题: {}, 内容: {}", topic, new String(message.getPayload()));
                    logger.info("消息ID: {}, QoS: {}, Retained: {}, Duplicate: {}",
                                message.getId(), message.getQos(), message.isRetained(), message.isDuplicate());
                    logger.info("消息长度: {} 字节", message.getPayload().length);
                    // 处理收到的消息
                    handleMessage(topic, new String(message.getPayload()));
                    logger.info("====================================");
    }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消息发送完成回调
                }
            });
            logger.info("MQTT回调设置完成");

            // 连接到MQTT broker
            logger.info("准备连接到MQTT broker...");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            
            if (!username.isEmpty() && !password.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                logger.info("使用用户名密码连接");
            } else {
                logger.info("使用无密码连接");
            }

            logger.info("正在连接到MQTT broker: {}", broker);
            mqttClient.connect(options);
            logger.info("MQTT客户端连接成功: {}", broker);

            // 订阅数据主题
            logger.info("准备订阅主题: {}", dataTopic);
            mqttClient.subscribe(dataTopic, 0);
            logger.info("订阅主题成功: {}", dataTopic);

            executorService = Executors.newSingleThreadExecutor();
            logger.info("MQTT初始化完成");

        } catch (MqttException e) {
            logger.error("MQTT初始化失败: {}", e.getMessage());
            logger.error("MQTT错误详细信息:", e);
            System.out.println("MQTT初始化失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("MQTT初始化过程中发生未知错误: {}", e.getMessage());
            logger.error("错误详细信息:", e);
            System.out.println("MQTT初始化过程中发生未知错误: " + e.getMessage());
        }
    }

    private void reconnect() {
        executorService.submit(() -> {
            try {
                Thread.sleep(5000);
                if (!mqttClient.isConnected()) {
                    logger.info("尝试重新连接MQTT broker...");
                    mqttClient.connect();
                    mqttClient.subscribe(dataTopic, 0);
                    logger.info("MQTT重新连接成功");
                }
            } catch (Exception e) {
                logger.error("MQTT重连失败: {}", e.getMessage());
            }
        });
    }

    private void handleMessage(String topic, String payload) {
        try {
            // 解析JSON格式的传感器数据
            if (topic.equals(dataTopic)) {
                logger.info("处理传感器数据: {}", payload);
                
                // 解析JSON数据
                SensorDataDTO sensorDataDTO = objectMapper.readValue(payload, SensorDataDTO.class);
                
                // 打印解析后的数据
                logger.info("解析后的传感器数据: temp={}, humi={}, pm25={}, smoke={}", 
                        sensorDataDTO.getTemp(), sensorDataDTO.getHumi(), 
                        sensorDataDTO.getPm25(), sensorDataDTO.getSmoke());
                
                // 通过WebSocket推送给前端
                if (webSocketController != null) {
                    webSocketController.broadcastSensorData(sensorDataDTO);
                    logger.info("通过WebSocket推送传感器数据成功");
                } else {
                    logger.warn("WebSocketController未初始化，无法推送数据");
                }
            }
        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", e.getMessage());
        }
    }
    
    // 临时类，用于解析JSON数据
    private static class SensorDataDTO {
        private int temp;
        private int humi;
        private int pm25;
        private double smoke;
        private String location;
        private String sensorType;
        
        public int getTemp() {
            return temp;
        }
        
        public void setTemp(int temp) {
            this.temp = temp;
        }
        
        public int getHumi() {
            return humi;
        }
        
        public void setHumi(int humi) {
            this.humi = humi;
        }
        
        public int getPm25() {
            return pm25;
        }
        
        public void setPm25(int pm25) {
            this.pm25 = pm25;
        }
        
        public double getSmoke() {
            return smoke;
        }

        public void setSmoke(double smoke) {
            this.smoke = smoke;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getSensorType() {
            return sensorType;
        }
        
        public void setSensorType(String sensorType) {
            this.sensorType = sensorType;
        }
    }

    public void publish(String topic, String message) {
        try {
            System.out.println("MqttService.publish() 方法被调用");
            System.out.println("MQTT客户端连接状态: " + mqttClient.isConnected());
            System.out.println("准备发布MQTT消息: topic=" + topic + ", payload=" + message);

            if (mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(0);
                mqttClient.publish(topic, mqttMessage);
                System.out.println("MQTT消息发布成功: topic=" + topic + ", payload=" + message);
                logger.info("发布MQTT消息 - 主题: {}, 内容: {}", topic, message);
            } else {
                System.out.println("MQTT客户端未连接，无法发布消息");
                logger.warn("MQTT客户端未连接，无法发布消息");
            }
        } catch (MqttException e) {
            System.out.println("发布MQTT消息失败: " + e.getMessage());
            e.printStackTrace();
            logger.error("发布MQTT消息失败: {}", e.getMessage());
        }
    }

    /**
     * 检查MQTT客户端是否已连接
     * @return true表示已连接，false表示未连接
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * 发布MQTT消息（支持指定QoS）
     * @param topic 主题
     * @param message 消息内容
     * @param qos 服务质量等级（0=最多一次，1=至少一次，2=只有一次）
     */
    public void publishWithQos(String topic, String message, int qos) {
        try {
            System.out.println("MqttService.publishWithQos() 方法被调用");
            System.out.println("MQTT客户端连接状态: " + mqttClient.isConnected());
            System.out.println("准备发布MQTT消息: topic=" + topic + ", payload=" + message + ", qos=" + qos);

            if (mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(qos);
                mqttClient.publish(topic, mqttMessage);
                System.out.println("MQTT消息发布成功: topic=" + topic + ", payload=" + message + ", qos=" + qos);
                logger.info("发布MQTT消息 - 主题: {}, 内容: {}, QoS: {}", topic, message, qos);
            } else {
                System.out.println("MQTT客户端未连接，无法发布消息");
                logger.warn("MQTT客户端未连接，无法发布消息");
            }
        } catch (MqttException e) {
            System.out.println("发布MQTT消息失败: " + e.getMessage());
            e.printStackTrace();
            logger.error("发布MQTT消息失败: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT客户端已断开连接");
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (MqttException e) {
            logger.error("关闭MQTT客户端失败: {}", e.getMessage());
        }
    }
}