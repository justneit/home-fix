import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'
import { refreshSession } from '@/lib/api-client'
import { useAuthStore } from '@/stores/auth-store'
import type { Role } from '@/types/api'
import * as authApi from './api'
import type { LoginInput, RegisterPayload } from './schemas'

export const ROLE_HOME: Record<Role, string> = {
  CUSTOMER: '/customer',
  WORKER: '/worker',
  ADMIN: '/admin',
}

/** Restores the session from the refresh cookie once, when the app starts. */
export function useAuthBootstrap() {
  const status = useAuthStore((s) => s.status)
  useEffect(() => {
    if (status === 'unknown') {
      refreshSession().catch(() => useAuthStore.getState().clearSession())
    }
  }, [status])
}

export function useLogin() {
  const setSession = useAuthStore((s) => s.setSession)
  return useMutation({
    mutationFn: (input: LoginInput) => authApi.login(input),
    onSuccess: (data) => setSession(data.accessToken, data.user),
  })
}

export function useRegister() {
  const setSession = useAuthStore((s) => s.setSession)
  return useMutation({
    mutationFn: (payload: RegisterPayload) => authApi.register(payload),
    onSuccess: (data) => setSession(data.accessToken, data.user),
  })
}

export function useLogout() {
  const queryClient = useQueryClient()
  const clearSession = useAuthStore((s) => s.clearSession)
  return useMutation({
    mutationFn: authApi.logout,
    // Clear local state even if the server call fails; the access token expires on its own.
    onSettled: () => {
      clearSession()
      queryClient.clear()
    },
  })
}

export function useCurrentUser() {
  const userId = useAuthStore((s) => s.user?.id)
  return useQuery({
    queryKey: ['users', 'me', userId],
    queryFn: authApi.fetchCurrentUser,
    enabled: userId !== undefined,
  })
}
