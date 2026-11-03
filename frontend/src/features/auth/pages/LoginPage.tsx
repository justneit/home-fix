import { Link, useLocation, useNavigate } from 'react-router'
import { toast } from 'sonner'
import { messages as m } from '@/lib/i18n'
import { LoginForm } from '../components/LoginForm'
import { ROLE_HOME, useLogin } from '../hooks'
import { AuthCard } from './AuthCard'

export function LoginPage() {
  const login = useLogin()
  const navigate = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from

  return (
    <AuthCard
      title={m.auth.login.title}
      description={m.auth.login.description}
      footer={
        <>
          {m.auth.login.noAccount}{' '}
          <Link
            to="/register"
            className="font-medium text-primary underline-offset-4 hover:underline"
          >
            {m.nav.register}
          </Link>
        </>
      }
    >
      <LoginForm
        onSubmit={async (values) => {
          const { user } = await login.mutateAsync(values)
          toast.success(m.auth.login.success)
          // RequireAuth redirects again if `from` belongs to another role.
          navigate(from ?? ROLE_HOME[user.role], { replace: true })
        }}
      />
    </AuthCard>
  )
}
