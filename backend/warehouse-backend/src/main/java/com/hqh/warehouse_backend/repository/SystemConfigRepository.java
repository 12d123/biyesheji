package com.hqh.warehouse_backend.repository;

import com.hqh.warehouse_backend.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 根据配置类型查找配置列表
     */
    List<SystemConfig> findByConfigType(String configType);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 更新配置值
     */
    @Modifying
    @Query("UPDATE SystemConfig c SET c.configValue = :value, c.updatedTime = :updateTime WHERE c.configKey = :key")
    int updateConfigValue(@Param("key") String key, @Param("value") String value, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据多个配置键查找配置列表
     */
    @Query("SELECT c FROM SystemConfig c WHERE c.configKey IN :keys")
    List<SystemConfig> findByConfigKeys(@Param("keys") List<String> keys);

    /**
     * 删除指定配置键的配置
     */
    @Modifying
    @Query("DELETE FROM SystemConfig c WHERE c.configKey = :key")
    int deleteByConfigKey(@Param("key") String key);

    /**
     * 获取所有配置键列表
     */
    @Query("SELECT c.configKey FROM SystemConfig c")
    List<String> findAllConfigKeys();

    /**
     * 根据配置类型和键前缀查找配置
     */
    List<SystemConfig> findByConfigTypeAndConfigKeyStartingWith(String configType, String keyPrefix);
}