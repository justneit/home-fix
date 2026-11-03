import { expect, test } from '@playwright/test'

const DEMO_PASSWORD = 'HomeFix@123'

test('home page lists service categories from the backend', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Dịch vụ sửa chữa' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Sửa ống nước' })).toBeVisible()
})

test('customer can register and lands in the customer portal', async ({ page }) => {
  const email = `e2e-${Date.now()}-${Math.random().toString(36).slice(2, 8)}@example.com`

  await page.goto('/register')
  await page.getByLabel('Họ và tên').fill('Khách Hàng E2E')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Mật khẩu', { exact: true }).fill('Matkhau123')
  await page.getByLabel('Xác nhận mật khẩu').fill('Matkhau123')
  await page.getByRole('button', { name: 'Đăng ký' }).click()

  await expect(page).toHaveURL(/\/customer$/)
  await expect(page.getByRole('heading', { name: 'Xin chào, Khách Hàng E2E' })).toBeVisible()
})

test('worker registration lands in the worker portal', async ({ page }) => {
  const email = `e2e-worker-${Date.now()}-${Math.random().toString(36).slice(2, 8)}@example.com`

  await page.goto('/register?type=WORKER')
  await page.getByLabel('Họ và tên').fill('Thợ E2E')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Mật khẩu', { exact: true }).fill('Matkhau123')
  await page.getByLabel('Xác nhận mật khẩu').fill('Matkhau123')
  await page.getByRole('button', { name: 'Đăng ký' }).click()

  await expect(page).toHaveURL(/\/worker$/)
  await expect(page.getByText('Thợ sửa chữa', { exact: true })).toBeVisible()
})

test('wrong password shows a generic error', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('Email').fill('khachhang@homefix.test')
  await page.getByLabel('Mật khẩu').fill('sai-mat-khau-1')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng.')).toBeVisible()
})

test('session survives a reload and logout ends it', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('Email').fill('thosua@homefix.test')
  await page.getByLabel('Mật khẩu').fill(DEMO_PASSWORD)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL(/\/worker$/)

  // Access token lives in memory only; the refresh cookie restores the session.
  await page.reload()
  await expect(page.getByRole('heading', { name: /Xin chào, Trần Minh Thợ/ })).toBeVisible()

  await page.getByRole('button', { name: 'Mở menu tài khoản' }).click()
  await page.getByRole('menuitem', { name: 'Đăng xuất' }).click()
  await expect(page).toHaveURL(/\/$/)

  await page.goto('/worker')
  await expect(page).toHaveURL(/\/login$/)
})

test('a customer cannot open the admin portal', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('Email').fill('khachhang@homefix.test')
  await page.getByLabel('Mật khẩu').fill(DEMO_PASSWORD)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL(/\/customer$/)

  await page.goto('/admin')
  await expect(page).toHaveURL(/\/customer$/)
})
