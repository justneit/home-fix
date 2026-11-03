import { Link, useNavigate, useSearchParams } from 'react-router'
import { toast } from 'sonner'
import { messages as m } from '@/lib/i18n'
import { RegisterForm } from '../components/RegisterForm'
import { ROLE_HOME, useRegister } from '../hooks'
import { toRegisterPayload } from '../schemas'
import { AuthCard } from './AuthCard'

export function RegisterPage() {
  const registerAccount = useRegister()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const defaultAccountType = params.get('type') === 'WORKER' ? 'WORKER' : 'CUSTOMER'

  return (
    <AuthCard
      title={m.auth.register.title}
      description={m.auth.register.description}
      footer={
        <>
          {m.auth.register.hasAccount}{' '}
          <Link to="/login" className="font-medium text-primary underline-offset-4 hover:underline">
            {m.nav.login}
          </Link>
        </>
      }
    >
      <RegisterForm
        defaultAccountType={defaultAccountType}
        onSubmit={async (values) => {
          const { user } = await registerAccount.mutateAsync(toRegisterPayload(values))
          toast.success(m.auth.register.success)
          navigate(ROLE_HOME[user.role], { replace: true })
        }}
      />
    </AuthCard>
  )
}
