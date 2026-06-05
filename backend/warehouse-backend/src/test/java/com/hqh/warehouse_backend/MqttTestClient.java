package com.hqh.warehouse_backend;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttTestClient {

    public static void main(String[] args) {
        String broker = "tcp://192.168.1.100:1883";
        String clientId = "test-client";
        String topic = "warehouse/data";
        
        try {
            // 创建MQTT客户端
            MqttClient mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            
            // 连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            
            // 连接到broker
            System.out.println("连接到MQTT broker: " + broker);
            mqttClient.connect(options);
            System.out.println("连接成功");
            
            // 发送测试消息
            String messageContent = "{\"temp\":25,\"humi\":60,\"pm25\":30,\"smoke\":0}";
            MqttMessage message = new MqttMessage(messageContent.getBytes());
            message.setQos(0);
            
            System.out.println("发布消息到主题: " + topic);
            System.out.println("消息内容: " + messageContent);
            mqttClient.publish(topic, message);
            System.out.println("消息发布成功");
            
            // 断开连接
            mqttClient.disconnect();
            System.out.println("已断开连接");
            
        } catch (MqttException e) {
            System.out.println("MQTT错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
