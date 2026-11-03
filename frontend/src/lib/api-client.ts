import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth-store'
import type { User } from '@/types/api'

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: User
}

/**
 * Shared HTTP client. The API is served from the same origin (Vite proxy in development,
 * nginx in containers), so the refresh-token and XSRF cookies are first-party.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  timeout: 15_000,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/logout', '/auth/csrf']

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const config = error.config as RetriableConfig | undefined
  const isAuthCall = AUTH_PATHS.some((p) => config?.url?.startsWith(p))
  if (error.response?.status !== 401 || !config || config._retried || isAuthCall) {
    throw error
  }
  config._retried = true
  try {
    await refreshSession()
  } catch {
    useAuthStore.getState().clearSession()
    throw error
  }
  return api(config)
})

/** Refresh and logout are CSRF-protected; make sure the XSRF-TOKEN cookie exists first. */
export async function ensureCsrfCookie() {
  if (!document.cookie.split('; ').some((c) => c.startsWith('XSRF-TOKEN='))) {
    await api.get('/auth/csrf')
  }
}

let refreshInFlight: Promise<AuthResponse> | null = null

/**
 * Exchanges the HttpOnly refresh cookie for a new access token. Concurrent callers share one
 * request, because the backend rotates the token and treats reuse of an old one as theft.
 */
export function refreshSession(): Promise<AuthResponse> {
  refreshInFlight ??= (async () => {
    try {
      await ensureCsrfCookie()
      const { data } = await api.post<AuthResponse>('/auth/refresh')
      useAuthStore.getState().setSession(data.accessToken, data.user)
      return data
    } finally {
      refreshInFlight = null
    }
  })()
  return refreshInFlight
}
