import { AlertTriangle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { messages as m } from '@/lib/i18n'

interface ErrorStateProps {
  message: string
  onRetry?: () => void
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div
      role="alert"
      className="flex flex-col items-center gap-3 rounded-lg border border-dashed p-8 text-center"
    >
      <AlertTriangle className="size-8 text-destructive" aria-hidden />
      <p className="text-sm text-muted-foreground">{message}</p>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry}>
          {m.common.retry}
        </Button>
      )}
    </div>
  )
}
