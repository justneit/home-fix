import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import { Button } from '@/components/ui/button'
import { messages as m } from '@/lib/i18n'

/** Route-level error boundary and 404 page. */
export function RouteError() {
  const error = useRouteError()
  const notFound = isRouteErrorResponse(error) && error.status === 404
  if (!notFound) {
    console.error(error)
  }
  return <ErrorPage notFound={notFound} />
}

export function NotFoundPage() {
  return <ErrorPage notFound />
}

function ErrorPage({ notFound }: { notFound: boolean }) {
  return (
    <main className="flex min-h-svh flex-col items-center justify-center gap-4 p-6 text-center">
      <p className="text-5xl font-bold text-primary">{notFound ? '404' : ':('}</p>
      <h1 className="text-xl font-semibold">
        {notFound ? m.routeError.notFoundTitle : m.routeError.title}
      </h1>
      <p className="max-w-md text-sm text-muted-foreground">
        {notFound ? m.routeError.notFoundDescription : m.routeError.description}
      </p>
      <Button asChild>
        <Link to="/">{m.common.backHome}</Link>
      </Button>
    </main>
  )
}
