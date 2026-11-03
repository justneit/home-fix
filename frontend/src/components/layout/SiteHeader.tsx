import { Link } from 'react-router'
import { Logo } from '@/components/common/Logo'
import { Button } from '@/components/ui/button'
import { ROLE_HOME } from '@/features/auth/hooks'
import { messages as m } from '@/lib/i18n'
import { useAuthStore } from '@/stores/auth-store'
import { UserMenu } from './UserMenu'

export function SiteHeader() {
  const { status, user } = useAuthStore()

  return (
    <header className="sticky top-0 z-40 border-b bg-background/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4">
        <Logo to={user ? ROLE_HOME[user.role] : '/'} />
        <nav className="flex items-center gap-2">
          {status === 'authenticated' && user ? (
            <UserMenu user={user} />
          ) : (
            status === 'anonymous' && (
              <>
                <Button variant="ghost" asChild>
                  <Link to="/login">{m.nav.login}</Link>
                </Button>
                <Button asChild>
                  <Link to="/register">{m.nav.register}</Link>
                </Button>
              </>
            )
          )}
        </nav>
      </div>
    </header>
  )
}
