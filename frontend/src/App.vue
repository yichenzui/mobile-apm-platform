<template>
  <el-config-provider>
    <router-view />
  </el-config-provider>
</template>

<script setup>
// 这里可以添加全局配置和逻辑
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  height: 100%;
}
</style>
