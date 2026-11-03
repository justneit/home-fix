import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { getErrorMessage } from '@/lib/api-error'
import { messages as m } from '@/lib/i18n'
import { type LoginInput, loginSchema } from '../schemas'

interface LoginFormProps {
  onSubmit: (values: LoginInput) => Promise<unknown>
}

export function LoginForm({ onSubmit }: LoginFormProps) {
  const form = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })
  const { errors, isSubmitting } = form.formState

  const submit = form.handleSubmit(async (values) => {
    try {
      await onSubmit(values)
    } catch (error) {
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
        <Field data-invalid={!!errors.email}>
          <FieldLabel htmlFor="login-email">{m.auth.email}</FieldLabel>
          <Input
            id="login-email"
            type="email"
            autoComplete="email"
            aria-invalid={!!errors.email}
            {...form.register('email')}
          />
          <FieldError errors={[errors.email]} />
        </Field>
        <Field data-invalid={!!errors.password}>
          <FieldLabel htmlFor="login-password">{m.auth.password}</FieldLabel>
          <Input
            id="login-password"
            type="password"
            autoComplete="current-password"
            aria-invalid={!!errors.password}
            {...form.register('password')}
          />
          <FieldError errors={[errors.password]} />
        </Field>
        <Button type="submit" disabled={isSubmitting} className="w-full">
          {isSubmitting && <Loader2 className="animate-spin" aria-hidden />}
          {m.auth.login.submit}
        </Button>
      </FieldGroup>
    </form>
  )
}
