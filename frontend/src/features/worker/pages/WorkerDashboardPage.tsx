import { PortalHome } from '@/components/layout/PortalHome'
import { messages as m } from '@/lib/i18n'

export function WorkerDashboardPage() {
  return <PortalHome subtitle={m.portal.worker.subtitle} planned={m.portal.worker.planned} />
}
