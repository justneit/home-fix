import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { Toaster } from '@/components/ui/sonner'
import { useAuthBootstrap } from '@/features/auth/hooks'
import { queryClient } from './query-client'
import { router } from './router'

export function App() {
  useAuthBootstrap()
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-center" richColors />
    </QueryClientProvider>
  )
}
