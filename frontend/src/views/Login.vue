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
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
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
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
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
</style>
