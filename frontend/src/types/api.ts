/** RFC 9457 problem detail as returned by every failing backend endpoint. */
export interface ProblemDetail {
  type?: string
  title?: string
  status: number
  detail?: string
  instance?: string
  code: string
  errors?: FieldViolation[]
}

export interface FieldViolation {
  field: string
  code: string
  message: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type Role = 'CUSTOMER' | 'WORKER' | 'ADMIN'
export type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'DISABLED'

export interface User {
  id: number
  email: string
  fullName: string
  phone: string | null
  role: Role
  status: AccountStatus
  createdAt: string
}
