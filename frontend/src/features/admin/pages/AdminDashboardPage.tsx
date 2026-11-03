import { PortalHome } from '@/components/layout/PortalHome'
import { messages as m } from '@/lib/i18n'

export function AdminDashboardPage() {
  return <PortalHome subtitle={m.portal.admin.subtitle} planned={m.portal.admin.planned} />
}
