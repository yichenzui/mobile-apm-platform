import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 响应式判断当前是否为移动端窄屏（默认断点 768px，与 styles/responsive.css 保持一致）
 * 用于需要在模板中切换属性的场景（如 el-descriptions 的列数），纯样式场景直接用 CSS 媒体查询即可
 */
export function useIsMobile(breakpoint = 768) {
  const isMobile = ref(typeof window !== 'undefined' && window.innerWidth <= breakpoint)

  const update = () => {
    isMobile.value = window.innerWidth <= breakpoint
  }

  onMounted(() => {
    update()
    window.addEventListener('resize', update)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', update)
  })

  return isMobile
}
