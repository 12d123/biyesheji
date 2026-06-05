package com.hqh.warehouse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_threshold")
public class AlertThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "min_temperature")
    private Double minTemperature;

    @Column(name = "max_temperature")
    private Double maxTemperature;

    @Column(name = "min_humidity")
    private Double minHumidity;

    @Column(name = "max_humidity")
    private Double maxHumidity;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    // ========== 构造函数 ==========

    public AlertThreshold() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public AlertThreshold(String location, Double minTemperature, Double maxTemperature,
                          Double minHumidity, Double maxHumidity, String description) {
        this();
        this.location = location;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.description = description;
    }

    // ========== Getter 和 Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(Double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public Double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(Double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public Double getMinHumidity() {
        return minHumidity;
    }

    public void setMinHumidity(Double minHumidity) {
        this.minHumidity = minHumidity;
    }

    public Double getMaxHumidity() {
        return maxHumidity;
    }

    public void setMaxHumidity(Double maxHumidity) {
        this.maxHumidity = maxHumidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return String.format("AlertThreshold{id=%d, location='%s', temp=%s-%s℃, humidity=%s-%s%%, enabled=%s}",
                id, location, minTemperature, maxTemperature, minHumidity, maxHumidity, enabled);
    }

    /**
     * 检查数据是否在阈值范围内
     */
    public boolean isTemperatureNormal(Double temperature) {
        return temperature >= minTemperature && temperature <= maxTemperature;
    }

    /**
     * 检查数据是否在阈值范围内
     */
    public boolean isHumidityNormal(Double humidity) {
        return humidity >= minHumidity && humidity <= maxHumidity;
    }

    /**
     * 更新阈值并自动设置更新时间
     */
    public void updateThreshold(Double minTemp, Double maxTemp, Double minHum, Double maxHum, String desc) {
        this.minTemperature = minTemp;
        this.maxTemperature = maxTemp;
        this.minHumidity = minHum;
        this.maxHumidity = maxHum;
        this.description = desc;
        this.updatedTime = LocalDateTime.now();
    }
}