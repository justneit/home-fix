import { isAxiosError } from 'axios'
import { messages } from '@/lib/i18n'
import type { FieldViolation, ProblemDetail } from '@/types/api'

type ErrorCode = keyof typeof messages.errors

export function getProblem(error: unknown): ProblemDetail | null {
  if (isAxiosError(error) && error.response?.data && typeof error.response.data === 'object') {
    const data = error.response.data as Partial<ProblemDetail>
    if (typeof data.code === 'string') {
      return data as ProblemDetail
    }
  }
  return null
}

/** Localized, user-safe message for any error thrown by the API client. */
export function getErrorMessage(error: unknown): string {
  const problem = getProblem(error)
  if (problem && problem.code in messages.errors) {
    return messages.errors[problem.code as ErrorCode]
  }
  if (isAxiosError(error) && !error.response) {
    return messages.errors.NETWORK
  }
  return messages.errors.UNKNOWN
}

export function getFieldViolations(error: unknown): FieldViolation[] {
  return getProblem(error)?.errors ?? []
}
