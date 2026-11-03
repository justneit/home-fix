import { Inbox } from 'lucide-react'

export function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-lg border border-dashed p-8 text-center">
      <Inbox className="size-8 text-muted-foreground" aria-hidden />
      <p className="text-sm text-muted-foreground">{message}</p>
    </div>
  )
}
