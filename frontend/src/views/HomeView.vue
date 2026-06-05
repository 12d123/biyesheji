<template>
  <div class="dashboard-container">
    <!-- 顶部标题和用户信息区域 -->
    <div class="header-section">
      <div class="header-content">
        <div class="header-title">
          <h1>智能仓储环境监控系统</h1>
          <p>实时监控仓库环境数据，确保存储安全</p>
        </div>
         <!-- 添加导航菜单 -->
        <div class="nav-menu">
        <el-menu
            :default-active="activeNav"
            mode="horizontal"
            @select="handleNavSelect"
            class="nav-menu-container"
    >
      <el-menu-item index="dashboard">监控看板</el-menu-item>
      <el-menu-item index="alerts">告警中心</el-menu-item>
      <el-menu-item index="history">历史数据</el-menu-item>
      <el-menu-item index="reports">统计报表</el-menu-item>
      <el-menu-item index="settings">系统设置</el-menu-item>
        </el-menu>
        </div>
        <div class="user-info">
          <el-dropdown @command="handleUserCommand">
            <span class="user-dropdown">
              <el-avatar size="small" :icon="UserFilled" />
              <span class="user-name">{{ userInfo.fullName || userInfo.username }}</span>
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <div class="system-status">
        <el-tag v-if="connectionStatus === 'connected'" type="success">
          ✅ 系统运行正常
        </el-tag>
        <el-tag v-else-if="connectionStatus === 'connecting'" type="warning">
          🔄 连接后端服务中...
        </el-tag>
        <el-tag v-else type="danger">
          ❌ 后端服务异常
        </el-tag>
        <span class="data-count">数据记录: {{ stats.total }} 条</span>
        <!-- 告警状态 -->
        <div class="alert-status">
          <el-badge :value="activeAlerts.length" :max="99" class="alert-badge">
            <el-tag 
              :type="getAlertStatusType" 
              class="alert-tag"
              @click="showAlertPanel = true"
            >
              {{ getAlertStatusIcon }} 告警: {{ activeAlerts.length }} 个
            </el-tag>
          </el-badge>
        </div>
      </div>
    </div>

    <!-- 告警面板 -->
    <el-drawer
      v-model="showAlertPanel"
      title="环境告警系统"
      direction="rtl"
      size="400px"
    >
      <div class="alert-panel">
        <!-- 告警统计 -->
        <div class="alert-stats">
          <el-row :gutter="10">
            <el-col :span="8">
              <div class="stat-item danger">
                <div class="stat-count">{{ criticalAlertsCount }}</div>
                <div class="stat-label">紧急告警</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item warning">
                <div class="stat-count">{{ warningAlertsCount }}</div>
                <div class="stat-label">警告</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item normal">
                <div class="stat-count">{{ normalCount }}</div>
                <div class="stat-label">正常</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 告警列表 -->
        <div class="alert-list">
          <div v-if="activeAlerts.length === 0" class="no-alerts">
            <el-empty description="暂无告警" />
          </div>
          
          <div v-else>
            <div class="alert-actions">
              <el-button size="small" @click="acknowledgeAllAlerts">
                🆗 全部确认
              </el-button>
              <el-button size="small" @click="clearAcknowledgedAlerts">
                🗑️ 清理已处理
              </el-button>
            </div>
            
            <div 
              v-for="alert in sortedAlerts" 
              :key="alert.id"
              class="alert-item"
              :class="`alert-${alert.level}`"
            >
              <div class="alert-header">
                <span class="alert-icon">{{ getAlertLevelStyle(alert.level).icon }}</span>
                <span class="alert-location">{{ alert.location }}</span>
                <el-tag 
                  size="small" 
                  :type="getAlertLevelStyle(alert.level).type"
                >
                  {{ alert.level === 'danger' ? '紧急' : '警告' }}
                </el-tag>
              </div>
              
              <div class="alert-content">
                 <div class="alert-data">
                 <span>温度: {{ alert?.temperature || 0 }}℃</span>
                 <span>湿度: {{ alert?.humidity || 0 }}%</span>
                 </div>
                 <div 
                  v-for="(message, index) in alert.messages" 
                  :key="index"
                  class="alert-message"
                >
                  {{ message }}
                </div>
                <div class="alert-range">
                  正常范围: {{ alert.range.temperature.min }}~{{ alert.range.temperature.max }}℃, 
                  {{ alert.range.humidity.min }}~{{ alert.range.humidity.max }}%
                </div>
              </div>
              
              <div class="alert-footer">
                <span class="alert-time">{{ formatAlertTime(alert.timestamp) }}</span>
                <el-button 
                  v-if="!alert.acknowledged"
                  size="small" 
                  type="primary"
                  @click="acknowledgeAlert(alert.id)"
                >
                  确认
                </el-button>
                <el-tag v-else size="small" type="info">已处理</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
    <!-- 在 status-section 之前添加用户信息卡片 -->
     <el-card class="user-info-section" v-if="authStore.user">
     <template #header>
      <div class="card-header">
      <span>👤 用户信息</span>
      <el-tag :type="getRoleTagType(authStore.user.role)">
        {{ getRoleDisplayName(authStore.user.role) }}
      </el-tag>
    </div>
  </template>
  
  <div class="user-details">
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="user-detail-item">
          <label>用户名:</label>
          <span>{{ authStore.user.username }}</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="user-detail-item">
          <label>姓名:</label>
          <span>{{ authStore.user.fullName || '未设置' }}</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="user-detail-item">
          <label>角色:</label>
          <span>{{ getRoleDisplayName(authStore.user.role) }}</span>
        </div>
      </el-col>
    </el-row>
    
    <!-- 权限说明 -->
    <el-divider content-position="left">权限说明</el-divider>
    <div class="permission-info">
      <p>{{ getRoleDescription(authStore.user.role) }}</p>
      <div class="permission-badges">
        <el-tag 
          v-for="permission in getUserPermissions(authStore.user.role)" 
          :key="permission"
          size="small"
          type="info"
          class="permission-tag"
        >
          {{ permission }}
        </el-tag>
      </div>
    </div>
  </div>
</el-card>

    <!-- 连接状态显示 -->
    <el-card class="status-section">
      <template #header>
        <div class="card-header">
          <span>系统状态</span>
          <el-button 
            type="primary" 
            size="small" 
            @click="refreshData" 
            :loading="loading"
            :disabled="connectionStatus === 'connecting'"
          >
            🔄 刷新状态
          </el-button>
        </div>
      </template>
      <div class="status-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="status-item">
              <h4>后端服务</h4>
              <el-tag :type="getStatusType(connectionStatus)">
                {{ getStatusText(connectionStatus) }}
              </el-tag>
              <p class="status-desc">API服务连接状态</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="status-item">
              <h4>数据库</h4>
              <el-tag type="success">✅ 正常</el-tag>
              <p class="status-desc">MySQL数据连接</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="status-item">
              <h4>数据更新</h4>
              <el-tag type="info">{{ lastUpdateTime || '暂无数据' }}</el-tag>
              <p class="status-desc">最后更新时间</p>
            </div>
          </el-col>
        </el-row>
        
        <div class="connection-message" v-if="connectionMessage">
          <p><strong>状态信息:</strong> {{ connectionMessage }}</p>
        </div>
      </div>
    </el-card>

    <!-- 实时传感器数据显示 -->
    <el-card class="sensor-display-section">
      <template #header>
        <div class="card-header">
          <span>实时环境数据</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="refreshData" 
              :loading="loading"
              :disabled="connectionStatus !== 'connected'"
            >
              🔄 刷新数据
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="generateMockData" 
              :loading="mockLoading"
              :disabled="connectionStatus !== 'connected'"
            >
              🎲 生成模拟数据
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="sensor-display">
  <el-row :gutter="20">
    <!-- 温度卡片 -->
    <el-col :span="6">
      <div class="sensor-card temperature" :class="{ 'sensor-alert': currentAlert && currentAlert.level === 'danger' }">
        <div class="sensor-icon">🌡️</div>
        <h3>当前温度</h3>
        <div class="sensor-value">
          {{ currentData.temperature || '--' }}℃
          <span v-if="currentAlert && currentAlert.level === 'danger'" class="alert-indicator">🚨</span>
        </div>
        <div class="sensor-location">{{ currentData.location || '--' }}</div>
        <div class="sensor-time">{{ currentData.time || '--' }}</div>
      </div>
    </el-col>
    <!-- 湿度卡片 -->
    <el-col :span="6">
      <div class="sensor-card humidity" :class="{ 'sensor-alert': currentAlert && currentAlert.level === 'warning' }">
        <div class="sensor-icon">💧</div>
        <h3>当前湿度</h3>
        <div class="sensor-value">
          {{ currentData.humidity || '--' }}%
          <span v-if="currentAlert && currentAlert.level === 'warning'" class="alert-indicator">🔔</span>
        </div>
        <div class="sensor-location">{{ currentData.location || '--' }}</div>
        <div class="sensor-time">{{ currentData.time || '--' }}</div>
      </div>
    </el-col>
    <!-- PM2.5 卡片 -->
    <el-col :span="6">
      <div class="sensor-card pm25" :class="{ 'sensor-alert': pm25Alert }">
        <div class="sensor-icon">🌫️</div>
        <h3>PM2.5</h3>
        <div class="sensor-value">
          {{ currentData.pm25 || '--' }} μg/m³
          <span v-if="pm25Alert" class="alert-indicator">⚠️</span>
        </div>
        <div class="sensor-location">{{ currentData.location || '--' }}</div>
        <div class="sensor-time">{{ currentData.time || '--' }}</div>
        <!-- 联动状态提示 -->
        <div class="linkage-status" v-if="currentData.pm25 >= 100 && fanMode === 'auto'">
          <el-tag type="success" size="small" v-if="currentData.pm25 < 150">⚡ 风扇已自动开启</el-tag>
          <el-tag type="warning" size="small" v-else-if="currentData.pm25 < 200">⚠️ 风扇已开启（警告）</el-tag>
          <el-tag type="danger" size="small" v-else>🚨 风扇+报警已触发</el-tag>
        </div>
      </div>
    </el-col>
    <!-- 烟雾卡片 -->
    <el-col :span="6">
      <div class="sensor-card smoke" :class="{ 'sensor-alert': smokeAlert }">
        <div class="sensor-icon">🔥</div>
        <h3>烟雾浓度</h3>
        <div class="sensor-value">
          {{ currentData.smoke || '--' }} ppm
          <span v-if="smokeAlert" class="alert-indicator">🚨</span>
        </div>
        <div class="sensor-location">{{ currentData.location || '--' }}</div>
        <div class="sensor-time">{{ currentData.time || '--' }}</div>
        <!-- 联动状态提示 -->
        <div class="linkage-status" v-if="currentData.smoke >= 600 && fanMode === 'auto'">
          <el-tag type="warning" size="small" v-if="currentData.smoke < 800">⚠️ 风扇已开启（警告）</el-tag>
          <el-tag type="danger" size="small" v-else>🚨 风扇+报警已触发</el-tag>
        </div>
      </div>
    </el-col>
  </el-row>
  
  <!-- 第二行：告警统计 + 风扇控制 -->
  <el-row :gutter="20" class="sensor-row" style="margin-top: 20px;">
    <el-col :span="12">
      <div class="sensor-card stats">
        <div class="sensor-icon">📊</div>
        <h3>告警统计</h3>
        <div class="sensor-value">{{ activeAlerts.length }} 个</div>
        <div class="sensor-location">当前告警</div>
        <div class="sensor-detail">
          <span>紧急: {{ criticalAlertsCount }} 个</span>
          <span>警告: {{ warningAlertsCount }} 个</span>
        </div>
      </div>
    </el-col>
    <el-col :span="12">
      <div class="sensor-card fan-control">
        <div class="sensor-icon">🌀</div>
        <h3>风扇控制</h3>
        <!-- 模式切换 -->
        <div class="fan-mode">
          <el-radio-group v-model="fanMode" size="small" @change="changeFanMode">
            <el-radio-button label="auto">自动模式</el-radio-button>
            <el-radio-button label="manual">手动模式</el-radio-button>
          </el-radio-group>
        </div>
        <!-- 手动模式下显示风扇开关 -->
        <div class="fan-status" v-if="fanMode === 'manual'">
          <el-switch
            v-model="fanState"
            active-text="开启"
            inactive-text="关闭"
            :before-change="toggleFan"
            :loading="fanLoading"
          />
          <div class="fan-tip" v-if="fanState">
            <el-tag type="danger" size="small" effect="dark">🚨 手动控制（报警联动）</el-tag>
          </div>
        </div>
        <!-- 自动模式下显示状态说明 -->
        <div class="sensor-location" v-else>
          {{ getAutoModeStatus() }}
        </div>
      </div>
    </el-col>
  </el-row>
</div>
    </el-card>
    <!-- 在 sensor-display-section 之后添加权限功能卡片 -->
    <el-card class="permission-features-section" v-if="authStore.user">
  <template #header>
    <div class="card-header">
      <span>🚀 功能菜单</span>
      <el-tag type="primary">基于角色权限</el-tag>
    </div>
  </template>
  
  <div class="features-content">
    <el-row :gutter="20">
      <!-- 数据查看 - 所有角色可见 -->
      <el-col :span="6">
        <div class="feature-card" @click="navigateToDataView">
          <div class="feature-icon">📊</div>
          <h4>数据查看</h4>
          <p>查看传感器数据和统计信息</p>
          <el-tag size="small" type="success">所有角色</el-tag>
        </div>
      </el-col>
      
      <!-- 数据管理 - OPERATOR 和 ADMIN 可见 -->
      <el-col :span="6" v-if="authStore.hasPermission('OPERATOR')">
        <div class="feature-card" @click="navigateToDataManagement">
          <div class="feature-icon">🛠️</div>
          <h4>数据管理</h4>
          <p>管理传感器数据和设置</p>
          <el-tag size="small" type="warning">操作员+</el-tag>
        </div>
      </el-col>
      
      <!-- 用户管理 - 仅 ADMIN 可见 -->
      <el-col :span="6" v-if="authStore.hasPermission('ADMIN')">
        <div class="feature-card" @click="navigateToUserManagement">
          <div class="feature-icon">👥</div>
          <h4>用户管理</h4>
          <p>管理系统用户和权限</p>
          <el-tag size="small" type="danger">仅管理员</el-tag>
        </div>
      </el-col>
      
      <!-- 系统设置 - 仅 ADMIN 可见 -->
      <el-col :span="6" v-if="authStore.hasPermission('ADMIN')">
        <div class="feature-card" @click="navigateToSystemSettings">
          <div class="feature-icon">⚙️</div>
          <h4>系统设置</h4>
          <p>配置系统参数和告警规则</p>
          <el-tag size="small" type="danger">仅管理员</el-tag>
        </div>
      </el-col>
    </el-row>
  </div>
</el-card>
  <!-- 图表区域 -->
  <el-row :gutter="20" class="chart-row">
    <el-col :span="12">
      <el-card class="chart-section compact">
        <template #header>
          <div class="card-header">
            <span>🌡️ 温度变化趋势</span>
            <el-button 
              size="small" 
              @click="refreshData" 
              :loading="loading"
              :disabled="connectionStatus !== 'connected'"
            >
              刷新
            </el-button>
          </div>
        </template>
        
        <div class="chart-content compact">
          <TemperatureChart :chartData="temperatureHistory" chartHeight="320px" />
        </div>
      </el-card>
    </el-col>
    
    <el-col :span="12">
      <el-card class="chart-section compact">
        <template #header>
          <div class="card-header">
            <span>📊 仓库环境对比</span>
            <el-tag type="info" size="small">多仓库</el-tag>
          </div>
        </template>
        
        <div class="chart-content compact">
          <!-- 调试信息 -->
          <div v-if="connectionStatus === 'connected'" style="font-size: 12px; color: #666; margin-bottom: 10px; padding: 0 10px;">
            数据统计: {{ warehouseStats.length }} 个仓库
            <span v-for="(item, index) in warehouseStats" :key="item.name" style="margin-left: 10px;">
              {{ item.name }}({{ item.temperature }}℃/{{ item.humidity }}%)<span v-if="index < warehouseStats.length - 1">,</span>
            </span>
          </div>
          
          <WarehouseComparisonChart :chartData="warehouseStats" :loading="loading" chartHeight="320px" />
        </div>
         </el-card>
         </el-col>
          </el-row>
          <el-row :gutter="20" class="chart-row" style="margin-top: 20px;">
          <el-col :span="24">
          <el-card class="chart-section compact">
          <template #header><div class="card-header"><span>🌫️ PM2.5 变化趋势</span></div></template>
          <div class="chart-content compact">
          <PM25TrendChart :chartData="pm25History" chartHeight="320px" />
           </div>
           </el-card>
           </el-col>
          </el-row>


    <!-- 仓库环境标准说明 -->
    <el-card class="standards-section">
      <template #header>
        <div class="card-header">
          <span>仓库环境标准</span>
          <el-tag type="info">监控标准</el-tag>
        </div>
      </template>
      
      <div class="standards-content">
        <el-table :data="warehouseStandards" stripe style="width: 100%">
          <el-table-column prop="name" label="仓库区域" width="120" />
          <el-table-column label="温度范围" width="150">
            <template #default="scope">
              {{ scope.row.temperature.min }}℃ ~ {{ scope.row.temperature.max }}℃
            </template>
          </el-table-column>
          <el-table-column label="湿度范围" width="150">
            <template #default="scope">
              {{ scope.row.humidity.min }}% ~ {{ scope.row.humidity.max }}%
            </template>
          </el-table-column>
          <el-table-column prop="description" label="环境要求" />
          <el-table-column label="当前状态" width="100">
            <template #default="scope">
              <el-tag 
                :type="getLocationStatus(scope.row.name).type"
                size="small"
              >
                {{ getLocationStatus(scope.row.name).text }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 手动添加传感器数据 -->
    <el-card class="manual-add-section">
      <template #header>
        <div class="card-header">
          <span>手动添加传感器数据</span>
          <el-tag type="primary">真实数据操作</el-tag>
        </div>
      </template>
      
      <div class="manual-add-content">
        <el-alert
          v-if="connectionStatus !== 'connected'"
          title="后端服务未连接"
          type="warning"
          description="请先确保后端服务正常运行"
          show-icon
          :closable="false"
        />
        
        <el-form 
          :model="addForm" 
          :rules="formRules" 
          ref="addFormRef" 
          label-width="100px" 
          class="add-form"
          :disabled="connectionStatus !== 'connected'"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="温度" prop="temperature">
                <el-input-number 
                  v-model="addForm.temperature" 
                  :min="-50" 
                  :max="50" 
                  :step="0.1"
                  :precision="1"
                  controls-position="right"
                  placeholder="请输入温度"
                  style="width: 100%"
                >
                  <template #append>℃</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="湿度" prop="humidity">
                <el-input-number 
                  v-model="addForm.humidity" 
                  :min="0" 
                  :max="100" 
                  :step="0.1"
                  :precision="1"
                  controls-position="right"
                  placeholder="请输入湿度"
                  style="width: 100%"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="位置" prop="location">
                <el-select v-model="addForm.location" placeholder="请选择位置" style="width: 100%">
                  <el-option label="A区仓库" value="A区仓库" />
                  <el-option label="B区仓库" value="B区仓库" />
                  <el-option label="C区仓库" value="C区仓库" />
                  <el-option label="冷冻库" value="冷冻库" />
                  <el-option label="常温库" value="常温库" />
                  <el-option label="贵重物品区" value="贵重物品区" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="传感器类型" prop="sensorType">
                <el-select v-model="addForm.sensorType" placeholder="请选择传感器类型" style="width: 100%">
                  <el-option label="DHT11" value="DHT11" />
                  <el-option label="DHT22" value="DHT22" />
                  <el-option label="SHT30" value="SHT30" />
                  <el-option label="BME280" value="BME280" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="PM2.5" prop="pm25">
                <el-input-number 
                  v-model="addForm.pm25" 
                  :min="0" 
                  :max="500" 
                  :step="0.1"
                  :precision="1"
                  controls-position="right"
                  placeholder="请输入PM2.5"
                  style="width: 100%"
                >
                  <template #append>μg/m³</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="烟雾" prop="smoke">
                <el-input-number
                  v-model="addForm.smoke"
                  :min="0"
                  :max="2000"
                  :step="1"
                  :precision="0"
                  controls-position="right"
                  placeholder="请输入烟雾浓度"
                  style="width: 100%"
                >
                  <template #append>ppm</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleAddData" 
              :loading="addingData"
              :disabled="connectionStatus !== 'connected'"
            >
              📝 添加传感器数据
            </el-button>
            <el-button @click="resetForm">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 数据操作按钮 -->
    <el-card class="actions-section">
      <template #header>
        <div class="card-header">
          <span>数据操作</span>
          <el-tag type="info">批量操作</el-tag>
        </div>
      </template>
      
      <div class="actions-content">
        <el-button-group>
          <el-button 
            type="success" 
            @click="generateMockData" 
            :loading="mockLoading"
            :disabled="connectionStatus !== 'connected'"
          >
            🎲 生成模拟数据
          </el-button>
          <el-button 
            type="warning" 
            @click="generateBatchMockData" 
            :loading="batchMockLoading"
            :disabled="connectionStatus !== 'connected'"
          >
            📦 批量生成(5条)
          </el-button>
          <el-button 
            type="info" 
            @click="showAlertData"
            :loading="loading"
            :disabled="connectionStatus !== 'connected'"
          >
            ⚠️ 查看告警数据
          </el-button>
          <el-button 
            type="danger" 
            @click="clearAllData" 
            :loading="clearingData"
            :disabled="connectionStatus !== 'connected' || historyData.length === 0"
          >
            🗑️ 清空所有数据
          </el-button>
        </el-button-group>
        
        <div class="auto-refresh">
          <el-checkbox v-model="autoRefresh" @change="toggleAutoRefresh">
            自动刷新 (30秒)
          </el-checkbox>
        </div>
      </div>
    </el-card>

    <!-- 历史数据表格 -->
    <el-card class="history-section">
      <template #header>
        <div class="card-header">
          <span>历史数据记录</span>
          <div class="table-actions">
            <el-button 
              size="small" 
              @click="refreshData" 
              :loading="loading"
              :disabled="connectionStatus !== 'connected'"
            >
              刷新
            </el-button>
            <el-button 
              size="small" 
              @click="showAlertData"
              :loading="loading"
              :disabled="connectionStatus !== 'connected'"
            >
              只看告警
            </el-button>
            <span style="color: #909399; font-size: 14px; margin-left: 10px;">
              共 {{ historyData.length }} 条记录
            </span>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="historyData" 
        stripe 
        style="width: 100%" 
        v-loading="loading"
        :default-sort="{ prop: 'time', order: 'descending' }"
        empty-text="暂无数据"
      >
      <el-table-column prop="pm25" label="PM2.5" width="100" sortable>
  <template #default="scope">
    <span :class="getPm25Class(scope.row.pm25, scope.row.location)">
      {{ scope.row.pm25 }} μg/m³
    </span>
     </template>
      </el-table-column>
      <el-table-column prop="smoke" label="烟雾" width="100">
          <template #default="scope">
          <span :class="getSmokeClass(scope.row.smoke)">
         {{ scope.row.smoke || 0 }} ppm
          </span>
          </template>
         </el-table-column>
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="temperature" label="温度" width="100" sortable>
          <template #default="scope">
            <span :class="getTemperatureClass(scope.row.temperature, scope.row.location)">
              {{ scope.row.temperature }}℃
              <span v-if="getDataAlert(scope.row)" class="cell-alert">
                {{ getAlertLevelStyle(getDataAlert(scope.row).level).icon }}
              </span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="humidity" label="湿度" width="100" sortable>
          <template #default="scope">
            <span :class="getHumidityClass(scope.row.humidity, scope.row.location)">
              {{ scope.row.humidity }}%
              <span v-if="getDataAlert(scope.row)" class="cell-alert">
                {{ getAlertLevelStyle(getDataAlert(scope.row).level).icon }}
              </span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="120" />
        <el-table-column prop="sensorType" label="传感器类型" width="120" />
        <el-table-column prop="time" label="记录时间" width="180" sortable />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="getDataStatus(scope.row.temperature, scope.row.humidity, scope.row.location).type"
              size="small"
            >
              {{ getDataStatus(scope.row.temperature, scope.row.humidity, scope.row.location).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警" width="80">
          <template #default="scope">
            <span v-if="getDataAlert(scope.row)" class="alert-badge-cell">
              {{ getAlertLevelStyle(getDataAlert(scope.row).level).icon }}
            </span>
            <span v-else>✅</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 错误提示 -->
    <el-dialog
      v-model="showErrorDialog"
      title="系统错误"
      width="500px"
      :before-close="handleCloseErrorDialog"
    >
      <div class="error-dialog-content">
        <el-alert
          type="error"
          :title="errorTitle"
          :description="errorMessage"
          show-icon
          :closable="false"
        />
        <div class="error-actions" style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="showErrorDialog = false">
            确定
          </el-button>
          <el-button @click="refreshData">
            重试连接
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  SwitchButton, 
  ArrowDown,
  UserFilled 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

// 导入图表组件
import TemperatureChart from '@/components/TemperatureChart.vue'
import WarehouseComparisonChart from '@/components/WarehouseComparisonChart.vue'
import PM25TrendChart from '@/components/PM25TrendChart.vue'

// 导入工具函数
import { checkDataAbnormal } from '@/utils/alerts'
//导入api
import sensorApi from '@/api/sensorApi'
// 导入WebSocket服务
import webSocketService from '@/api/websocket'

// 路由和状态管理
const router = useRouter()
const authStore = useAuthStore()
const activeNav = ref('dashboard')
// ========== 响应式数据 ==========

// 状态管理
const connectionStatus = ref('connecting') // connecting, connected, error
const connectionMessage = ref('正在连接后端服务...')
const historyData = ref([])
const currentSensorData = ref({})
const systemStats = ref({})
const lastUpdateTime = ref('')

// 告警系统状态
const showAlertPanel = ref(false)
const alerts = ref([])

// 加载状态
const loading = ref(false)
const addingData = ref(false)
const mockLoading = ref(false)
const batchMockLoading = ref(false)
const clearingData = ref(false)

// 自动刷新
const autoRefresh = ref(true)
let refreshTimer = null

// 记录上次显示数据通知的时间（每30秒最多一次）
let lastDataNotificationTime = 0
const NOTIFICATION_INTERVAL = 30 * 1000 // 30秒（毫秒）

// 错误处理
const showErrorDialog = ref(false)
const errorTitle = ref('')
const errorMessage = ref('')


const handleNavSelect = (index) => {
  activeNav.value = index
  switch (index) {
    case 'dashboard':
      // 已经是首页，不需要操作
      break
    case 'alerts':
      showAlertPanel.value = true
      break
    case 'history':
      // 跳转到数据查看页面
      router.push('/data-view')
      break
    case 'reports':
      // 跳转到数据管理页面
      router.push('/data-management')
      break
    case 'settings':
      if (authStore.hasPermission('ADMIN')) {
        navigateToSystemSettings()  // 这里调用实际的导航方法
      } else {
        ElMessage.warning('无权限访问系统设置')
      }
      break
  }
}

// 滚动到历史数据区域
const scrollToHistory = () => {
  const historySection = document.querySelector('.history-section')
  if (historySection) {
    historySection.scrollIntoView({ behavior: 'smooth' })
  }
}

// ========== 角色和权限相关方法 ==========
const getRoleDisplayName = (role) => {
  const roleNames = {
    'VIEWER': '查看员',
    'OPERATOR': '操作员', 
    'ADMIN': '管理员',
    'SUPER_ADMIN': '超级管理员'
  }
  return roleNames[role] || role
}

const getRoleTagType = (role) => {
  const types = {
    'VIEWER': 'info',
    'OPERATOR': 'success',
    'ADMIN': 'warning',
    'SUPER_ADMIN': 'danger'
  }
  return types[role] || 'info'
}

const getRoleDescription = (role) => {
  const descriptions = {
    'VIEWER': '查看员 - 拥有数据查看和监控权限，可以查看实时数据和历史记录',
    'OPERATOR': '操作员 - 拥有数据管理和操作权限，可以录入和管理传感器数据',
    'ADMIN': '管理员 - 拥有用户管理和系统配置权限，可以管理所有系统功能',
    'SUPER_ADMIN': '超级管理员 - 拥有系统所有权限，包括高级配置和用户管理'
  }
  return descriptions[role] || '未知角色'
}

const getUserPermissions = (role) => {
  const permissions = {
    'VIEWER': ['数据查看', '实时监控', '历史查询'],
    'OPERATOR': ['数据查看', '实时监控', '历史查询', '数据录入', '数据管理'],
    'ADMIN': ['数据查看', '实时监控', '历史查询', '数据录入', '数据管理', '用户管理', '系统配置'],
    'SUPER_ADMIN': ['所有权限']
  }
  return permissions[role] || []
}

// ========== 导航方法 ==========
const navigateToDataView = () => {
  router.push('/data-view')
}

const navigateToDataManagement = () => {
  if (authStore.hasPermission('OPERATOR')) {
    router.push('/data-management')
  } else {
    ElMessage.warning('无权限访问数据管理')
  }
}

const navigateToUserManagement = () => {
  if (authStore.hasPermission('ADMIN')) {
    // 跳转到用户管理路由
    router.push('/user-management')
  } else {
    ElMessage.warning('无权限访问用户管理')
  }
}

const navigateToSystemSettings = () => {
  if (authStore.hasPermission('ADMIN')) {
    // 使用动态导入避免循环依赖
    import('@/api/systemApi.js').then(() => {
      router.push('/system-settings')
    }).catch(error => {
      console.error('加载系统设置模块失败:', error)
      ElMessage.error('系统设置功能暂不可用')
    })
  } else {
    ElMessage.warning('无权限访问系统设置')
  }
}
// 表单相关
const addFormRef = ref()
const addForm = ref({
  temperature: 25.0,
  humidity: 60.0,
  pm25: 20.0,
  smoke: 0,
  location: 'A区仓库',
  sensorType: 'DHT11'
})

const formRules = {
  temperature: [
    { required: true, message: '请输入温度', trigger: 'blur' },
    { type: 'number', min: -50, max: 50, message: '温度范围 -50-50℃', trigger: 'blur' }
  ],
  humidity: [
    { required: true, message: '请输入湿度', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '湿度范围 0-100%', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请选择位置', trigger: 'change' }
  ],
  sensorType: [
    { required: true, message: '请选择传感器类型', trigger: 'change' }
  ]
}
  const fanState = ref(false);
  const fanLoading = ref(false);
  const fanMode = ref('auto');  // 风扇控制模式：'auto' 或 'manual'
  const fanToggling = ref(false);  // 防止快速连续点击的标志位

  // 切换风扇模式
  const changeFanMode = async (mode) => {
    console.log('🔄 切换风扇模式:', mode);
    try {
      // 通过WebSocket发送模式切换指令到后端，后端再通过MQTT发送给设备
      const modeMessage = mode === 'manual' ? { mode: 'manual' } : { mode: 'auto' };
      // 如果是手动模式，同时发送当前风扇状态
      if (mode === 'manual' && fanState.value) {
        modeMessage.on = true;
      }
      await sensorApi.setFanMode(modeMessage);
      ElMessage.success(mode === 'manual' ? '已切换到手动模式' : '已切换到自动模式');
    } catch (error) {
      console.error('❌ 切换风扇模式失败:', error);
      ElMessage.error('切换风扇模式失败: ' + error.message);
    }
  };

  // 获取自动模式状态描述
  const getAutoModeStatus = () => {
    const alerts = activeAlerts.value;
    if (alerts.length > 0) {
      const pm25Alert = alerts.find(a => a.type === 'pm25');
      const smokeAlert = alerts.find(a => a.type === 'smoke');
      if (pm25Alert && pm25Alert.severity === 'warning') {
        return '⚠️ 风扇已自动开启（PM2.5预警）';
      }
      if (smokeAlert && smokeAlert.severity === 'warning') {
        return '⚠️ 风扇已自动开启（烟雾预警）';
      }
      return '🚨 风扇已自动开启（传感器超限）';
    }
    return '✅ 系统自动控制中';
  };

  // 切换风扇状态 - 使用before-change确保API调用成功后才切换UI
  const toggleFan = async () => {
    console.log('🔍 [BEFORE-CHANGE] toggleFan被调用');
    console.log('🔍 [BEFORE-CHANGE] 当前fanToggling状态:', fanToggling.value);
    console.log('🔍 [BEFORE-CHANGE] 当前fanState状态:', fanState.value);

    // 防止快速连续点击：如果正在处理中，直接返回false（禁止切换）
    if (fanToggling.value) {
      console.log('⚠️ 风扇切换中，忽略重复点击');
      ElMessage.warning('操作过于频繁，请稍后再试');
      return false;  // 返回false阻止UI切换
    }

    // before-change传递的是当前状态，所以需要反转得到目标状态
    const newState = !fanState.value;
    console.log('🌀 toggleFan 被调用，当前状态:', fanState.value, '目标状态:', newState);
    
    // 设置切换中标志 - 在API调用完成前保持锁定
    fanToggling.value = true;
    fanLoading.value = true;
    console.log('🔍 [BEFORE-CHANGE] 已设置标志位: fanLoading=', fanLoading.value, ', fanToggling=', fanToggling.value);

    try {
      console.log('📤 通过REST API发送风扇控制指令...');

      // 使用REST API发送风扇控制指令
      const response = await sensorApi.setFanState(newState);
      console.log('📡 REST API响应:', response);

      if (response && response.success) {
        console.log('✅ 风扇控制指令已发送:', newState ? '开启' : '关闭');
        ElMessage.success(newState ? '风扇已开启（手动控制）' : '风扇已关闭');
        console.log('🔍 [BEFORE-CHANGE] 准备返回true（允许UI切换）');
        
        // 立即清除标志位，减少锁定时间
        // 注意：在before-change中返回true后，el-switch会立即更新UI
        // 所以我们可以立即清除标志位，允许下一次操作
        fanToggling.value = false;
        fanLoading.value = false;
        console.log('🔍 [BEFORE-CHANGE] 立即清除标志位完成');
        
        return true;  // 返回true允许UI切换
      } else {
        throw new Error(response?.message || '风扇控制失败');
      }
    } catch (error) {
      console.error('❌ 切换风扇状态失败:', error);
      ElMessage.error('切换风扇状态失败: ' + error.message);
      console.log('🔍 [BEFORE-CHANGE] 准备返回false（阻止UI切换）');
      
      // 错误时立即清除标志位
      fanToggling.value = false;
      fanLoading.value = false;
      return false;  // 返回false阻止UI切换
    }
  };

// PM2.5 图表数据
  const pm25History = computed(() => {
  return historyData.value.slice(0, 10).map(item => ({
    time: formatTime(item.time),
    pm25: item.pm25 || 0
   }));
   });

// 告警计算属性（PM2.5 和烟雾）
const pm25Alert = computed(() => {
  if (!currentData.value.location || currentData.value.pm25 === undefined) return false;
  const range = warehouseRanges[currentData.value.location];
  return range?.pm25 ? (currentData.value.pm25 < range.pm25.min || currentData.value.pm25 > range.pm25.max) : false;
});

const smokeAlert = computed(() => {
  if (!currentData.value.location || currentData.value.smoke === undefined) return false;
  // 烟雾浓度阈值：800 ppm报警
  const SMOKE_THRESHOLD = 800
  return currentData.value.smoke >= SMOKE_THRESHOLD;
});
// 模拟仓库范围数据
const warehouseRanges = {
  'A区仓库': {
    name: 'A区仓库',
    temperature: { min: 18, max: 25 },
    humidity: { min: 40, max: 65 },
    pm25: { min: 0, max: 35 },
    description: '普通物品存储区'
  },
  'B区仓库': {
    name: 'B区仓库',
    temperature: { min: 20, max: 28 },
    humidity: { min: 45, max: 70 },
    pm25: { min: 0, max: 35 },
    description: '电子产品存储区'
  },
  'C区仓库': {
    name: 'C区仓库',
    temperature: { min: 15, max: 22 },
    humidity: { min: 35, max: 60 },
    pm25: { min: 0, max: 35 },
    description: '精密仪器存储区'
  },
  '冷冻库': {
    name: '冷冻库',
    temperature: { min: -10, max: 5 },
    humidity: { min: 30, max: 50 },
    pm25: { min: 0, max: 35 },
    description: '冷冻食品存储区'
  },
  '常温库': {
    name: '常温库',
    temperature: { min: 18, max: 25 },
    humidity: { min: 45, max: 65 },
    pm25: { min: 0, max: 35 },
    description: '常温物品存储区'
  },
  '贵重物品区': {
    name: '贵重物品区',
    temperature: { min: 20, max: 24 },
    humidity: { min: 40, max: 55 },
    pm25: { min: 0, max: 35 },
    description: '贵重物品存储区'
  }
}

// ========== 计算属性 ==========

// 用户信息
const userInfo = computed(() => authStore.user || {})

// 当前数据（最新一条）
const currentData = computed(() => {
  if (historyData.value.length === 0) {
    return {
      temperature: 0,
      humidity: 0,
      pm25: 0,
      smoke: 0,
      location: '暂无数据',
      time: ''
    }
  }
  const latest = historyData.value[0]
  return {
    temperature: latest.temperature,
    humidity: latest.humidity,
    pm25: latest.pm25 || 0,
    smoke: latest.smoke || 0,
    location: latest.location,
    time: formatTime(latest.time)
  }
})

// 当前数据告警状态
const currentAlert = computed(() => {
  if (!currentData.value.location || currentData.value.location === '暂无数据') {
    return null
  }
  return checkDataAbnormal(currentData.value)
})

// 活跃告警（未确认的）
const activeAlerts = computed(() => {
  return alerts.value.filter(alert => !alert.acknowledged)
})

// 排序后的告警（紧急优先）
const sortedAlerts = computed(() => {
  return [...activeAlerts.value].sort((a, b) => {
    if (a.level === 'danger' && b.level !== 'danger') return -1
    if (a.level !== 'danger' && b.level === 'danger') return 1
    return new Date(b.timestamp) - new Date(a.timestamp)
  })
})

// 告警统计
const criticalAlertsCount = computed(() => {
  return activeAlerts.value.filter(alert => alert.level === 'danger').length
})

const warningAlertsCount = computed(() => {
  return activeAlerts.value.filter(alert => alert.level === 'warning').length
})

const normalCount = computed(() => {
  return Object.keys(warehouseRanges).length - activeAlerts.value.length
})

// 告警状态类型
const getAlertStatusType = computed(() => {
  if (criticalAlertsCount.value > 0) return 'danger'
  if (warningAlertsCount.value > 0) return 'warning'
  return 'success'
})

const getAlertStatusIcon = computed(() => {
  if (criticalAlertsCount.value > 0) return '🚨'
  if (warningAlertsCount.value > 0) return '🔔'
  return '✅'
})

// 仓库环境标准表格数据
const warehouseStandards = computed(() => {
  return Object.values(warehouseRanges)
})

// 统计信息
const stats = computed(() => {
  const today = new Date().toDateString()
  const todayCount = historyData.value.filter(item => {
    if (!item.time) return false
    const itemDate = new Date(item.time).toDateString()
    return itemDate === today
  }).length
  
  return {
    total: historyData.value.length,
    today: todayCount
  }
})

// 温度图表数据 - 修复数据结构
const temperatureHistory = computed(() => {
  console.log('📈 生成温度图表数据，历史数据长度:', historyData.value.length)
  
  // 取最近10条数据用于温度趋势图
  const recentData = historyData.value.slice(0, 10)
  const chartData = recentData.map(item => ({
    time: formatTime(item.time),
    temperature: item.temperature
  }))
  
  console.log('📈 温度图表数据:', chartData)
  return chartData
})

// 仓库统计数据 - 修复数据结构
const warehouseStats = computed(() => {
  console.log('🏭 计算warehouseStats，历史数据:', historyData.value)
  
  if (!historyData.value || historyData.value.length === 0) {
    console.log('🏭 无历史数据，返回空数组')
    return [];
  }

  const warehouseMap = {};
  
  historyData.value.forEach(item => {
    if (item && item.location) {
      if (!warehouseMap[item.location]) {
        warehouseMap[item.location] = {
          name: item.location,
          temperature: 0,
          humidity: 0,
          count: 0
        };
      }
      warehouseMap[item.location].temperature += parseFloat(item.temperature) || 0;
      warehouseMap[item.location].humidity += parseFloat(item.humidity) || 0;
      warehouseMap[item.location].count++;
    }
  });
  
  // 计算平均值并返回数组
  const result = Object.values(warehouseMap).map(warehouse => ({
    name: warehouse.name,
    temperature: warehouse.count > 0 ? (warehouse.temperature / warehouse.count).toFixed(1) : '0',
    humidity: warehouse.count > 0 ? (warehouse.humidity / warehouse.count).toFixed(1) : '0'
  }));
  
  console.log('🏭 计算后的warehouseStats:', result);
  return result;
});
// ========== 方法 ==========

// 用户操作处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      handleUserProfile()
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理用户信息
const handleUserProfile = () => {
  ElMessage.info('个人信息功能开发中...')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用退出登录
    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    // 用户取消退出
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  try {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (e) {
    return timestamp
  }
}

// 格式化告警时间
const formatAlertTime = (timestamp) => {
  try {
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
  } catch (e) {
    return timestamp
  }
}

// 更新最后更新时间
const updateLastUpdateTime = () => {
  lastUpdateTime.value = new Date().toLocaleTimeString()
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'connected': return 'success'
    case 'connecting': return 'warning'
    case 'error': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'connected': return '✅ 正常'
    case 'connecting': return '🔄 连接中'
    case 'error': return '❌ 异常'
    default: return '未知状态'
  }
}

// 检查连接状态
const checkConnection = async () => {
  try {
    connectionStatus.value = 'connecting'
    connectionMessage.value = '正在检查后端服务连接...'
    
    const result = await sensorApi.healthCheck()
    if (result.status === 'connected') {
      connectionStatus.value = 'connected'
      connectionMessage.value = '后端服务连接成功'
      return true
    } else {
      connectionStatus.value = 'error'
      connectionMessage.value = result.message
      return false
    }
  } catch (error) {
    connectionStatus.value = 'error'
    connectionMessage.value = '后端服务连接失败: ' + error.message
    showError('连接失败', '无法连接到后端服务，请确保后端服务正在运行。')
    return false
  }
}


const getAlertLevelStyle = (level) => {
  const styles = {
    danger: { icon: '🚨', type: 'danger' },
    warning: { icon: '🔔', type: 'warning' },
    normal: { icon: '✅', type: 'success' }
  }
  return styles[level] || styles.normal
}

// 确认单个告警
const acknowledgeAlert = (alertId) => {
  const alert = alerts.value.find(a => a.id === alertId)
  if (alert) {
    alert.acknowledged = true
    ElMessage.success('告警已确认')
  }
}

// 确认所有告警
const acknowledgeAllAlerts = () => {
  alerts.value.forEach(alert => {
    alert.acknowledged = true
  })
  ElMessage.success('所有告警已确认')
}

// 清理已处理的告警
const clearAcknowledgedAlerts = () => {
  alerts.value = alerts.value.filter(alert => !alert.acknowledged)
  ElMessage.success('已清理处理的告警')
}

// 获取位置状态
const getLocationStatus = (location) => {
  const locationAlerts = activeAlerts.value.filter(alert => alert.location === location)
  const critical = locationAlerts.some(alert => alert.level === 'danger')
  const warning = locationAlerts.some(alert => alert.level === 'warning')
  
  if (critical) return { type: 'danger', text: '异常' }
  if (warning) return { type: 'warning', text: '警告' }
  return { type: 'success', text: '正常' }
}

// 获取数据告警状态
const getDataAlert = (data) => {
  return checkDataAbnormal(data)
}

// 获取温度样式类（基于区域范围）
const getTemperatureClass = (temperature, location) => {
  if (!warehouseRanges[location]) return 'temp-unknown'
  
  const range = warehouseRanges[location]
  if (temperature < range.temperature.min) return 'temp-low'
  if (temperature > range.temperature.max) return 'temp-high'
  return 'temp-normal'
}

// 获取湿度样式类（基于区域范围）
const getHumidityClass = (humidity, location) => {
  if (!warehouseRanges[location]) return 'humidity-unknown'
  
  const range = warehouseRanges[location]
  if (humidity < range.humidity.min) return 'humidity-low'
  if (humidity > range.humidity.max) return 'humidity-high'
  return 'humidity-normal'
}

// 获取PM2.5样式类（基于区域范围）
const getPm25Class = (pm25, location) => {
  if (!warehouseRanges[location]) return 'pm25-unknown'

  const range = warehouseRanges[location]
  if (pm25 < range.pm25.min) return 'pm25-low'
  if (pm25 > range.pm25.max) return 'pm25-high'
  return 'pm25-normal'
}

// 获取烟雾样式类（基于区域范围）
const getSmokeClass = (smoke) => {
  // 烟雾浓度阈值：800 ppm报警
  const SMOKE_THRESHOLD = 800
  if (smoke === undefined || smoke === null) return 'smoke-unknown'
  if (smoke >= SMOKE_THRESHOLD) return 'smoke-high'
  return 'smoke-normal'
}

// 获取数据状态（基于区域范围）
const getDataStatus = (temperature, humidity, location) => {
  const checkResult = checkDataAbnormal({ temperature, humidity, location })
  if (checkResult.isAbnormal) {
    return { type: 'danger', text: '异常' }
  }
  return { type: 'success', text: '正常' }
}

// 加载所有数据
const loadAllData = async () => {
  if (connectionStatus.value !== 'connected') {
    const connected = await checkConnection()
    if (!connected) return
  }
  
  try {
    loading.value = true
    console.log('🔄 开始加载传感器数据...')
    
    // 调用真实API获取数据
    const rawData = await sensorApi.getAllData()
    console.log('📊 API返回的原始数据:', rawData)
    console.log('📊 原始数据类型:', typeof rawData)
    console.log('📊 原始数据长度:', Array.isArray(rawData) ? rawData.length : '不是数组')
    
    if (Array.isArray(rawData) && rawData.length > 0) {
      console.log('📊 第一条数据样例:', rawData[0])
    }
    
    // 转换数据格式
    historyData.value = formatBackendToFrontend(rawData)
    console.log('🔄 转换后的数据:', historyData.value)
    console.log('🔄 转换后数据长度:', historyData.value.length)
    
    if (historyData.value.length > 0) {
      console.log('📊 转换后第一条数据:', historyData.value[0])
    }
    
    updateLastUpdateTime()
    
    // 检查告警
    checkDataAlerts()
    
    if (historyData.value.length > 0) {
      ElMessage.success(`数据加载成功，共 ${historyData.value.length} 条记录`)
    } else {
      console.warn('⚠️ 数据转换后为空数组')
      ElMessage.info('暂无数据记录，请生成模拟数据或手动添加数据')
    }
    
  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败: ' + error.message)
    showError('数据加载失败', error.message)
  } finally {
    loading.value = false
  }
}
// 添加数据格式转换函数
const formatBackendToFrontend = (backendData) => {
  console.log('🔄 开始转换数据格式，原始数据:', backendData)
  
  if (!backendData) {
    console.log('⚠️ 后端数据为空')
    return []
  }
  
  let dataToProcess = backendData
  
  // 如果后端数据包含在data字段中，提取出来
  if (backendData.data && Array.isArray(backendData.data)) {
    console.log('📦 从data字段提取数组数据')
    dataToProcess = backendData.data
  }
  
  if (Array.isArray(dataToProcess)) {
    const converted = dataToProcess
      .filter(item => item && (item.temperature !== undefined || item.humidity !== undefined)) // 过滤无效数据
      .map(item => {
        // 处理不同的字段名映射，添加默认值
        const convertedItem = {
          id: item.id || item.sensorId || Date.now() + Math.random(),
          temperature: parseFloat(item.temperature) || 0,
          humidity: parseFloat(item.humidity) || 0,
    pm25: parseFloat(item.pm25) || 0,
    smoke: parseFloat(item.smoke) || 0,
    location: item.warehouseLocation || item.location || item.warehouseArea || '未知位置',
          sensorType: item.sensorType || item.type || 'DHT11',
          time: item.createdTime || item.timestamp || item.time || item.createdAt || new Date().toISOString()
        }
        
        console.log('🔄 转换单个数据项:', { 原始: item, 转换后: convertedItem })
        return convertedItem
      })
    
    console.log('✅ 数据转换完成，转换后条数:', converted.length)
    return converted
  }
  
  console.log('❌ 数据不是数组格式，返回空数组')
  return []
}

// 检查数据告警
const checkDataAlerts = () => {
  alerts.value = []
  historyData.value.forEach(item => {
    // 确保 item 有必要的属性
    if (!item || typeof item.temperature === 'undefined' || typeof item.humidity === 'undefined') {
      console.warn('无效的数据项:', item)
      return
    }
    
    const alert = checkDataAbnormal(item)
    if (alert.isAbnormal) {
      // 确保 range 存在
      const range = warehouseRanges[item.location] || {
        temperature: { min: 0, max: 100 },
        humidity: { min: 0, max: 100 }
      }
      
      alerts.value.push({
        id: Date.now() + Math.random(),
        location: item.location || '未知位置',
        temperature: item.temperature || 0,
        humidity: item.humidity || 0,
        level: alert.level || 'warning',
        messages: alert.messages || ['数据异常'],
        range: range,
        timestamp: new Date(),
        acknowledged: false
      })
    }
  })
}

// 刷新数据
const refreshData = async () => {
  await loadAllData()
}

// 显示错误对话框
const showError = (title, message) => {
  errorTitle.value = title
  errorMessage.value = message
  showErrorDialog.value = true
}

// 关闭错误对话框
const handleCloseErrorDialog = () => {
  showErrorDialog.value = false
}

// 手动添加数据
const handleAddData = async () => {
  try {
    addingData.value = true
    
    // 表单验证
    if (!addFormRef.value) return
    const valid = await addFormRef.value.validate()
    if (!valid) return
    
    // 准备后端数据格式
    const backendData = {
      warehouseLocation: addForm.value.location,
      temperature: addForm.value.temperature,
      humidity: addForm.value.humidity,
      pm25: addForm.value.pm25,
      smoke: addForm.value.smoke,
      sensorType: addForm.value.sensorType
    }
    
    // 调用真实API添加数据
    await sensorApi.addSensorData(backendData)
    
    ElMessage.success('传感器数据添加成功')
    
    // 重置表单
    resetForm()
    
    // 重新加载数据
    await loadAllData()
    
  } catch (error) {
    console.error('添加数据失败:', error)
    ElMessage.error('添加数据失败: ' + error.message)
    showError('添加数据失败', error.message)
  } finally {
    addingData.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
  addForm.value = {
    temperature: 25.0,
    humidity: 60.0,
    pm25: 20.0,
    smoke: 0,
    location: 'A区仓库',
    sensorType: 'DHT11'
  }
}

// 生成模拟数据
const generateMockData = async () => {
  try {
    mockLoading.value = true
    
    // 调用真实API生成模拟数据
    await sensorApi.generateMockData()
    
    ElMessage.success('模拟数据生成成功')
    
    // 重新加载数据
    await loadAllData()
    
  } catch (error) {
    console.error('生成模拟数据失败:', error)
    ElMessage.error('生成模拟数据失败: ' + error.message)
    showError('生成模拟数据失败', error.message)
  } finally {
    mockLoading.value = false
  }
}

// 批量生成模拟数据
const generateBatchMockData = async () => {
  try {
    batchMockLoading.value = true
    
    // 调用真实API批量生成模拟数据
    await sensorApi.generateBatchMockData(5)
    
    ElMessage.success('批量模拟数据生成成功')
    
    // 重新加载数据
    await loadAllData()
    
  } catch (error) {
    console.error('批量生成模拟数据失败:', error)
    ElMessage.error('批量生成模拟数据失败: ' + error.message)
    showError('批量生成模拟数据失败', error.message)
  } finally {
    batchMockLoading.value = false
  }
}

// 显示告警数据
const showAlertData = async () => {
  try {
    loading.value = true
    
    // 调用真实API获取异常数据
    const response = await sensorApi.getAbnormalData()
    
    // 转换数据格式
    const abnormalData = formatBackendToFrontend(response)
    
    if (abnormalData.length > 0) {
      // 更新告警数据
      alerts.value = abnormalData.map(item => {
        if (!item || typeof item.temperature === 'undefined' || typeof item.humidity === 'undefined') {
          console.warn('无效的异常数据项:', item)
          return null
        }
        
        const alert = checkDataAbnormal(item)
        // 确保 range 存在
        const range = warehouseRanges[item.location] || {
          temperature: { min: 0, max: 100 },
          humidity: { min: 0, max: 100 }
        }
        
        return {
          id: item.id || Date.now() + Math.random(),
          location: item.location || '未知位置',
          temperature: item.temperature || 0,
          humidity: item.humidity || 0,
          level: alert.level || 'warning',
          messages: alert.messages || ['数据异常'],
          range: range,
          timestamp: new Date(item.time || new Date()),
          acknowledged: false
        }
      }).filter(alert => alert !== null) // 过滤掉无效的告警
      
      showAlertPanel.value = true
      ElMessage.info(`发现 ${abnormalData.length} 条异常数据`)
    } else {
      ElMessage.info('没有发现异常数据')
    }
  } catch (error) {
    console.error('获取告警数据失败:', error)
    ElMessage.error('获取告警数据失败: ' + error.message)
    showError('获取告警数据失败', error.message)
  } finally {
    loading.value = false
  }
}
// 清空所有数据
const clearAllData = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除所有传感器数据，是否继续？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    clearingData.value = true
    
    // 调用真实API清空数据
    await sensorApi.clearAllData()
    
    // 清空本地数据
    historyData.value = []
    alerts.value = []
    
    ElMessage.success('所有数据已清空')
    
    // 更新最后更新时间
    updateLastUpdateTime()
    
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消清空操作')
    } else {
      console.error('清空数据失败:', error)
      ElMessage.error('清空数据失败: ' + error.message)
      showError('清空数据失败', error.message)
    }
  } finally {
    clearingData.value = false
  }
}

// 切换自动刷新
const toggleAutoRefresh = (enabled) => {
  if (enabled) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  refreshTimer = setInterval(async () => {
    if (connectionStatus.value === 'connected' && !loading.value) {
      await loadAllData()
    }
  }, 30000) // 30秒
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 处理WebSocket传感器数据
const handleSensorData = (data) => {
  console.log('🌐 收到WebSocket传感器数据:', data)
  
  // 转换数据格式
  const formattedData = {
    id: data.id || Date.now() + Math.random(),
    temperature: parseFloat(data.temperature || data.temp) || 0,
    humidity: parseFloat(data.humidity || data.humi) || 0,
    pm25: parseFloat(data.pm25) || 0,
    smoke: parseFloat(data.smoke) || 0,
    location: data.location || data.warehouseLocation || 'A区仓库',
    sensorType: data.sensorType || 'DHT11',
    time: data.createdTime || data.timestamp || new Date().toISOString()
  }
  
  console.log('🔄 格式化后的传感器数据:', formattedData)
  
  // 添加到历史数据开头
  historyData.value.unshift(formattedData)
  
  // 保持数据长度在合理范围
  if (historyData.value.length > 100) {
    historyData.value = historyData.value.slice(0, 100)
  }
  
  // 更新最后更新时间
  updateLastUpdateTime()

  // 检查告警
  checkDataAlerts()

  // 显示收到新数据的通知（每3分钟最多一次）
  const currentTime = Date.now()
  if (currentTime - lastDataNotificationTime >= NOTIFICATION_INTERVAL) {
    ElMessage.success('收到新的传感器数据')
    lastDataNotificationTime = currentTime
  }
}

// 初始化
onMounted(async () => {
  console.log('HomeView 开始挂载');
  
  // 初始检查连接状态
  await checkConnection()
  
  // 如果连接成功，加载数据
  if (connectionStatus.value === 'connected') {
    // 添加更长的延迟确保所有DOM完全渲染
    setTimeout(async () => {
      console.log('开始加载数据');
      await loadAllData()
      
      // 数据加载后验证
      console.log('🔍 数据加载完成验证:')
      console.log('  历史数据长度:', historyData.value.length)
      console.log('  当前数据:', currentData.value)
      console.log('  温度图表数据:', temperatureHistory.value.length)
      console.log('  仓库统计数据:', warehouseStats.value.length)
      
      if (historyData.value.length === 0) {
        console.warn('⚠️ 历史数据为空，建议生成模拟数据')
        ElMessage.warning('检测到无数据，请点击"生成模拟数据"按钮')
      }
      
      // 连接WebSocket
      try {
        console.log('🌐 开始连接WebSocket...')
        await webSocketService.connect()
        console.log('🌐 WebSocket连接成功')
        
        // 注册传感器数据回调
        webSocketService.on('onSensorData', handleSensorData)
      } catch (error) {
        console.error('🌐 WebSocket连接失败:', error)
        ElMessage.warning('WebSocket连接失败，将使用轮询方式获取数据')
      }
    }, 800)
  }
  
  // 启动自动刷新
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

// 组件卸载时断开WebSocket连接
onUnmounted(() => {
  console.log('HomeView 组件卸载');
  
  // 停止自动刷新
  stopAutoRefresh()
  
  // 断开WebSocket连接
  webSocketService.disconnect()
  console.log('🌐 WebSocket已断开连接')
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.header-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-title h1 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-title p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 12px;
}

.data-count {
  color: #909399;
  font-size: 14px;
}

/* 告警状态样式 */
.alert-status {
  margin-left: 15px;
}

.alert-badge :deep(.el-badge__content) {
  top: -8px;
  right: -8px;
}

.alert-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.alert-tag:hover {
  transform: scale(1.05);
}

.status-section {
  margin-bottom: 20px;
}

.status-content {
  padding: 16px 0;
}

.status-item {
  text-align: center;
  padding: 16px;
}

.status-item h4 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.status-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.connection-message {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.connection-message p {
  margin: 0;
  font-size: 14px;
}

.sensor-display-section {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.sensor-display {
  margin: 8px 0;
}

.sensor-card {
  text-align: center;
  padding: 28px 20px;
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.sensor-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
}

.sensor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.sensor-card.temperature {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
}

.sensor-card.humidity {
  background: linear-gradient(135deg, #4ecdc4, #6de0d8);
}

.sensor-card.stats {
  background: linear-gradient(135deg, #45b7d1, #67c8e3);
}

/* 传感器卡片告警样式 */
.sensor-alert {
  animation: pulse-alert 2s infinite;
}

@keyframes pulse-alert {
  0% { box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3); }
  50% { box-shadow: 0 4px 20px rgba(245, 108, 108, 0.6); }
  100% { box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3); }
}

.alert-indicator {
  margin-left: 5px;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.3; }
}

.sensor-alert-info {
  margin-top: 8px;
  padding: 5px;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 4px;
}

.alert-message-small {
  font-size: 12px;
  color: #f56c6c;
  text-align: center;
}

.sensor-icon {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.9;
}

.sensor-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  opacity: 0.9;
  font-weight: 500;
}

.sensor-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sensor-location {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 6px;
}

.sensor-time {
  font-size: 12px;
  opacity: 0.8;
}

.sensor-detail {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 8px;
}

/* 图表区域样式 - 紧凑布局 */
.chart-row {
  margin-bottom: 20px;
}

.chart-section.compact {
  margin-bottom: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-content.compact {
  padding: 0;
  height: 350px; /* 固定高度 */
  min-height: 350px;
  flex: 1;
  position: relative;
}

/* 标准说明区域 */
.standards-section {
  margin-bottom: 20px;
}

.standards-content {
  padding: 10px 0;
}

.manual-add-section {
  margin-bottom: 20px;
}

.manual-add-content {
  padding: 16px 0;
}

.add-form {
  max-width: 600px;
}

.actions-section {
  margin-bottom: 20px;
}

.actions-content {
  padding: 16px 0;
  text-align: center;
}

.auto-refresh {
  margin-top: 16px;
}

.history-section {
  margin-bottom: 20px;
}

.table-actions {
  display: flex;
  align-items: center;
}

/* 告警面板样式 */
.alert-panel {
  padding: 0 10px;
}

.alert-stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px 10px;
  border-radius: 8px;
  color: white;
}

.stat-item.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stat-item.warning {
  background: linear-gradient(135deg, #e6a23c, #e8b464);
}

.stat-item.normal {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-count {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.alert-actions {
  margin-bottom: 15px;
  text-align: center;
}

.alert-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.alert-item {
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  border-left: 4px solid;
}

.alert-item.alert-danger {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.alert-item.alert-warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.alert-icon {
  font-size: 16px;
}

.alert-location {
  font-weight: bold;
  flex: 1;
}

.alert-content {
  margin-bottom: 8px;
}

.alert-data {
  display: flex;
  gap: 15px;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.alert-message {
  color: #f56c6c;
  font-size: 14px;
  margin-bottom: 3px;
}

.alert-range {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.alert-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-time {
  font-size: 12px;
  color: #909399;
}

.no-alerts {
  text-align: center;
  padding: 40px 0;
}

/* 温度湿度样式 */
.temp-low {
  color: #409eff;
  font-weight: 600;
}

.temp-normal {
  color: #67c23a;
  font-weight: 600;
}

.temp-high {
  color: #f56c6c;
  font-weight: 600;
}

.temp-unknown {
  color: #909399;
  font-weight: 600;
}

/* 湿度样式 */
.humidity-low {
  color: #409eff;
  font-weight: 600;
}

.humidity-normal {
  color: #67c23a;
  font-weight: 600;
}

.humidity-high {
  color: #f56c6c;
  font-weight: 600;
}

.humidity-unknown {
  color: #909399;
  font-weight: 600;
}

/* 烟雾样式 */
.smoke-normal {
  color: #67c23a;
  font-weight: 600;
}

.smoke-high {
  color: #f56c6c;
  font-weight: 600;
}

.smoke-unknown {
  color: #909399;
  font-weight: 600;
}

/* 表格中的告警样式 */
.cell-alert {
  margin-left: 5px;
  animation: blink 1s infinite;
}

.alert-badge-cell {
  animation: blink 1s infinite;
}

.error-dialog-content {
  padding: 8px 0;
}

:deep(.el-input-group__append) {
  background-color: #f5f7fa;
  color: #909399;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .header-section {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-title h1 {
    font-size: 20px;
  }
  
  .system-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .sensor-card {
    padding: 20px 16px;
  }
  
  .sensor-value {
    font-size: 28px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .table-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  /* 图表响应式 */
  .chart-row {
    flex-direction: column;
  }
  
  .chart-content.compact {
    height: 280px;
  }
  
  /* 告警面板响应式 */
  .alert-stats .el-col {
    margin-bottom: 10px;
  }
  
  .alert-data {
    flex-direction: column;
    gap: 5px;
  }
  
  .alert-footer {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
.nav-menu {
  flex: 1;
  margin: 0 40px;
  display: flex;
  justify-content: center;
}

.nav-menu-container {
  border: none;
  background: transparent;
}

.nav-menu-container :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 0 8px;
  border-radius: 6px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

.nav-menu-container :deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
  color: #409eff;
}

.nav-menu-container :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  border-bottom: 2px solid #409eff;
}

/* 用户信息卡片样式 */
.user-info-section {
  margin-bottom: 20px;
}

.user-details {
  padding: 10px 0;
}

.user-detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.user-detail-item label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.user-detail-item span {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.permission-info {
  margin-top: 10px;
}

.permission-info p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.permission-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-tag {
  margin: 2px;
}

/* 功能卡片样式 */
.permission-features-section {
  margin-bottom: 20px;
}

.features-content {
  padding: 10px 0;
}

.feature-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.feature-card h4 {
  margin: 8px 0;
  color: #303133;
  font-size: 16px;
}

.feature-card p {
  margin: 4px 0 8px 0;
  color: #606266;
  font-size: 12px;
  line-height: 1.4;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .nav-menu {
    margin: 0 20px;
  }
  
  .nav-menu-container :deep(.el-menu-item) {
    margin: 0 4px;
    font-size: 14px;
    padding: 0 12px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .nav-menu {
    margin: 0;
    order: 2;
    width: 100%;
  }
  
  .nav-menu-container {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .nav-menu-container :deep(.el-menu-item) {
    height: 40px;
    line-height: 40px;
    margin: 2px;
    font-size: 13px;
    padding: 0 10px;
  }
  
  .user-info {
    order: 3;
    width: 100%;
    justify-content: center;
  }
  
  /* 功能卡片响应式 */
  .features-content .el-col {
    margin-bottom: 15px;
  }
  
  .feature-card {
    height: 120px;
    padding: 15px;
  }
  
  .feature-icon {
    font-size: 24px;
  }
  
  .feature-card h4 {
    font-size: 14px;
  }
  
  .feature-card p {
    font-size: 11px;
  }
}
</style>