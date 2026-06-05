package com.hqh.warehouse_backend.config;

import com.hqh.warehouse_backend.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private SensorDataService sensorDataService;
    
    @Override
    public void run(String... args) throws Exception {
        // 如果数据库为空，初始化一些测试数据
        if (sensorDataService.getDataCount() == 0) {
            System.out.println("🎯 初始化测试数据...");
            for (int i = 0; i < 5; i++) {
                sensorDataService.generateAndSaveMockData();
                // 稍微延迟，让时间戳不同
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("✅ 测试数据初始化完成，共生成5条记录");
        } else {
            System.out.println("✅ 数据库中已有 " + sensorDataService.getDataCount() + " 条记录");
        }
    }
}