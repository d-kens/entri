# Testing

Tests sit on a spectrum from fast and isolated to slow and realistic:

```
unit tests  →  E2E tests (real backend)
```

- **Unit** — one class, no browser, milliseconds
- **E2E** — full UI stack, real backend, real browser

---

## Unit tests

**What:** Test a single class — a component, service, pipe, or utility — in isolation. No browser, no HTTP, no Angular DI tree. Fast.

**How they work:** Dependencies are replaced with fakes or spies. The class is called directly and the output is asserted.

**File convention:** Co-located with the file they test, suffixed `.spec.ts`.
```
login.ts
login.spec.ts   ← tests Login in isolation
```

**Run:**
```bash
npm test
```

---

## E2E tests (Cypress)

All Cypress tests run in a real browser against the app on `localhost:4200`. Start the app first:

```bash
# Terminal 1
npm start

# Terminal 2
npm run cy:open     # interactive — pick a spec to run
npm run cy:run      # headless — run everything
```

### File naming

The suffix tells you what type of test it is:

```
login.cy.ts         ← E2E test — do all the flows work?
```

### Patterns used across all Cypress tests

**Page Object (PO)** — each tested page has a `.po.ts` file next to its test. The PO owns all `cy.get()` calls. Tests only describe behaviour, never touch selectors directly:

```ts
new LoginPo().visit().typeEmail('a@b.com').typePassword('secret').submit();
```

If a selector changes you fix it in one place — the PO — not across every test.

**`data-cy` attributes** — interactive elements in templates carry a `data-cy="..."` attribute used exclusively by tests. They are unaffected by styling, class, or text changes:

```html
<input data-cy="email-input" ... >
```
```ts
cy.get('[data-cy="email-input"]')
```

**Base PO** — `cypress/support/base.po.ts` provides shared assertions (`hasText`, `isVisible`, `urlIncludes`) that every PO inherits.

### 1. Regression tests (`*.cy.ts`)

**What:** Full end-to-end feature tests covering detailed user flows — form validation, navigation, error messages triggered by client-side logic. Run against the real app with no mocking.

**When to run:** Full regression run before releases or after significant changes.

**Run:**
```bash
npm run cy:run
```

**Example:**
```ts
it('shows required-field errors when submitted empty', () => {
  po.visit().submit();
  po.hasText('Email is required');
  po.hasText('Password is required');
});
```

---

## Adding a new test

### Regression test

1. If the page has no PO yet, create `feature.po.ts` extending `BasePo`
2. Add `data-cy` attributes to the template for any element the test needs to interact with
3. Create `feature.cy.ts` and write `describe` / `it` blocks using the PO

```ts
// feature/feature.po.ts
import { BasePo } from '../../../../support/base.po';

export class FeaturePo extends BasePo {
  visit() { cy.visit('/feature'); return this; }
  clickSave() { cy.get('[data-cy="save-btn"]').click(); return this; }
}

// feature/feature.cy.ts
describe('Feature page', () => {
  const po = new FeaturePo();
  beforeEach(() => po.visit());

  it('saves successfully', () => {
    po.clickSave();
    po.hasText('Saved');
  });
});
```

### Unit test

1. Create `feature.spec.ts` next to `feature.ts`
2. Use `TestBed` for Angular components or instantiate the class directly for services and utilities
3. Stub or spy on dependencies — do not use real HTTP or real services

```ts
// feature.spec.ts
import { Feature } from './feature';

describe('Feature', () => {
  it('does the thing', () => {
    const instance = new Feature();
    expect(instance.doThing()).toBe(true);
  });
});
```
