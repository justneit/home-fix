import { Link } from 'react-router'
import { Button } from '@/components/ui/button'
import { ServiceCategoryGrid } from '@/features/service-catalog/components/ServiceCategoryGrid'
import { messages as m } from '@/lib/i18n'

export function HomePage() {
  return (
    <div className="mx-auto max-w-6xl space-y-14 px-4 py-10 md:py-16">
      <section className="max-w-2xl space-y-5">
        <h1 className="text-3xl font-bold tracking-tight text-balance md:text-5xl">
          {m.landing.heroTitle}
        </h1>
        <p className="text-lg text-muted-foreground">{m.landing.heroSubtitle}</p>
        <div className="flex flex-col gap-3 sm:flex-row">
          <Button size="lg" asChild>
            <Link to="/register?type=CUSTOMER">{m.landing.ctaCustomer}</Link>
          </Button>
          <Button size="lg" variant="outline" asChild>
            <Link to="/register?type=WORKER">{m.landing.ctaWorker}</Link>
          </Button>
        </div>
      </section>

      <section aria-labelledby="categories-title" className="space-y-5">
        <h2 id="categories-title" className="text-2xl font-semibold">
          {m.landing.categoriesTitle}
        </h2>
        <ServiceCategoryGrid />
      </section>

      <section aria-labelledby="how-title" className="space-y-5">
        <h2 id="how-title" className="text-2xl font-semibold">
          {m.landing.howItWorksTitle}
        </h2>
        <ol className="grid gap-4 md:grid-cols-3">
          {m.landing.steps.map((step, index) => (
            <li key={step.title} className="rounded-xl bg-muted/50 p-5">
              <span className="text-sm font-semibold text-primary">0{index + 1}</span>
              <h3 className="mt-1 font-medium">{step.title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{step.body}</p>
            </li>
          ))}
        </ol>
      </section>
    </div>
  )
}
