import { LogOut, UserRound } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useLogout } from '@/features/auth/hooks'
import { messages as m } from '@/lib/i18n'
import type { User } from '@/types/api'

export function UserMenu({ user }: { user: User }) {
  const logout = useLogout()
  const navigate = useNavigate()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="gap-2" aria-label={m.nav.openMenu}>
          <UserRound className="size-4" aria-hidden />
          <span className="hidden max-w-40 truncate sm:inline">{user.fullName}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel className="space-y-0.5">
          <span className="block truncate font-medium">{user.fullName}</span>
          <span className="block truncate text-xs font-normal text-muted-foreground">
            {user.email}
          </span>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onSelect={() => {
            // Leave the protected page first; otherwise the route guard redirects to /login.
            navigate('/')
            logout.mutate()
          }}
        >
          <LogOut aria-hidden />
          {m.nav.logout}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
