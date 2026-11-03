import { CircleDashed } from 'lucide-react'
import { ErrorState } from '@/components/common/ErrorState'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useCurrentUser } from '@/features/auth/hooks'
import { getErrorMessage } from '@/lib/api-error'
import { format, messages as m } from '@/lib/i18n'

interface PortalHomeProps {
  subtitle: string
  /** Features scheduled for later phases; rendered explicitly as "coming soon". */
  planned: readonly string[]
}

/** Landing page shared by the three portals until their real features are implemented. */
export function PortalHome({ subtitle, planned }: PortalHomeProps) {
  const { data: user, isPending, isError, error, refetch } = useCurrentUser()

  return (
    <div className="mx-auto max-w-6xl space-y-6 px-4 py-8">
      {isPending ? (
        <Skeleton className="h-16 w-full max-w-md" />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : (
        <header className="space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="text-2xl font-semibold">
              {format(m.portal.greeting, { name: user.fullName })}
            </h1>
            <Badge variant="secondary">{m.roles[user.role]}</Badge>
          </div>
          <p className="text-muted-foreground">{subtitle}</p>
        </header>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>{m.portal.plannedTitle}</CardTitle>
            <CardDescription>{m.portal.plannedDescription}</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-3">
              {planned.map((item) => (
                <li key={item} className="flex items-center justify-between gap-3 text-sm">
                  <span className="flex items-center gap-2">
                    <CircleDashed className="size-4 shrink-0 text-muted-foreground" aria-hidden />
                    {item}
                  </span>
                  <Badge variant="outline">{m.common.comingSoon}</Badge>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>

        {user && (
          <Card>
            <CardHeader>
              <CardTitle>{m.portal.accountTitle}</CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="space-y-2 text-sm">
                <div>
                  <dt className="text-muted-foreground">{m.auth.email}</dt>
                  <dd className="break-all">{user.email}</dd>
                </div>
                {user.phone && (
                  <div>
                    <dt className="text-muted-foreground">{m.auth.phone}</dt>
                    <dd>{user.phone}</dd>
                  </div>
                )}
              </dl>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
