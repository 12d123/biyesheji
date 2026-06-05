# 基于 Spring Boot + Vue3 的智能仓储环境监测与管理系统

## 一、项目介绍

本项目是一个面向仓储环境管理场景的智能环境监测与管理系统，主要用于对仓库中的温度、湿度、PM2.5、烟雾浓度等环境数据进行采集、存储、查询和前端展示。

系统采用前后端分离架构，后端基于 Spring Boot 开发，前端基于 Vue3 开发，数据库使用 MySQL。项目结合 STM32、ESP8266 和传感器模块，实现物联网设备数据与 Web 管理平台之间的联动。

本项目来源于个人毕业设计，主要用于实践 Java 后端开发、Vue3 前端开发、MySQL 数据库设计、接口联调以及物联网数据接入等内容。

## 二、项目技术栈

### 1. 后端技术

* Java
* Spring Boot
* MySQL
* MyBatis / MyBatis-Plus
* RESTful API
* Maven

### 2. 前端技术

* Vue3
* JavaScript
* Axios
* HTML
* CSS

### 3. 物联网与硬件相关

* STM32
* ESP8266
* 温湿度传感器
* PM2.5 传感器
* 烟雾浓度传感器

### 4. 开发与调试工具

* IntelliJ IDEA
* VS Code
* Navicat
* Postman
* Git / GitHub

## 三、系统主要功能

### 1. 环境数据采集

系统可接收温度、湿度、PM2.5、烟雾浓度等环境监测数据，用于模拟或接入真实仓储环境监测场景。

### 2. 环境数据上传

硬件端通过 STM32 采集传感器数据，并借助 ESP8266 将数据上传至后端系统。
在无硬件环境下，也可以通过 Postman 模拟硬件端上传数据，方便接口测试和系统调试。

### 3. 数据库存储

后端接收到环境数据后，将数据保存至 MySQL 数据库中，便于后续查询、展示和管理。

### 4. 环境数据展示

前端页面用于展示温度、湿度、PM2.5、烟雾浓度等环境数据，使仓储环境状态更加直观。

### 5. 历史数据查询

系统支持环境监测历史数据查询，便于查看仓储环境变化情况。

### 6. 设备状态管理

系统可扩展设备信息管理功能，用于展示设备编号、设备名称、设备状态等信息。

## 四、项目数据流

```text
温湿度 / PM2.5 / 烟雾浓度传感器
        ↓
      STM32
        ↓
     ESP8266
        ↓
 Spring Boot 后端接口
        ↓
      MySQL 数据库
        ↓
     Vue3 前端页面展示
```

## 五、项目目录结构

```text
biyesheji
├── backend
│   └── warehouse-backend
│       ├── src
│       │   ├── main
│       │   │   ├── java
│       │   │   └── resources
│       │   └── test
│       └── pom.xml
│
├── frontend
│   ├── src
│   ├── package.json
│   └── vite.config.js
│
├── .gitignore
└── README.md
```

说明：
`backend` 目录存放 Spring Boot 后端代码；
`frontend` 目录存放 Vue3 前端代码；
`.gitignore` 用于排除不需要上传的文件；
`README.md` 为项目说明文档。

## 六、数据库设计示例

### 1. 环境数据表：environment_data

该表用于存储仓储环境监测数据，包括温度、湿度、PM2.5、烟雾浓度和采集时间等信息。

```sql
CREATE TABLE environment_data (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  temperature DOUBLE COMMENT '温度',
  humidity DOUBLE COMMENT '湿度',
  pm25 DOUBLE COMMENT 'PM2.5数值',
  smoke DOUBLE COMMENT '烟雾浓度',
  create_time DATETIME COMMENT '采集时间'
);
```

### 2. 设备信息表：device

该表用于存储设备基础信息，可用于后续扩展设备管理功能。

```sql
CREATE TABLE device (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  device_code VARCHAR(50) COMMENT '设备编号',
  device_name VARCHAR(100) COMMENT '设备名称',
  status VARCHAR(20) COMMENT '设备状态',
  update_time DATETIME COMMENT '更新时间'
);
```

## 七、接口示例

### 1. 上传环境数据

```text
POST /api/env/upload
```

请求示例：

```json
{
  "temperature": 26.5,
  "humidity": 58,
  "pm25": 35,
  "smoke": 12
}
```

功能说明：
用于接收硬件端或 Postman 模拟上传的环境监测数据，并将数据保存到 MySQL 数据库中。

### 2. 查询环境数据列表

```text
GET /api/env/list
```

功能说明：
用于查询历史环境监测数据，前端页面可调用该接口展示数据列表。

### 3. 查询最新环境数据

```text
GET /api/env/latest
```

功能说明：
用于查询最新一条环境监测数据，便于前端展示当前仓储环境状态。

### 4. 查询设备信息列表

```text
GET /api/device/list
```

功能说明：
用于查询设备编号、设备名称、设备状态等基础信息。

## 八、项目运行方式

### 1. 后端运行

1. 使用 IntelliJ IDEA 打开 `backend/warehouse-backend` 后端项目；
2. 在 MySQL 中创建对应数据库；
3. 修改 `application.yml` 或 `application.properties` 中的数据库连接信息；
4. 启动 Spring Boot 项目；
5. 使用 Postman 测试后端接口是否正常。

数据库连接配置示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/warehouse_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 前端运行

1. 使用 VS Code 打开 `frontend` 前端项目；
2. 安装依赖：

```bash
npm install
```

3. 启动前端项目：

```bash
npm run dev
```

4. 在浏览器中访问前端页面。

## 九、接口测试方式

项目可使用 Postman 模拟硬件端上传环境数据。

示例请求：

```text
POST /api/env/upload
Content-Type: application/json
```

请求体：

```json
{
  "temperature": 26.5,
  "humidity": 58,
  "pm25": 35,
  "smoke": 12
}
```

测试流程：

1. 使用 Postman 调用上传接口；
2. 查看接口是否返回成功结果；
3. 在 MySQL 中查询数据是否成功入库；
4. 打开前端页面，检查页面展示数据是否与数据库一致。

## 十、项目亮点

1. 采用 Spring Boot + Vue3 前后端分离架构，项目结构较清晰，便于后续维护和扩展。
2. 结合 STM32、ESP8266 和传感器模块，体现物联网设备数据采集与 Web 系统联动能力。
3. 使用 MySQL 对环境监测数据进行持久化存储，支持历史数据查询和后续数据分析。
4. 支持通过 Postman 模拟硬件数据上传，便于在无硬件环境下进行接口测试和系统调试。
5. 项目覆盖后端接口开发、前端页面展示、数据库设计、接口联调和 GitHub 项目托管等实践内容。

## 十一、项目不足与后续优化方向

当前项目主要完成了环境数据采集、上传、存储和展示等基础功能，后续可从以下方向继续优化：

1. 增加报警阈值设置功能，当温度、湿度、PM2.5 或烟雾浓度超过设定范围时进行提醒。
2. 增加历史数据图表展示功能，使环境变化趋势更加直观。
3. 增加用户登录与权限管理功能，提高系统安全性。
4. 增加设备异常状态提醒功能，提升系统实用性。
5. 优化硬件端数据上传稳定性，增强异常数据过滤和处理能力。
6. 后续可接入 AI 分析能力，对仓储环境数据进行异常判断和趋势分析。

## 十二、项目说明

本项目为个人毕业设计项目，主要用于学习和实践 Spring Boot、Vue3、MySQL 以及物联网数据接入等相关技术。项目重点在于理解前后端分离开发流程、数据库设计、接口联调、硬件数据接入和项目版本管理。
