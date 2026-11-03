import { create } from 'zustand'
import type { User } from '@/types/api'

export type AuthStatus = 'unknown' | 'authenticated' | 'anonymous'

interface AuthState {
  status: AuthStatus
  /** Short-lived access token. Kept in memory only, never persisted. */
  accessToken: string | null
  user: User | null
  setSession: (accessToken: string, user: User) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()((set) => ({
  status: 'unknown',
  accessToken: null,
  user: null,
  setSession: (accessToken, user) => set({ status: 'authenticated', accessToken, user }),
  clearSession: () => set({ status: 'anonymous', accessToken: null, user: null }),
}))
