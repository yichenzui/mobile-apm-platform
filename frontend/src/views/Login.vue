<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>移动应用性能监控平台</h2>
        </div>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="login-button" 
            size="large"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <!-- 演示账号快捷登录：点击后自动填入账号密码并登录 -->
        <el-divider class="demo-divider">
          演示账号一键登录
        </el-divider>
        <div class="demo-login">
          <el-button
            class="demo-btn demo-admin"
            size="large"
            :disabled="loading"
            @click="quickLogin('admin', '123456789', '管理员')"
          >
            <el-icon><Avatar /></el-icon> 管理员演示账号
          </el-button>
          <el-button
            class="demo-btn demo-user"
            size="large"
            :disabled="loading"
            @click="quickLogin('demo', '123456', '普通用户')"
          >
            <el-icon><UserFilled /></el-icon> 普通用户演示账号
          </el-button>
          <p class="demo-tip">
            管理员 admin / 123456789 · 普通用户 demo / 123456
          </p>
        </div>

        <div class="register-link">
          还没有账号？<router-link to="/register">
            立即注册
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.validate((valid) => {
    if (valid) doLogin()
  })
}

/** 演示账号快捷登录：填入账号密码后直接登录 */
const quickLogin = (username, password, roleLabel) => {
  if (loading.value) return
  loginForm.username = username
  loginForm.password = password
  loginFormRef.value?.clearValidate()
  ElMessage.info(`已填入${roleLabel}演示账号，正在登录…`)
  doLogin()
}

/** 执行登录请求 */
const doLogin = async () => {
  loading.value = true
  try {
    const response = await request.post('/auth/login', loginForm)

    // 后端返回 Result<LoginVO> 格式：{ code, message, data: { accessToken, refreshToken, userId, username, role } }
    const loginData = response.data
    if (!loginData || !loginData.accessToken) {
      throw new Error('登录响应数据异常，请联系管理员')
    }

    // 保存token
    localStorage.setItem('token', loginData.accessToken)
    localStorage.setItem('refreshToken', loginData.refreshToken)
    localStorage.setItem('userInfo', JSON.stringify({
      userId: loginData.userId,
      username: loginData.username,
      role: loginData.role
    }))

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // 后端返回的业务错误（如"用户名或密码错误"）优先展示
    ElMessage.error(error.response?.data?.message || error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #409EFF;
}

.login-button {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.register-link a {
  color: #409EFF;
  text-decoration: none;
}

/* 演示账号快捷登录 */
.demo-divider {
  margin: 0 0 16px;

  :deep(.el-divider__text) {
    font-size: 12px;
    color: #909399;
    background: #fff;
  }
}

.demo-login {
  .demo-btn {
    display: flex;
    justify-content: center;
    width: 100%;
    margin: 0 0 10px;

    & + .demo-btn {
      margin-left: 0;
    }
  }

  .demo-admin {
    border-color: #409eff;
    color: #409eff;
  }

  .demo-user {
    border-color: #67c23a;
    color: #67c23a;
  }

  .demo-tip {
    margin: 6px 0 0;
    font-size: 12px;
    line-height: 1.6;
    color: #909399;
    text-align: center;
  }
}

/* ===== 移动端（手机竖屏） ===== */
@media (max-width: 768px) {
  .login-container {
    align-items: flex-start;
    padding: 40px 16px 24px;
  }

  .card-header h2 {
    font-size: 18px;
  }
}
</style>
