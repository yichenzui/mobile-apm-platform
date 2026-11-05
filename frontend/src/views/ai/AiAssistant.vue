<template>
  <div class="ai-assistant">
    <!-- 头部 -->
    <div class="ai-header">
      <div class="header-left">
        <el-icon
          :size="24"
          color="#409EFF"
        >
          <MagicStick />
        </el-icon>
        <h2>AI 智能助手</h2>
        <el-tag
          type="success"
          size="small"
        >
          DeepSeek
        </el-tag>
      </div>
      <div class="header-right">
        <el-button
          type="default"
          size="small"
          @click="$router.push('/dashboard')"
        >
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button
          type="primary"
          :icon="DataAnalysis"
          :loading="diagnosing"
          size="small"
          @click="runDiagnosis"
        >
          {{ diagnosing ? '诊断中...' : '一键诊断' }}
        </el-button>
        <el-select
          v-model="diagApp"
          style="width: 140px"
          size="small"
          placeholder="选择应用"
        >
          <el-option
            v-for="app in appList"
            :key="app.appKey"
            :label="app.name"
            :value="app.appKey"
          />
        </el-select>
        <el-button
          text
          @click="clearHistory"
        >
          <el-icon><Delete /></el-icon> 清空对话
        </el-button>
        <el-tag
          v-if="modelInfo"
          type="info"
          size="small"
        >
          {{ modelInfo }}
        </el-tag>
      </div>
    </div>

    <!-- 消息区域 -->
    <div
      ref="chatContainerRef"
      class="chat-container"
    >
      <!-- 欢迎消息 -->
      <div
        v-if="messages.length === 0"
        class="welcome-section"
      >
        <div class="welcome-icon">
          <el-icon
            :size="48"
            color="#409EFF"
          >
            <MagicStick />
          </el-icon>
        </div>
        <h3>你好，我是 APM 智能助手</h3>
        <p class="welcome-desc">
          我可以帮你分析应用性能数据、诊断告警问题、提供优化建议
        </p>

        <div class="quick-actions">
          <p class="quick-title">
            试试问我：
          </p>
          <div class="quick-cards">
            <div
              v-for="(item, index) in quickQuestions"
              :key="index"
              class="quick-card"
              @click="sendQuickQuestion(item.question)"
            >
              <el-icon :size="20">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 对话消息列表 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-item', msg.role === 'user' ? 'user-msg' : 'ai-msg']"
      >
        <div class="avatar">
          <el-avatar
            :size="36"
            :style="{ backgroundColor: msg.role === 'user' ? '#409EFF' : '#67C23A' }"
          >
            <el-icon
              v-if="msg.role === 'user'"
              :size="18"
            >
              <User />
            </el-icon>
            <el-icon
              v-else
              :size="18"
            >
              <MagicStick />
            </el-icon>
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-role">
            {{ msg.role === 'user' ? '你' : 'AI 助手' }}
          </div>
          <!-- 安全说明：renderMarkdown() 内部先执行 escapeHtml() 转义 HTML 实体后才拼接标签，已防 XSS -->
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div
            class="message-text"
            v-html="renderMarkdown(msg.content)"
          />
          <div
            v-if="msg.usage"
            class="usage-info"
          >
            <span>Tokens: {{ msg.usage.promptTokens }} / {{ msg.usage.completionTokens }}</span>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div
        v-if="loading"
        class="message-item ai-msg"
      >
        <div class="avatar">
          <el-avatar
            :size="36"
            style="background-color: #67C23A"
          >
            <el-icon :size="18">
              <MagicStick />
            </el-icon>
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-role">
            AI 助手
          </div>
          <div class="typing-indicator">
            <span /><span /><span />
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-wrapper">
        <el-input
          ref="inputRef"
          v-model="inputMessage"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 5 }"
          placeholder="输入您的问题... (Enter 发送，Shift+Enter 换行)"
          :disabled="loading"
          @keydown.enter.exact.prevent="onEnterSend"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          circle
          :loading="loading"
          :disabled="!inputMessage.trim()"
          class="send-btn"
          @click="handleSend"
        />
      </div>
      <div class="input-hint">
        AI 回答仅供参考，重要决策请结合实际情况判断
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, User, Delete, Promotion, DataAnalysis, HomeFilled } from '@element-plus/icons-vue'
import { sendMessage, runDiagnose } from '@/api/ai'
import { getAppList } from '@/api/dashboard'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const diagnosing = ref(false)
const modelInfo = ref('')
const chatContainerRef = ref(null)
const inputRef = ref(null)
const appList = ref([])
const diagApp = ref('test-app')

// 快捷问题
const quickQuestions = [
  { label: '分析崩溃率', icon: 'DataAnalysis', question: '当前应用的崩溃率偏高，请帮我分析可能的原因和优化方案' },
  { label: '解读告警', icon: 'Warning', question: '收到内存使用率过高的告警，应该如何排查和处理？' },
  { label: '性能优化', icon: 'TrendCharts', question: 'App 启动时间过长，有哪些常见的优化手段？' },
  { label: '通用提问', icon: 'QuestionFilled', question: '移动应用性能监控中，最重要的指标有哪些？' }
]

/** 一键智能诊断 */
async function runDiagnosis() {
  if (!diagApp.value) {
    ElMessage.warning('请先选择要诊断的应用')
    return
  }
  diagnosing.value = true
  try {
    const response = await runDiagnose({
      appKey: diagApp.value,
      startTime: new Date(Date.now() - 7 * 86400000).toISOString(),
      endTime: new Date().toISOString(),
      dimension: 'all'
    })
    const data = response.data || {}
    // 将诊断报告作为AI消息显示
    let reportContent = data.report || '诊断完成，暂无详细报告。'
    if (data.riskLevel) {
      reportContent = `> **综合风险等级：${data.riskLevel}**\n\n${reportContent}`
    }
    if (data.keyMetrics) {
      reportContent = `## 关键指标摘要\n\n| 指标 | 数值 |\n|------|------|\n| 崩溃率 | ${Number(data.keyMetrics.crashRate ?? 0).toFixed(2)}% |\n| 平均启动时间 | ${data.keyMetrics.avgStartupTime?.toFixed(0)}ms |\n| 网络成功率 | ${data.keyMetrics.networkSuccessRate?.toFixed(1)}% |\n| 活跃用户 | ${data.keyMetrics.activeUsers} |\n| 告警数 | ${data.keyMetrics.alertCount} |\n\n${reportContent}`
    }
    messages.value.push({ role: 'user', content: `请对应用【${diagApp.value}】进行一键智能诊断` })
    messages.value.push({ role: 'assistant', content: reportContent })
    if (data.model) modelInfo.value = data.model
    ElMessage.success('诊断完成！')
    scrollToBottom()
  } catch (error) {
    console.error('诊断失败:', error)
    ElMessage.error('诊断失败: ' + (error.response?.data?.message || error.message))
  } finally {
    diagnosing.value = false
  }
}

/** HTML 实体转义，防止 v-html 注入（AI 返回内容可能包含业务数据或恶意脚本） */
function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/** 渲染 Markdown 表格块（全部行形如 |a|b|，第二行为分隔行则视为表头） */
function renderTableBlock(block) {
  const rows = block
    .trim()
    .split('\n')
    .map(r => r.trim())
    .filter(r => r.startsWith('|') && r.endsWith('|'))
  if (rows.length < 2) return block
  const cells = row => row.slice(1, -1).split('|').map(c => c.trim())
  const isDivider = row => /^\|[\s:|-]+\|$/.test(row)
  const head = isDivider(rows[1]) ? cells(rows[0]) : null
  const body = (head ? rows.slice(2) : rows).filter(r => !isDivider(r)).map(cells)
  const th = head ? '<tr>' + head.map(c => `<th>${c}</th>`).join('') + '</tr>' : ''
  const tr = body.map(r => '<tr>' + r.map(c => `<td>${c}</td>`).join('') + '</tr>').join('')
  return `<table class="md-table">${th ? `<thead>${th}</thead>` : ''}<tbody>${tr}</tbody></table>`
}

/** 渲染简单 Markdown（先转义再渲染，防 XSS；块级语法必须在换行替换前处理） */
function renderMarkdown(text) {
  if (!text) return ''
  // 必须先转义再渲染 Markdown，否则 v-html 会被注入脚本（XSS）
  let html = escapeHtml(text)
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="code-block"><code>$2</code></pre>')
    // 表格：连续以 | 开头结尾的行
    .replace(/(?:^\|.*\|[ \t]*\n?)+/gm, block => renderTableBlock(block))
    // 标题（# ~ ######）
    .replace(/^#{1,2}[ \t]+(.+)$/gm, '<div class="md-h2">$1</div>')
    .replace(/^#{3,6}[ \t]+(.+)$/gm, '<div class="md-h3">$1</div>')
    // 引用（去掉引用内可能出现的 # 标题标记）、列表、分隔线
    .replace(/^&gt;[ \t]*$/gm, '')
    .replace(/^&gt;[ \t]*(?:#{1,6}[ \t]*)?(\S.*)$/gm, '<blockquote class="md-quote">$1</blockquote>')
    .replace(/^[-*][ \t]+(.+)$/gm, '<li>$1</li>')
    .replace(/^(?:-{3,}|\*{3,})[ \t]*$/gm, '<hr class="md-hr">')
    // 行内样式
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  // 换行放最后，避免块级语法的行首锚点失效
  return html.replace(/\n/g, '<br>')
}

/** Enter 发送（排除中文输入法组合态，避免选词时误发送） */
function onEnterSend(e) {
  if (e && (e.isComposing || e.keyCode === 229)) return
  handleSend()
}

/** 发送消息 */
async function handleSend() {
  const content = inputMessage.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const historyMessages = messages.value.slice(-10).map(m => ({
      role: m.role,
      content: m.content
    }))

    const response = await sendMessage({
      content,
      messages: historyMessages.slice(0, -1)
    })

    const data = response.data
    messages.value.push({
      role: 'assistant',
      content: data.content || '抱歉，我暂时无法回答这个问题。',
      usage: {
        promptTokens: data.promptTokens,
        completionTokens: data.completionTokens
      }
    })

    if (data.model && !modelInfo.value) {
      modelInfo.value = data.model
    }

    scrollToBottom()
  } catch (error) {
    console.error('AI 调用失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '请求失败'
    messages.value.push({
      role: 'assistant',
      content: `**错误**: ${errorMsg}。请检查网络连接或稍后重试。`
    })
    scrollToBottom()
  } finally {
    loading.value = false
    inputRef.value?.focus()
  }
}

function sendQuickQuestion(question) {
  inputMessage.value = question
  handleSend()
}

function clearHistory() {
  messages.value = []
  modelInfo.value = ''
  inputRef.value?.focus()
}

function scrollToBottom() {
  nextTick(() => {
    const container = chatContainerRef.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

onMounted(async () => {
  inputRef.value?.focus()
  try {
    const res = await getAppList()
    appList.value = res.data || []
    if (appList.value.length > 0) diagApp.value = appList.value[0].appKey
  } catch (e) { console.error(e) }
})
</script>

<style lang="scss" scoped>
.ai-assistant {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f0fe 100%);

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    h2 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

/* 欢迎页面 */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;

  h3 {
    margin: 16px 0 8px;
    color: #303133;
  }

  .welcome-desc {
    color: #909399;
    margin-bottom: 32px;
    max-width: 400px;
  }

  .quick-title {
    color: #606266;
    font-size: 14px;
    margin-bottom: 12px;
  }

  .quick-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    max-width: 480px;
  }

  .quick-card {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    color: #606266;

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
      color: #409eff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }

    .el-icon {
      font-size: 18px;
    }
  }
}

/* 消息样式 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user-msg {
    flex-direction: row-reverse;

    .message-content {
      text-align: right;
    }
  }

  &.ai-msg {
    flex-direction: row;
  }
}

.message-content {
  max-width: 70%;
  min-width: 100px;
}

.message-role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  padding: 0 4px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.7;
  word-break: break-word;
  font-size: 14px;
}

.user-msg .message-text {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.ai-msg .message-text {
  background: #f5f7fa;
  color: #303133;
  border-top-left-radius: 4px;

  /* AI 回复由 v-html 渲染，节点没有 scoped 属性，必须用 :deep() 才能命中样式 */
  :deep(.code-block) {
    background: #282c34;
    color: #abb2bf;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 8px 0;
    font-size: 13px;
  }

  :deep(.inline-code) {
    background: #e8e8e8;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
    color: #e74c3c;
  }

  :deep(strong) {
    color: #303133;
  }

  :deep(li) {
    list-style-position: inside;
    margin-left: 4px;
  }

  :deep(.md-h2) {
    font-size: 16px;
    font-weight: 600;
    margin: 14px 0 8px;
    color: #303133;
  }

  :deep(.md-h3) {
    font-size: 15px;
    font-weight: 600;
    margin: 10px 0 6px;
    color: #303133;
  }

  :deep(.md-hr) {
    border: none;
    border-top: 1px solid #dcdfe6;
    margin: 12px 0;
  }

  :deep(.md-table) {
    width: auto;
    max-width: 100%;
    border-collapse: collapse;
    margin: 8px 0;
    font-size: 13px;
    background: #fff;
  }

  :deep(.md-table th),
  :deep(.md-table td) {
    border: 1px solid #e4e7ed;
    padding: 6px 10px;
    text-align: left;
  }

  :deep(.md-table th) {
    background: #f0f2f5;
    font-weight: 600;
  }

  :deep(.md-quote) {
    padding: 8px 12px;
    margin: 8px 0;
    border-left: 3px solid #409eff;
    background: #ecf5ff;
    color: #606266;
    font-style: italic;
  }
}

.usage-info {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 6px;
  padding: 0 4px;
}

/* 输入动画 */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 12px;
  border-top-left-radius: 4px;

  span {
    width: 8px;
    height: 8px;
    background: #409eff;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-area {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;

  .input-wrapper {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 12px;
    padding: 8px 12px;
    transition: border-color 0.3s;

    &:hover,
    &:focus-within {
      border-color: #409eff;
    }

    .el-textarea {
      flex: 1;

      :deep(.el-textarea__inner) {
        border: none;
        box-shadow: none;
        padding: 4px 0;
        resize: none;
        line-height: 1.6;
      }
    }

    .send-btn {
      flex-shrink: 0;
    }
  }

  .input-hint {
    font-size: 11px;
    color: #c0c4cc;
    text-align: center;
    margin-top: 8px;
  }
}

/* ===== 移动端（手机竖屏） ===== */
@media (max-width: 768px) {
  .ai-assistant {
    height: calc(100vh - 24px);
    height: calc(100dvh - 24px);
  }

  .ai-header {
    flex-wrap: wrap;
    padding: 10px 12px;
    gap: 8px;

    .header-left h2 {
      font-size: 16px;
    }

    .header-right {
      width: 100%;
      flex-wrap: wrap;
      gap: 6px;

      :deep(.el-select) {
        width: 100% !important;
      }
    }
  }

  .chat-container {
    padding: 12px;
  }

  .welcome-section {
    padding: 24px 8px;

    .quick-cards {
      grid-template-columns: 1fr;
    }
  }

  .input-area {
    padding: 12px;
  }
}
</style>
