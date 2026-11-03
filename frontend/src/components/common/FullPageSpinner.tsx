import { Loader2 } from 'lucide-react'
import { messages as m } from '@/lib/i18n'

export function FullPageSpinner() {
  return (
    <div className="flex min-h-svh items-center justify-center" role="status" aria-live="polite">
      <Loader2 className="size-8 animate-spin text-primary" aria-hidden />
      <span className="sr-only">{m.common.loading}</span>
    </div>
  )
}
