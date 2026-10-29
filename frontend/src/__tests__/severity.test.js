import { describe, it, expect } from 'vitest'
import { getSeverityType, getSeverityText } from '@/utils/severity'

describe('severity 映射 - issue 数字（1=低 2=中 3=高 4=严重）', () => {
  it('四个等级映射正确', () => {
    expect(getSeverityText(1)).toBe('低')
    expect(getSeverityText(2)).toBe('中')
    expect(getSeverityText(3)).toBe('高')
    expect(getSeverityText(4)).toBe('严重')
  })

  it('Tag 类型映射正确', () => {
    expect(getSeverityType(1)).toBe('info')
    expect(getSeverityType(2)).toBe('warning')
    expect(getSeverityType(3)).toBe('danger')
    expect(getSeverityType(4)).toBe('danger')
  })

  // 回归用例：IssueDetail.vue 曾按 0-3 解析，导致 4(严重) 显示成原始数字、"低"被错显示为"中"
  it('数字 4 必须识别为「严重」而不是回退成原值', () => {
    expect(getSeverityText(4)).not.toBe('4')
    expect(getSeverityText(4)).toBe('严重')
    expect(getSeverityType(4)).toBe('danger')
  })

  it('字符串形式的数字也能正确解析', () => {
    expect(getSeverityText('1')).toBe('低')
    expect(getSeverityText('3')).toBe('高')
  })
})

describe('severity 映射 - alert 字符串（LOW/MEDIUM/HIGH/CRITICAL）', () => {
  it('四个等级映射正确', () => {
    expect(getSeverityText('LOW')).toBe('低')
    expect(getSeverityText('MEDIUM')).toBe('中')
    expect(getSeverityText('HIGH')).toBe('高')
    expect(getSeverityText('CRITICAL')).toBe('严重')
  })

  it('Tag 类型映射正确', () => {
    expect(getSeverityType('LOW')).toBe('info')
    expect(getSeverityType('MEDIUM')).toBe('warning')
    expect(getSeverityType('HIGH')).toBe('danger')
    expect(getSeverityType('CRITICAL')).toBe('danger')
  })

  it('大小写不敏感', () => {
    expect(getSeverityText('critical')).toBe('严重')
    expect(getSeverityType('high')).toBe('danger')
  })
})

describe('severity 映射 - 边界值', () => {
  it('空值不报错并返回占位符', () => {
    expect(getSeverityText(null)).toBe('-')
    expect(getSeverityText(undefined)).toBe('-')
    expect(getSeverityText('')).toBe('-')
    expect(getSeverityType(null)).toBe('info')
    expect(getSeverityType(undefined)).toBe('info')
  })

  it('无法识别的值安全回退', () => {
    expect(getSeverityText(99)).toBe('99')
    expect(getSeverityType('UNKNOWN')).toBe('info')
  })
})
