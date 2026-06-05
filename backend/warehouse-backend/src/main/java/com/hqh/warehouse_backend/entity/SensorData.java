package com.hqh.warehouse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "temperature", nullable = false)
    private Double temperature;
    
    @Column(name = "humidity", nullable = false)
    private Double humidity;
    
    @Column(name = "location", nullable = false, length = 100)
    private String location;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "sensor_type", length = 50)
    private String sensorType = "DHT11";
    
    // ========== 新增字段 ==========
    @Column(name = "pm25")
    private Double pm25;

    @Column(name = "smoke")
    private Double smoke;
    
    // ========== 构造函数 ==========
    
    public SensorData() {
        this.createdTime = LocalDateTime.now();
        this.pm25 = 0.0;
        this.smoke = 0.0;
    }
    
    public SensorData(Double temperature, Double humidity, String location) {
        this();
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
    }
    
    public SensorData(Double temperature, Double humidity, String location, String sensorType) {
        this();
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
        this.sensorType = sensorType;
    }
    
    // 全参构造函数（可选）
    public SensorData(Double temperature, Double humidity, String location, String sensorType, Double pm25, Double smoke) {
        this(temperature, humidity, location, sensorType);
        this.pm25 = pm25;
        this.smoke = smoke;
    }
    
    // ========== Getter 和 Setter ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }
    
    public Double getPm25() { return pm25; }
    public void setPm25(Double pm25) { this.pm25 = pm25; }
    
    public Double getSmoke() { return smoke; }
    public void setSmoke(Double smoke) { this.smoke = smoke; }
    
    @Override
    public String toString() {
        return String.format("SensorData{id=%d, temperature=%.1f°C, humidity=%.1f%%, pm25=%.1f, smoke=%.1f ppm, location='%s', time=%s}",
                id, temperature, humidity, pm25, smoke, location, createdTime);
    }
}