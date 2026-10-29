<template>
  <div class="profile-page">
    <div class="page-header">
      <h2><el-icon><UserFilled /></el-icon> 个人中心</h2>
      <el-button
        type="default"
        @click="$router.push('/dashboard')"
      >
        <el-icon><HomeFilled /></el-icon> 返回首页
      </el-button>
    </div>

    <div class="profile-content">
      <!-- 左侧：身份卡片 -->
      <el-card
        class="identity-card"
        shadow="hover"
      >
        <div class="avatar-section">
          <div class="big-avatar">
            {{ avatarChar }}
          </div>
          <div class="user-brief">
            <h3>{{ userInfo.username || '未知用户' }}</h3>
            <el-tag
              :type="isAdmin ? 'danger' : 'primary'"
              size="small"
            >
              {{ isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <el-button
          v-if="!isAdmin"
          type="primary"
          class="logout-btn"
          style="margin-bottom: 10px"
          @click="pwdDialogVisible = true"
        >
          <el-icon><Lock /></el-icon> 修改密码
        </el-button>
        <el-tooltip
          v-else
          content="管理员暂不支持在线修改密码"
          placement="top"
        >
          <div>
            <el-button
              type="primary"
              plain
              disabled
              class="logout-btn"
              style="margin-bottom: 10px"
            >
              <el-icon><Lock /></el-icon> 修改密码
            </el-button>
          </div>
        </el-tooltip>
        <el-button
          type="danger"
          plain
          class="logout-btn"
          :loading="loggingOut"
          @click="handleLogout"
        >
          <el-icon><SwitchButton /></el-icon> 退出登录
        </el-button>
      </el-card>

      <!-- 右侧：详细信息 -->
      <el-card
        class="detail-card"
        shadow="hover"
      >
        <template #header>
          <span>身份信息</span>
        </template>
        <el-descriptions
          :column="1"
          border
          size="large"
        >
          <el-descriptions-item label="用户ID">
            {{ userInfo.userId ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userInfo.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag
              :type="isAdmin ? 'danger' : 'primary'"
              size="small"
            >
              {{ isAdmin ? 'ADMIN（管理员）' : 'USER（普通用户）' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="登录状态">
            <el-tag
              type="success"
              size="small"
            >
              在线
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览器">
            {{ browserInfo }}
          </el-descriptions-item>
          <el-descriptions-item label="本页打开时间">
            {{ pageOpenTime }}
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="isAdmin"
          title="管理员提示"
          description="您拥有全部权限：查看地域分析、版本对比、问题中心、数据采集模拟器等管理员专属功能。（管理员暂不支持在线修改密码）"
          type="info"
          :closable="false"
          style="margin-top: 16px"
        />
      </el-card>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="pwdDialogVisible"
      title="修改密码"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-width="90px"
      >
        <el-form-item
          label="原密码"
          prop="oldPassword"
        >
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item
          label="新密码"
          prop="newPassword"
        >
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="6-32位"
          />
        </el-form-item>
        <el-form-item
          label="确认新密码"
          prop="confirmPassword"
        >
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="changingPwd"
          @click="submitChangePassword"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, HomeFilled, SwitchButton, Lock } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const loggingOut = ref(false)
const pageOpenTime = ref(new Date().toLocaleString())

const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo')) || {}
  } catch {
    return {}
  }
})

const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
const avatarChar = computed(() => (userInfo.value.username || 'U').charAt(0).toUpperCase())

const browserInfo = (() => {
  const ua = navigator.userAgent
  if (ua.includes('Edg')) return 'Microsoft Edge'
  if (ua.includes('Chrome')) return 'Chrome'
  if (ua.includes('Firefox')) return 'Firefox'
  return '未知浏览器'
})()

// ===== 修改密码 =====
const pwdDialogVisible = ref(false)
const changingPwd = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度须为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const submitChangePassword = () => {
  if (!pwdFormRef.value) return
  pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      await request.post('/auth/change-password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      pwdDialogVisible.value = false
      // 修改密码后清空登录态，返回登录页
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      router.push('/login')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || e.message || '修改密码失败')
    } finally {
      changingPwd.value = false
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loggingOut.value = true
    try {
      // 通知后端吊销 token（失败不阻塞登出流程）
      await request.post('/auth/logout').catch(() => {})
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      loggingOut.value = false
      ElMessage.success('已退出登录')
      router.push('/login')
    }
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    color: #303133;
  }
}
.profile-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  max-width: 900px;
}
.identity-card {
  height: fit-content;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0 4px;
  .big-avatar {
    width: 84px;
    height: 84px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff, #7c4dff);
    color: #fff;
    font-size: 36px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
  }
  .user-brief {
    text-align: center;
    h3 {
      margin: 0 0 8px;
      color: #303133;
    }
  }
}
.logout-btn {
  width: 100%;
}
</style>
