import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it } from 'vitest'
import { getErrorMessage, getFieldViolations } from './api-error'
import { messages } from './i18n'

function httpError(status: number, data: unknown) {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('failed', 'ERR_BAD_REQUEST', config, null, {
    status,
    statusText: '',
    headers: {},
    config,
    data,
  })
}

describe('getErrorMessage', () => {
  it('maps backend error codes to localized messages', () => {
    const error = httpError(401, { status: 401, code: 'INVALID_CREDENTIALS' })
    expect(getErrorMessage(error)).toBe(messages.errors.INVALID_CREDENTIALS)
  })

  it('falls back for unknown codes and non-problem bodies', () => {
    expect(getErrorMessage(httpError(418, { status: 418, code: 'TEAPOT' }))).toBe(
      messages.errors.UNKNOWN,
    )
    expect(getErrorMessage(httpError(502, '<html>Bad gateway</html>'))).toBe(
      messages.errors.UNKNOWN,
    )
  })

  it('reports network failures distinctly', () => {
    expect(getErrorMessage(new AxiosError('Network Error', 'ERR_NETWORK'))).toBe(
      messages.errors.NETWORK,
    )
  })

  it('exposes field violations', () => {
    const error = httpError(400, {
      status: 400,
      code: 'VALIDATION_FAILED',
      errors: [{ field: 'email', code: 'Email', message: 'bad' }],
    })
    expect(getFieldViolations(error)).toEqual([{ field: 'email', code: 'Email', message: 'bad' }])
  })
})
