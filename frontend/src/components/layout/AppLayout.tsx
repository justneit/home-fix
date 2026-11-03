import { Outlet } from 'react-router'
import { SiteHeader } from './SiteHeader'

export function AppLayout() {
  return (
    <div className="flex min-h-svh flex-col">
      <SiteHeader />
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  )
}
