<template>
  <div class="unauthorized">
    <div class="error-container">
      <h1>403</h1>
      <h2>权限不足</h2>
      <p>抱歉，您没有权限访问此页面。</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 未授权页面逻辑
</script>

<style scoped>
.unauthorized {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 70vh;
  text-align: center;
}

.error-container h1 {
  font-size: 72px;
  margin: 0;
  color: #f56c6c;
}

.error-container h2 {
  font-size: 24px;
  margin: 10px 0;
}

.error-container p {
  color: #606266;
  margin-bottom: 20px;
}

.actions {
  margin-top: 20px;
}

.actions .el-button {
  margin: 0 10px;
}
</style>