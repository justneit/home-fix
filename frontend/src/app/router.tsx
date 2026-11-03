import { createBrowserRouter } from 'react-router'
import { FullPageSpinner } from '@/components/common/FullPageSpinner'
import { NotFoundPage, RouteError } from '@/components/common/RouteError'
import { AppLayout } from '@/components/layout/AppLayout'
import { RedirectIfAuthenticated, RequireAuth } from '@/features/auth/components/RequireAuth'
import { HomePage } from '@/features/landing/HomePage'

// Everything except the public landing page is loaded on demand.
export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    errorElement: <RouteError />,
    hydrateFallbackElement: <FullPageSpinner />,
    children: [
      { index: true, element: <HomePage /> },
      {
        element: <RedirectIfAuthenticated />,
        children: [
          {
            path: 'login',
            lazy: async () => ({
              Component: (await import('@/features/auth/pages/LoginPage')).LoginPage,
            }),
          },
          {
            path: 'register',
            lazy: async () => ({
              Component: (await import('@/features/auth/pages/RegisterPage')).RegisterPage,
            }),
          },
        ],
      },
      {
        path: 'customer',
        element: <RequireAuth roles={['CUSTOMER']} />,
        children: [
          {
            index: true,
            lazy: async () => ({
              Component: (await import('@/features/customer/pages/CustomerDashboardPage'))
                .CustomerDashboardPage,
            }),
          },
        ],
      },
      {
        path: 'worker',
        element: <RequireAuth roles={['WORKER']} />,
        children: [
          {
            index: true,
            lazy: async () => ({
              Component: (await import('@/features/worker/pages/WorkerDashboardPage'))
                .WorkerDashboardPage,
            }),
          },
        ],
      },
      {
        path: 'admin',
        element: <RequireAuth roles={['ADMIN']} />,
        children: [
          {
            index: true,
            lazy: async () => ({
              Component: (await import('@/features/admin/pages/AdminDashboardPage'))
                .AdminDashboardPage,
            }),
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
