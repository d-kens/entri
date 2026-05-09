# Testing

Tests sit on a spectrum from fast and isolated to slow and realistic:

```
unit tests  →  integration tests (MSW)  →  E2E tests (real backend)
```

- **Unit** — one class, no browser, milliseconds
- **Integration (MSW)** — full UI stack, API mocked, no backend needed
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
login.smoke.cy.ts   ← smoke test — is the page alive?
login.cy.ts         ← E2E test   — do all the flows work?
login.msw.cy.ts     ← integration test — does the UI handle server responses correctly?
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

### Cypress commands

| Command | Purpose |
|---------|---------|
| `cy.startMsw(handlers)` | Register mock API responses before `cy.visit()` |
| `cy.resetMsw()` | Signal end of mock overrides — call in `afterEach` |

---

### 1. Smoke tests (`*.smoke.cy.ts`)

**What:** Fast, high-level checks that a page loads and its key elements are present. No logic, no API calls.

**When to run:** On every deployment as a quick sanity check before running the full suite.

**Run:**
```bash
npm run cy:smoke
```

**Example:**
```ts
it('loads and shows the login form', () => {
  po.visit();
  cy.get('[data-cy="email-input"]').should('be.visible');
  cy.get('[data-cy="login-submit"]').should('be.visible');
});
```

---

### 2. Regression tests (`*.cy.ts`)

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

### 3. Integration tests — MSW (`*.msw.cy.ts`)

**What:** Tests that mock the API layer to simulate server responses that are hard or impossible to reproduce against a real backend — 401 bad credentials, 500 server errors, 409 conflicts. Unlike E2E tests, these do not require a running backend.

**How they work:** `cy.startMsw([handler])` intercepts the HTTP request before it leaves the browser and returns the response defined in the handler. The app behaves as if the real server responded.

**When to run:** Alongside regression tests, or independently when working on error-state UI.

**Run:**
```bash
npm run cy:msw
```

#### Handler structure

Handlers live in `cypress/support/msw/handlers/` — one file per feature domain. Each named export is one specific server scenario:

```
cypress/support/msw/
  handler.ts                  ← MswHandler type definition
  handlers/
    auth.handlers.ts          ← login, register, refresh scenarios
    users.handlers.ts         ← user fetch scenarios (add as needed)
```

Each handler describes the method, URL, status code, and body to return:

```ts
// cypress/support/msw/handlers/auth.handlers.ts
export const loginUnauthorized: MswHandler = {
  method: 'POST',
  url: '**/auth/login',
  statusCode: 401,
  body: { message: 'Invalid email or password.' },
  alias: 'loginRequest',       // lets tests use cy.wait('@loginRequest')
};
```

The `alias` field is optional but recommended — it lets your test wait for the request to confirm it was actually made before asserting on the result.

#### Example test

Always use `cy.startMsw()` in `beforeEach` and `cy.resetMsw()` in `afterEach` to keep tests isolated from each other:

```ts
import { loginUnauthorized, loginServerError } from '../../../../support/msw/handlers/auth.handlers';

describe('Login page (mocked)', () => {
  const po = new LoginPo();

  describe('when the server returns 401', () => {
    beforeEach(() => {
      cy.startMsw([loginUnauthorized]);
      po.visit();
    });

    afterEach(() => {
      cy.resetMsw();
    });

    it('shows the error message returned by the server', () => {
      po.loginAs('wrong@example.com', 'wrongpassword');
      cy.wait('@loginRequest');
      po.hasText('Invalid email or password.');
    });
  });
});
```

---

## Adding a new test

### Smoke test

1. Create `*.smoke.cy.ts` next to the existing `.cy.ts` for that page
2. Visit the page and assert the critical elements are visible — nothing more

```ts
// feature/feature.smoke.cy.ts
import { FeaturePo } from './feature.po';

describe('Feature page (smoke)', () => {
  const po = new FeaturePo();

  it('loads and shows the page', () => {
    po.visit();
    cy.get('[data-cy="some-key-element"]').should('be.visible');
  });
});
```

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

### Integration test (MSW)

1. Add the handler to the relevant file in `cypress/support/msw/handlers/`
2. Create `feature.msw.cy.ts` next to the regression test
3. Call `cy.startMsw([yourHandler])` in `beforeEach` before `cy.visit()`
4. Call `cy.resetMsw()` in `afterEach`
5. Use `cy.wait('@alias')` to confirm the request was made before asserting

```ts
// cypress/support/msw/handlers/feature.handlers.ts
export const featureServerError: MswHandler = {
  method: 'POST',
  url: '**/feature',
  statusCode: 500,
  body: { message: 'Something went wrong.' },
  alias: 'featureRequest',
};

// feature/feature.msw.cy.ts
import { featureServerError } from '../../../../support/msw/handlers/feature.handlers';

describe('Feature page (mocked)', () => {
  const po = new FeaturePo();

  beforeEach(() => {
    cy.startMsw([featureServerError]);
    po.visit();
  });

  afterEach(() => {
    cy.resetMsw();
  });

  it('shows an error when the server fails', () => {
    po.clickSave();
    cy.wait('@featureRequest');
    po.hasText('Something went wrong.');
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
