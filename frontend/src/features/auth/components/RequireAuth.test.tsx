import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { useAuthStore } from '@/stores/auth-store'
import type { Role, User } from '@/types/api'
import { RequireAuth } from './RequireAuth'

function renderAt(path: string) {
  const router = createMemoryRouter(
    [
      { path: '/login', element: <p>login page</p> },
      { path: '/customer', element: <p>customer home</p> },
      {
        path: '/admin',
        element: (
          <RequireAuth roles={['ADMIN']}>
            <p>admin area</p>
          </RequireAuth>
        ),
      },
    ],
    { initialEntries: [path] },
  )
  render(<RouterProvider router={router} />)
}

function signInAs(role: Role) {
  const user: User = {
    id: 1,
    email: 'u@example.com',
    fullName: 'U',
    phone: null,
    role,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
  }
  useAuthStore.getState().setSession('token', user)
}

describe('RequireAuth', () => {
  beforeEach(() => useAuthStore.getState().clearSession())

  it('redirects anonymous users to the login page', async () => {
    renderAt('/admin')
    expect(await screen.findByText('login page')).toBeInTheDocument()
  })

  it('redirects users with the wrong role to their own portal', async () => {
    signInAs('CUSTOMER')
    renderAt('/admin')
    expect(await screen.findByText('customer home')).toBeInTheDocument()
  })

  it('renders the protected content for the permitted role', async () => {
    signInAs('ADMIN')
    renderAt('/admin')
    expect(await screen.findByText('admin area')).toBeInTheDocument()
  })

  it('shows a loading state while the session is being restored', () => {
    useAuthStore.setState({ status: 'unknown', user: null, accessToken: null })
    renderAt('/admin')
    expect(screen.getByRole('status')).toBeInTheDocument()
  })
})
