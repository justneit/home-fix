import { PortalHome } from '@/components/layout/PortalHome'
import { messages as m } from '@/lib/i18n'

export function CustomerDashboardPage() {
  return <PortalHome subtitle={m.portal.customer.subtitle} planned={m.portal.customer.planned} />
}
