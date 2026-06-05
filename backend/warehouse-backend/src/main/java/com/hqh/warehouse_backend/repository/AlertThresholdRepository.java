package com.hqh.warehouse_backend.repository;

import com.hqh.warehouse_backend.entity.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long> {

    /**
     * 根据位置查找阈值配置
     */
    Optional<AlertThreshold> findByLocation(String location);

    /**
     * 查找所有启用的阈值配置
     */
    List<AlertThreshold> findByEnabledTrue();

    /**
     * 检查位置是否存在
     */
    boolean existsByLocation(String location);

    /**
     * 更新启用状态
     */
    @Modifying
    @Query("UPDATE AlertThreshold t SET t.enabled = :enabled, t.updatedTime = :updateTime WHERE t.id = :id")
    int updateEnabledStatus(@Param("id") Long id, @Param("enabled") Boolean enabled, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据位置列表查找阈值配置
     */
    @Query("SELECT t FROM AlertThreshold t WHERE t.location IN :locations")
    List<AlertThreshold> findByLocations(@Param("locations") List<String> locations);

    /**
     * 查找所有位置列表
     */
    @Query("SELECT t.location FROM AlertThreshold t")
    List<String> findAllLocations();

    /**
     * 更新阈值范围
     */
    @Modifying
    @Query("UPDATE AlertThreshold t SET t.minTemperature = :minTemp, t.maxTemperature = :maxTemp, " +
            "t.minHumidity = :minHum, t.maxHumidity = :maxHum, t.description = :desc, " +
            "t.updatedTime = :updateTime WHERE t.id = :id")
    int updateThresholdRange(@Param("id") Long id,
                             @Param("minTemp") Double minTemp,
                             @Param("maxTemp") Double maxTemp,
                             @Param("minHum") Double minHum,
                             @Param("maxHum") Double maxHum,
                             @Param("desc") String desc,
                             @Param("updateTime") LocalDateTime updateTime);

    /**
     * 统计启用的阈值配置数量
     */
    long countByEnabledTrue();
}