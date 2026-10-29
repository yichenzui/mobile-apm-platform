/**
 * 严重程度（severity）映射工具
 *
 * 统一维护映射口径，避免各页面重复定义导致不一致：
 * - issue  表：severity 为数字  1=低 2=中 3=高 4=严重
 * - alert  表：severity 为字符串 LOW / MEDIUM / HIGH / CRITICAL
 *
 * 两个函数均同时兼容数字与字符串，便于在仪表盘等混合场景下复用。
 */

/** issue 数字 severity → Element Plus Tag 类型 */
const ISSUE_SEVERITY_TYPE = {
  1: 'info',
  2: 'warning',
  3: 'danger',
  4: 'danger'
}

/** issue 数字 severity → 中文 */
const ISSUE_SEVERITY_TEXT = {
  1: '低',
  2: '中',
  3: '高',
  4: '严重'
}

/** alert 字符串 severity → Element Plus Tag 类型 */
const ALERT_SEVERITY_TYPE = {
  LOW: 'info',
  MEDIUM: 'warning',
  HIGH: 'danger',
  CRITICAL: 'danger'
}

/** alert 字符串 severity → 中文 */
const ALERT_SEVERITY_TEXT = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '严重'
}

/** 判断是否为空值（null / undefined / 空串） */
const isBlank = (v) => v === null || v === undefined || v === ''

/**
 * 获取严重程度对应的 Tag 类型
 * @param {number|string} severity issue 的 1-4 或 alert 的 LOW/MEDIUM/HIGH/CRITICAL
 * @returns {string} Element Plus 的 Tag type
 */
export const getSeverityType = (severity) => {
  if (isBlank(severity)) return 'info'

  // 数字型（issue）：1-4
  const num = Number(severity)
  if (!Number.isNaN(num) && ISSUE_SEVERITY_TYPE[num]) {
    return ISSUE_SEVERITY_TYPE[num]
  }

  // 字符串型（alert）：统一大写后匹配，兼容后端大小写差异
  const key = String(severity).toUpperCase()
  return ALERT_SEVERITY_TYPE[key] || 'info'
}

/**
 * 获取严重程度中文文本
 * @param {number|string} severity issue 的 1-4 或 alert 的 LOW/MEDIUM/HIGH/CRITICAL
 * @returns {string} 中文严重程度；无法识别时返回原值
 */
export const getSeverityText = (severity) => {
  if (isBlank(severity)) return '-'

  const num = Number(severity)
  if (!Number.isNaN(num) && ISSUE_SEVERITY_TEXT[num] !== undefined) {
    return ISSUE_SEVERITY_TEXT[num]
  }

  const key = String(severity).toUpperCase()
  return ALERT_SEVERITY_TEXT[key] || String(severity)
}

/** 供下拉筛选使用的 issue 严重程度选项（值为字符串，避免 0/'' 的真值判断问题） */
export const ISSUE_SEVERITY_OPTIONS = [
  { label: '低', value: '1' },
  { label: '中', value: '2' },
  { label: '高', value: '3' },
  { label: '严重', value: '4' }
]

/** 供下拉筛选使用的 alert 严重程度选项 */
export const ALERT_SEVERITY_OPTIONS = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
  { label: '严重', value: 'CRITICAL' }
]
