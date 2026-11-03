import { QueryClient } from '@tanstack/react-query'
import { isAxiosError } from 'axios'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Retry transient failures only; 4xx responses will not succeed on retry.
      retry: (failureCount, error) => {
        const status = isAxiosError(error) ? error.response?.status : undefined
        return failureCount < 2 && (status === undefined || status >= 500)
      },
    },
  },
})
