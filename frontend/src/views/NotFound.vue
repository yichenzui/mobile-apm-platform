<template>
  <div class="not-found-page">
    <el-result
      icon="warning"
      title="404"
      sub-title="抱歉，您访问的页面不存在或已被移除"
    >
      <template #extra>
        <el-button
          type="primary"
          @click="$router.push('/dashboard')"
        >
          返回首页
        </el-button>
        <el-button @click="$router.back()">
          返回上一页
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
// 404 兜底页面
</script>

<style lang="scss" scoped>
.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}
</style>
