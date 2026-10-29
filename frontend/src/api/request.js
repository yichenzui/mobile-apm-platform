import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    return response.data
  },
  error => {
    // 对响应错误做点什么
    if (error.response) {
      // 登录/注册接口返回 401 时不做全局跳转，交给页面自行提示错误，
      // 否则整页刷新会把 ElMessage 提示冲掉，用户只看到"页面闪一下"
      const reqUrl = error.config?.url || ''
      const isAuthPage = reqUrl.includes('/auth/login') || reqUrl.includes('/auth/register')
      switch (error.response.status) {
        case 401:
          if (isAuthPage) {
            return Promise.reject(error)
          }
          // 未授权，跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          // 权限不足
          break
        case 500:
          // 服务器错误
          break
        default:
      }
    }
    return Promise.reject(error)
  }
)

export default request
