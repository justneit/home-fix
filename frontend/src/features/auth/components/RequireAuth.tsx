import type { ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { FullPageSpinner } from '@/components/common/FullPageSpinner'
import { useAuthStore } from '@/stores/auth-store'
import type { Role } from '@/types/api'
import { ROLE_HOME } from '../hooks'

interface RequireAuthProps {
  roles?: Role[]
  children?: ReactNode
}

/** Route guard: waits for session restore, then redirects anonymous users or wrong roles. */
export function RequireAuth({ roles, children }: RequireAuthProps) {
  const { status, user } = useAuthStore()
  const location = useLocation()

  if (status === 'unknown') {
    return <FullPageSpinner />
  }
  if (status === 'anonymous' || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={ROLE_HOME[user.role]} replace />
  }
  return children ?? <Outlet />
}

/** For login/register: signed-in users are sent to their portal instead. */
export function RedirectIfAuthenticated({ children }: { children?: ReactNode }) {
  const { status, user } = useAuthStore()
  if (status === 'unknown') {
    return <FullPageSpinner />
  }
  if (status === 'authenticated' && user) {
    return <Navigate to={ROLE_HOME[user.role]} replace />
  }
  return children ?? <Outlet />
}
