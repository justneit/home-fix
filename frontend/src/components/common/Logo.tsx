import { Wrench } from 'lucide-react'
import { Link } from 'react-router'
import { messages as m } from '@/lib/i18n'

export function Logo({ to = '/' }: { to?: string }) {
  return (
    <Link to={to} className="flex items-center gap-2 font-semibold tracking-tight">
      <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
        <Wrench className="size-4" aria-hidden />
      </span>
      <span className="text-lg">{m.app.name}</span>
    </Link>
  )
}
