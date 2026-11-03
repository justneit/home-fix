import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it, vi } from 'vitest'
import { messages as m } from '@/lib/i18n'
import { LoginForm } from './LoginForm'

describe('LoginForm', () => {
  it('shows validation errors and does not submit when empty', async () => {
    const onSubmit = vi.fn()
    render(<LoginForm onSubmit={onSubmit} />)

    await userEvent.click(screen.getByRole('button', { name: m.auth.login.submit }))

    expect(await screen.findAllByText(m.validation.required)).toHaveLength(2)
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits trimmed credentials', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<LoginForm onSubmit={onSubmit} />)

    await userEvent.type(screen.getByLabelText(m.auth.email), '  an@example.com ')
    await userEvent.type(screen.getByLabelText(m.auth.password), 'Matkhau123')
    await userEvent.click(screen.getByRole('button', { name: m.auth.login.submit }))

    expect(onSubmit).toHaveBeenCalledWith({ email: 'an@example.com', password: 'Matkhau123' })
  })

  it('shows the generic invalid-credentials message from the server', async () => {
    const config = { headers: new AxiosHeaders() }
    const onSubmit = vi.fn().mockRejectedValue(
      new AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, null, {
        status: 401,
        statusText: 'Unauthorized',
        headers: {},
        config,
        data: { status: 401, code: 'INVALID_CREDENTIALS' },
      }),
    )
    render(<LoginForm onSubmit={onSubmit} />)

    await userEvent.type(screen.getByLabelText(m.auth.email), 'an@example.com')
    await userEvent.type(screen.getByLabelText(m.auth.password), 'sai-mat-khau')
    await userEvent.click(screen.getByRole('button', { name: m.auth.login.submit }))

    expect(await screen.findByText(m.errors.INVALID_CREDENTIALS)).toBeInTheDocument()
  })
})
