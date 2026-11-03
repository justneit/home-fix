import type { ReactNode } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function AuthCard({
  title,
  description,
  children,
  footer,
}: {
  title: string
  description: string
  children: ReactNode
  footer: ReactNode
}) {
  return (
    <div className="mx-auto flex w-full max-w-md flex-col gap-4 px-4 py-10">
      <Card>
        <CardHeader>
          <CardTitle className="text-xl">
            <h1>{title}</h1>
          </CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
        <CardContent>{children}</CardContent>
      </Card>
      <p className="text-center text-sm text-muted-foreground">{footer}</p>
    </div>
  )
}
