import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { messages as m } from '@/lib/i18n'
import { RegisterForm } from './RegisterForm'

async function fillValidForm() {
  await userEvent.type(screen.getByLabelText(m.auth.fullName), 'Trần Thị Bình')
  await userEvent.type(screen.getByLabelText(m.auth.email), 'binh@example.com')
  await userEvent.type(screen.getByLabelText(m.auth.password), 'Matkhau123')
  await userEvent.type(screen.getByLabelText(m.auth.confirmPassword), 'Matkhau123')
}

describe('RegisterForm', () => {
  it('reports a password confirmation mismatch', async () => {
    const onSubmit = vi.fn()
    render(<RegisterForm onSubmit={onSubmit} />)

    await fillValidForm()
    await userEvent.clear(screen.getByLabelText(m.auth.confirmPassword))
    await userEvent.type(screen.getByLabelText(m.auth.confirmPassword), 'Khac12345')
    await userEvent.click(screen.getByRole('button', { name: m.auth.register.submit }))

    expect(await screen.findByText(m.validation.passwordMismatch)).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits a worker registration when the worker option is chosen', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<RegisterForm onSubmit={onSubmit} />)

    await userEvent.click(screen.getByLabelText(new RegExp(m.auth.register.worker)))
    await fillValidForm()
    await userEvent.click(screen.getByRole('button', { name: m.auth.register.submit }))

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ accountType: 'WORKER', email: 'binh@example.com', phone: '' }),
    )
  })
})
