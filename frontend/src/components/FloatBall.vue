<template>
  <div
    ref="ballRef"
    class="float-ball"
  >
    <!-- 悬浮球主体 -->
    <div
      class="ball"
      :class="{ expanded: isExpanded }"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @mousedown="onMouseDown"
      @click="handleClick"
    >
      <div class="ball-inner">
        <!-- 收起状态：AI 图标 -->
        <transition
          name="fade"
          mode="out-in"
        >
          <div
            v-if="!isExpanded"
            key="avatar"
            class="ball-avatar"
          >
            <el-icon :size="28">
              <MagicStick />
            </el-icon>
          </div>
          <div
            v-else
            key="actions"
            class="ball-actions"
          >
            <span class="action-label">AI助手</span>
          </div>
        </transition>
      </div>
    </div>

    <!-- 展开后的快捷面板（可选） -->
    <transition name="panel-fade">
      <div
        v-if="isExpanded && showPanel"
        class="quick-panel"
        :style="getPanelStyle()"
      >
        <div class="panel-header">
          <span>AI 智能助手</span>
          <el-icon
            class="close-btn"
            @click.stop="toggleExpand"
          >
            <Close />
          </el-icon>
        </div>
        <div class="panel-body">
          <p class="panel-desc">
            有什么我可以帮你的吗？
          </p>
          <el-button
            type="primary"
            size="small"
            round
            @click="goToAiPage"
          >
            <el-icon><ChatDotRound /></el-icon> 打开完整对话
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick, Close, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const ballRef = ref(null)

// 悬浮球位置
const position = reactive({ x: window.innerWidth - 80, y: window.innerHeight - 200 })
const isExpanded = ref(false)
const showPanel = ref(false)
let isDragging = false
let dragStartX = 0
let dragStartY = 0
let dragOffsetX = 0
let dragOffsetY = 0
// 标记本次交互是否真的移动过：mouseup 后再派发的 click 需要据此忽略
let hasMoved = false

/** 鼠标按下 */
function onMouseDown(e) {
  // 忽略非左键点击
  if (e.button !== 0) return
  isDragging = true
  hasMoved = false
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragOffsetX = e.clientX - position.x
  dragOffsetY = e.clientY - position.y

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  document.body.style.userSelect = 'none'

  // 隐藏展开面板
  if (isExpanded.value) {
    setTimeout(() => { isExpanded.value = false; showPanel.value = false }, 100)
  }
}

/** 鼠标移动 */
function onMouseMove(e) {
  if (!isDragging) return

  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY

  // 超过5px才算拖动（避免误触）
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
    hasMoved = true
    let newX = e.clientX - dragOffsetX
    let newY = e.clientY - dragOffsetY

    // 边界限制
    const ballSize = 56
    newX = Math.max(0, Math.min(window.innerWidth - ballSize, newX))
    newY = Math.max(60, Math.min(window.innerHeight - ballSize, newY))

    position.x = newX
    position.y = newY
  }
}

/** 鼠标释放 */
function onMouseUp() {
  if (isDragging) {
    isDragging = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    document.body.style.userSelect = ''

    // 吸附到屏幕边缘
    snapToEdge()

    // click 事件在 mouseup 之后才派发，这里延后清除移动标记，
    // 否则 handleClick 里的守卫会失效（拖完松手会误展开面板）
    setTimeout(() => { hasMoved = false }, 0)
  }
}

/** 吸附到边缘 */
function snapToEdge() {
  const ballCenter = position.x + 28
  if (ballCenter < window.innerWidth / 2) {
    // 靠近左边 → 吸到左侧
    position.x = 8
  } else {
    // 靠近右边 → 吸到右侧
    position.x = window.innerWidth - 64
  }
}

/** 点击事件 */
function handleClick(e) {
  // 刚结束拖动则不触发点击（避免拖完松手误展开/收起面板）
  if (isDragging || hasMoved) return
  toggleExpand()
}

/** 切换展开状态 */
function toggleExpand() {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    setTimeout(() => { showPanel.value = true }, 150)
  }
}

/** 进入 AI 页面 */
function goToAiPage() {
  router.push('/ai-assistant')
  isExpanded.value = false
  showPanel.value = false
}

/** 面板位置 */
function getPanelStyle() {
  const right = position.x + 56 <= window.innerWidth / 2 ? 'auto' : `${window.innerWidth - position.x}px`
  const left = position.x + 56 <= window.innerWidth / 2 ? `${position.x + 68}px` : 'auto'
  const top = `${position.y - 10}px`
  return { left, top, right }
}

/** 窗口大小变化时重新定位 */
function onResize() {
  position.x = Math.min(position.x, window.innerWidth - 64)
  position.y = Math.min(position.y, window.innerHeight - 70)
}

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  // 若卸载时仍处于拖拽中，必须解绑 document 监听并恢复文本可选中状态，
  // 否则之后整个页面都无法选中文字，且监听器持续泄漏
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.body.style.userSelect = ''
  isDragging = false
  hasMoved = false
})
</script>

<style lang="scss" scoped>
.float-ball {
  position: fixed;
  z-index: 9999;
  pointer-events: none; /* 让内部元素接收事件 */

  * {
    pointer-events: auto;
  }
}

.ball {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  cursor: grab;
  position: fixed;
  z-index: 9999;
  transition: box-shadow 0.3s;
  user-select: none;

  &:hover {
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
  }

  &:active {
    cursor: grabbing;
    transform: scale(0.95);
  }

  &.expanded {
    border-radius: 16px;
    width: auto;
    padding: 8px 18px;
  }
}

.ball-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: inherit;
  color: #fff;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);

  .expanded & {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
  }
}

.ball-avatar {
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 26px;
  }
}

.ball-actions {
  display: flex;
  align-items: center;
  gap: 4px;

  .action-label {
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
  }
}

/* 快捷面板 */
.quick-panel {
  position: fixed;
  width: 250px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  z-index: 9998;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;

  .close-btn {
    cursor: pointer;
    opacity: 0.7;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }
  }
}

.panel-body {
  padding: 18px;
  text-align: center;

  .panel-desc {
    margin: 0 0 14px;
    font-size: 13px;
    color: #909399;
  }

  .el-button {
    width: 100%;

    .el-icon {
      margin-right: 4px;
    }
  }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.85);
}

.panel-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.panel-fade-leave-active {
  transition: all 0.2s ease;
}
.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(10px);
}
</style>
