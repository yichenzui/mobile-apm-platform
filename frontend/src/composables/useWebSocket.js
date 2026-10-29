import { ref } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * WebSocket STOMP 实时通信 Composable
 * 用于接收告警推送、KPI 更新等实时数据
 */
export function useWebSocket() {
  const client = ref(null)
  const connected = ref(false)
  const alerts = ref([])
  const kpiUpdates = ref([])
  const notifications = ref([])

  let stompClient = null

  const connect = () => {
    try {
      // 创建 SockJS 连接（静默失败，不影响页面其他功能）
      const socket = new SockJS('/ws-stomp', null, { timeout: 5000 })
      stompClient = new Client({
        webSocketFactory: () => socket,
        debug: (msg) => { /* 生产环境关闭debug */ },
        reconnectDelay: 5000, // 断线自动重连，5秒
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        connectionTimeout: 5000,

        onConnect: () => {
          connected.value = true
          console.log('[WebSocket] 已连接')

          // 订阅告警推送
          stompClient.subscribe('/topic/alerts', (message) => {
            try {
              const data = JSON.parse(message.body)
              alerts.value.unshift({ ...data, receivedAt: Date.now() })
              if (alerts.value.length > 50) alerts.value.pop()
            } catch (e) {
              console.error('[WebSocket] 解析告警消息失败:', e)
            }
          })

          // 订阅告警状态变更
          stompClient.subscribe('/topic/alerts/status', (message) => {
            try {
              const data = JSON.parse(message.body)
              // 更新本地告警状态
              const idx = alerts.value.findIndex(a => a.alertId === data.alertId)
              if (idx >= 0) {
                alerts.value[idx].status = data.status
                alerts.value[idx] = { ...alerts.value[idx] }
              }
            } catch (e) {
              console.error('[WebSocket] 解析告警状态变更失败:', e)
            }
          })

          // 订阅系统通知
          stompClient.subscribe('/topic/notifications', (message) => {
            try {
              const data = JSON.parse(message.body)
              notifications.value.unshift({ ...data, receivedAt: Date.now() })
              if (notifications.value.length > 20) notifications.value.pop()
            } catch (e) {
              console.error('[WebSocket] 解析通知失败:', e)
            }
          })
        },

        onDisconnect: () => {
          connected.value = false
          console.log('[WebSocket] 已断开，将自动重连...')
        },

        onStompError: (frame) => {
          console.error('[WebSocket] STOMP 错误:', frame.headers?.message || frame)
        }
      })

      stompClient.activate()
      client.value = stompClient
    } catch (error) {
      console.error('[WebSocket] 连接失败:', error)
      connected.value = false
    }
  }

  const disconnect = () => {
    if (stompClient) {
      try {
        stompClient.deactivate()
      } catch (e) {
        console.error('[WebSocket] 断开失败:', e)
      }
      connected.value = false
    }
  }

  // 订阅 KPI 实时更新（针对特定应用）
  const subscribeKpi = (appKey, callback) => {
    if (!stompClient || !connected.value || !appKey) return null
    try {
      const subscription = stompClient.subscribe(`/topic/kpi/${appKey}`, (message) => {
        try {
          const data = JSON.parse(message.body)
          kpiUpdates.value.unshift(data)
          if (kpiUpdates.value.length > 30) kpiUpdates.value.pop()
          callback?.(data)
        } catch (e) {
          console.error('[WebSocket] 解析KPI数据失败:', e)
        }
      })
      return subscription
    } catch (e) {
      console.error('[WebSocket] 订阅KPI失败:', e)
      return null
    }
  }

  return {
    connected,
    alerts,
    kpiUpdates,
    notifications,
    connect,
    disconnect,
    subscribeKpi
  }
}
