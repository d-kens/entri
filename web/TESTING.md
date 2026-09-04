# Testing

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

## Adding a new test

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
