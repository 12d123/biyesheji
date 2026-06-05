package com.hqh.warehouse_backend.repository;

import com.hqh.warehouse_backend.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    // 根据位置查找最新的10条数据
    List<SensorData> findTop10ByLocationOrderByCreatedTimeDesc(String location);
    
    // 获取所有位置的最新10条数据（不按位置过滤）
    List<SensorData> findTop10ByOrderByCreatedTimeDesc();
    
    // 查找指定时间范围内的数据
    List<SensorData> findByCreatedTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // 查找最新的单条数据
    SensorData findTopByOrderByCreatedTimeDesc();
    
    // 根据位置查找所有数据
    List<SensorData> findByLocation(String location);
    
    // 统计总记录数
    @Query("SELECT COUNT(s) FROM SensorData s")
    Long countTotalRecords();
    
    // 修复：统计今日记录数（使用时间范围而不是DATE函数）
    @Query("SELECT COUNT(s) FROM SensorData s WHERE s.createdTime >= :startOfDay AND s.createdTime <= :endOfDay")
    Long countTodayRecords(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // 批量删除方法
    @Modifying
    @Query(value = "DELETE FROM sensor_data WHERE id IN :ids", nativeQuery = true)
    int deleteByIds(@Param("ids") List<Long> ids);
    
    @Modifying
    @Query(value = "TRUNCATE TABLE sensor_data", nativeQuery = true)
    void truncateTable();
}