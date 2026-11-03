import { api, ensureCsrfCookie, type AuthResponse } from '@/lib/api-client'
import type { User } from '@/types/api'
import type { LoginInput, RegisterPayload } from './schemas'

export async function login(input: LoginInput) {
  const { data } = await api.post<AuthResponse>('/auth/login', input)
  return data
}

export async function register(payload: RegisterPayload) {
  const { data } = await api.post<AuthResponse>('/auth/register', payload)
  return data
}

export async function logout() {
  await ensureCsrfCookie()
  await api.post('/auth/logout')
}

export async function fetchCurrentUser() {
  const { data } = await api.get<User>('/users/me')
  return data
}
