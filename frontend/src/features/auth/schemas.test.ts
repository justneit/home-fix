import { describe, expect, it } from 'vitest'
import { registerSchema, toRegisterPayload } from './schemas'

const valid = {
  accountType: 'CUSTOMER' as const,
  fullName: 'Nguyễn Văn An',
  email: 'an@example.com',
  phone: '',
  password: 'Matkhau123',
  confirmPassword: 'Matkhau123',
}

function issuesFor(input: unknown) {
  const result = registerSchema.safeParse(input)
  return result.success ? [] : result.error.issues.map((i) => i.path.join('.'))
}

describe('registerSchema', () => {
  it('accepts a valid customer registration', () => {
    expect(issuesFor(valid)).toEqual([])
  })

  it('requires letters and digits in the password', () => {
    expect(issuesFor({ ...valid, password: 'chiconchu', confirmPassword: 'chiconchu' })).toContain(
      'password',
    )
    expect(issuesFor({ ...valid, password: '12345678', confirmPassword: '12345678' })).toContain(
      'password',
    )
  })

  it('accepts Vietnamese letters in the password', () => {
    expect(issuesFor({ ...valid, password: 'mậtkhẩu99', confirmPassword: 'mậtkhẩu99' })).toEqual([])
  })

  it('reports mismatched confirmation on confirmPassword', () => {
    expect(issuesFor({ ...valid, confirmPassword: 'Khac12345' })).toEqual(['confirmPassword'])
  })

  it('validates Vietnamese phone numbers only when provided', () => {
    expect(issuesFor({ ...valid, phone: '0912345678' })).toEqual([])
    expect(issuesFor({ ...valid, phone: '+84912345678' })).toEqual([])
    expect(issuesFor({ ...valid, phone: '12345' })).toContain('phone')
  })

  it('cannot register as admin', () => {
    expect(issuesFor({ ...valid, accountType: 'ADMIN' })).toContain('accountType')
  })
})

describe('toRegisterPayload', () => {
  it('drops the confirmation and sends an empty phone as null', () => {
    const payload = toRegisterPayload(registerSchema.parse({ ...valid, email: ' An@example.com ' }))
    expect(payload).toEqual({
      accountType: 'CUSTOMER',
      fullName: 'Nguyễn Văn An',
      email: 'An@example.com',
      phone: null,
      password: 'Matkhau123',
    })
  })
})
