import { zodResolver } from '@hookform/resolvers/zod'
import { HardHat, Home, Loader2 } from 'lucide-react'
import { useForm, useWatch } from 'react-hook-form'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSet,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { getErrorMessage, getFieldViolations } from '@/lib/api-error'
import { messages as m } from '@/lib/i18n'
import { cn } from '@/lib/utils'
import { type RegisterInput, registerSchema } from '../schemas'

interface RegisterFormProps {
  defaultAccountType?: RegisterInput['accountType']
  onSubmit: (values: RegisterInput) => Promise<unknown>
}

const FIELD_NAMES = ['fullName', 'email', 'phone', 'password'] as const

const ACCOUNT_TYPES = [
  {
    value: 'CUSTOMER',
    label: m.auth.register.customer,
    hint: m.auth.register.customerHint,
    icon: Home,
  },
  {
    value: 'WORKER',
    label: m.auth.register.worker,
    hint: m.auth.register.workerHint,
    icon: HardHat,
  },
] as const

export function RegisterForm({ defaultAccountType = 'CUSTOMER', onSubmit }: RegisterFormProps) {
  const form = useForm<RegisterInput>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      accountType: defaultAccountType,
      fullName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
  })
  const { errors, isSubmitting } = form.formState
  const accountType = useWatch({ control: form.control, name: 'accountType' })

  const submit = form.handleSubmit(async (values) => {
    try {
      await onSubmit(values)
    } catch (error) {
      // Surface server-side field errors next to the matching inputs.
      for (const violation of getFieldViolations(error)) {
        const field = FIELD_NAMES.find((name) => name === violation.field)
        if (field) {
          form.setError(field, { message: m.validation.invalid })
        }
      }
      form.setError('root', { message: getErrorMessage(error) })
    }
  })

  return (
    <form onSubmit={submit} noValidate>
      <FieldGroup>
        {errors.root?.message && (
          <Alert variant="destructive">
            <AlertDescription>{errors.root.message}</AlertDescription>
          </Alert>
        )}

        <FieldSet>
          <FieldLegend variant="label">{m.auth.register.accountTypeLabel}</FieldLegend>
          <div role="radiogroup" className="grid gap-3 sm:grid-cols-2">
            {ACCOUNT_TYPES.map(({ value, label, hint, icon: Icon }) => (
              <label
                key={value}
                className={cn(
                  'flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-colors',
                  'has-focus-visible:ring-[3px] has-focus-visible:ring-ring/50',
                  accountType === value ? 'border-primary bg-primary/5' : 'hover:bg-muted',
                )}
              >
                <input
                  type="radio"
                  value={value}
                  className="sr-only"
                  {...form.register('accountType')}
                />
                <Icon className="mt-0.5 size-5 shrink-0 text-primary" aria-hidden />
                <span className="space-y-0.5">
                  <span className="block text-sm font-medium">{label}</span>
                  <span className="block text-xs text-muted-foreground">{hint}</span>
                </span>
              </label>
            ))}
          </div>
        </FieldSet>

        <Field data-invalid={!!errors.fullName}>
          <FieldLabel htmlFor="register-fullName">{m.auth.fullName}</FieldLabel>
          <Input
            id="register-fullName"
            autoComplete="name"
            aria-invalid={!!errors.fullName}
            {...form.register('fullName')}
          />
          <FieldError errors={[errors.fullName]} />
        </Field>

        <Field data-invalid={!!errors.email}>
          <FieldLabel htmlFor="register-email">{m.auth.email}</FieldLabel>
          <Input
            id="register-email"
            type="email"
            autoComplete="email"
            aria-invalid={!!errors.email}
            {...form.register('email')}
          />
          <FieldError errors={[errors.email]} />
        </Field>

        <Field data-invalid={!!errors.phone}>
          <FieldLabel htmlFor="register-phone">
            {m.auth.phone}{' '}
            <span className="font-normal text-muted-foreground">({m.common.optional})</span>
          </FieldLabel>
          <Input
            id="register-phone"
            type="tel"
            autoComplete="tel"
            inputMode="tel"
            aria-invalid={!!errors.phone}
            {...form.register('phone')}
          />
          <FieldError errors={[errors.phone]} />
        </Field>

        <Field data-invalid={!!errors.password}>
          <FieldLabel htmlFor="register-password">{m.auth.password}</FieldLabel>
          <Input
            id="register-password"
            type="password"
            autoComplete="new-password"
            aria-invalid={!!errors.password}
            aria-describedby="register-password-hint"
            {...form.register('password')}
          />
          <FieldDescription id="register-password-hint">
            {m.auth.register.passwordHint}
          </FieldDescription>
          <FieldError errors={[errors.password]} />
        </Field>

        <Field data-invalid={!!errors.confirmPassword}>
          <FieldLabel htmlFor="register-confirmPassword">{m.auth.confirmPassword}</FieldLabel>
          <Input
            id="register-confirmPassword"
            type="password"
            autoComplete="new-password"
            aria-invalid={!!errors.confirmPassword}
            {...form.register('confirmPassword')}
          />
          <FieldError errors={[errors.confirmPassword]} />
        </Field>

        <Button type="submit" disabled={isSubmitting} className="w-full">
          {isSubmitting && <Loader2 className="animate-spin" aria-hidden />}
          {m.auth.register.submit}
        </Button>
      </FieldGroup>
    </form>
  )
}
