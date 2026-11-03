import { vi } from '@/locales/vi'

/** Active UI dictionary. Swap for a locale-aware provider when more languages are added. */
export const messages = vi

/** Replaces `{name}` placeholders: `format('Xin chào, {name}', { name: 'An' })`. */
export function format(template: string, values: Record<string, string | number>): string {
  return template.replace(/\{(\w+)\}/g, (match, key: string) =>
    key in values ? String(values[key]) : match,
  )
}
