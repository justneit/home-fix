import {
  BrickWall,
  Droplets,
  Hammer,
  KeyRound,
  type LucideIcon,
  PaintRoller,
  Snowflake,
  WashingMachine,
  Wrench,
  Zap,
} from 'lucide-react'
import { EmptyState } from '@/components/common/EmptyState'
import { ErrorState } from '@/components/common/ErrorState'
import { Skeleton } from '@/components/ui/skeleton'
import { getErrorMessage } from '@/lib/api-error'
import { messages as m } from '@/lib/i18n'
import { useServiceCategories } from '../api'

// Icon names are stored with each category in the database.
const ICONS: Record<string, LucideIcon> = {
  droplets: Droplets,
  zap: Zap,
  snowflake: Snowflake,
  'washing-machine': WashingMachine,
  hammer: Hammer,
  'paint-roller': PaintRoller,
  'key-round': KeyRound,
  'brick-wall': BrickWall,
}

export function ServiceCategoryGrid() {
  const { data, isPending, isError, error, refetch } = useServiceCategories()

  if (isPending) {
    return (
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4" aria-busy="true">
        {Array.from({ length: 8 }, (_, i) => (
          <Skeleton key={i} className="h-28 rounded-xl" />
        ))}
      </div>
    )
  }
  if (isError) {
    return (
      <ErrorState
        message={`${m.landing.categoriesError} ${getErrorMessage(error)}`}
        onRetry={() => void refetch()}
      />
    )
  }
  if (data.length === 0) {
    return <EmptyState message={m.landing.categoriesEmpty} />
  }

  return (
    <ul className="grid grid-cols-2 gap-3 md:grid-cols-4">
      {data.map((category) => {
        const Icon = (category.icon && ICONS[category.icon]) || Wrench
        return (
          <li
            key={category.id}
            className="flex flex-col gap-2 rounded-xl border bg-card p-4 transition-shadow hover:shadow-sm"
          >
            <span className="flex size-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Icon className="size-5" aria-hidden />
            </span>
            <h3 className="font-medium">{category.name}</h3>
            {category.description && (
              <p className="line-clamp-2 text-xs text-muted-foreground">{category.description}</p>
            )}
          </li>
        )
      })}
    </ul>
  )
}
