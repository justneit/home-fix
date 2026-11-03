import { z } from 'zod'
import { messages } from '@/lib/i18n'

const v = messages.validation

// Mirrors the backend constraints in RegisterRequest / LoginRequest.
export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, { error: v.required })
    .pipe(z.email({ error: v.email })),
  password: z.string().min(1, { error: v.required }).max(128, { error: v.passwordMax }),
})

export type LoginInput = z.infer<typeof loginSchema>

export const registerSchema = z
  .object({
    accountType: z.enum(['CUSTOMER', 'WORKER']),
    fullName: z.string().trim().min(1, { error: v.required }).max(100, { error: v.tooLong }),
    email: z
      .string()
      .trim()
      .min(1, { error: v.required })
      .max(254, { error: v.tooLong })
      .pipe(z.email({ error: v.email })),
    phone: z
      .string()
      .trim()
      .refine((value) => value === '' || /^(0|\+84)\d{9}$/.test(value), { error: v.phone }),
    password: z
      .string()
      .min(8, { error: v.passwordMin })
      .max(72, { error: v.passwordMax })
      .regex(/^(?=.*\p{L})(?=.*\d).+$/u, { error: v.passwordComplexity }),
    confirmPassword: z.string().min(1, { error: v.required }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    error: v.passwordMismatch,
    path: ['confirmPassword'],
  })

export type RegisterInput = z.infer<typeof registerSchema>

export type RegisterPayload = Omit<RegisterInput, 'confirmPassword' | 'phone'> & {
  phone: string | null
}

export function toRegisterPayload({
  confirmPassword: _,
  ...input
}: RegisterInput): RegisterPayload {
  return { ...input, phone: input.phone === '' ? null : input.phone }
}
