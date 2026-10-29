module.exports = {
  root: true,
  env: {
    browser: true,
    es2022: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  ignorePatterns: ['dist/', 'node_modules/', 'public/', '*.config.js'],
  rules: {
    // 单文件组件名多为单词（如 Login.vue / Profile.vue），关闭多词命名校验
    'vue/multi-word-component-names': 'off',
    'no-unused-vars': ['warn', { args: 'none' }],
    // AI 对话需要渲染 Markdown，必须用到 v-html。
    // 已在 AiAssistant.vue 的 renderMarkdown() 中先做 escapeHtml() 实体转义再拼接标签，
    // 不存在 XSS 风险，故关闭该规则（模板属性跨行时 eslint-disable 注释无法生效）。
    'vue/no-v-html': 'off'
  }
}
