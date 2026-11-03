import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api-client'

export interface ServiceCategory {
  id: number
  code: string
  name: string
  description: string | null
  icon: string | null
}

export async function fetchServiceCategories() {
  const { data } = await api.get<ServiceCategory[]>('/services')
  return data
}

export function useServiceCategories() {
  return useQuery({
    queryKey: ['service-categories'],
    queryFn: fetchServiceCategories,
    staleTime: 5 * 60_000,
  })
}
