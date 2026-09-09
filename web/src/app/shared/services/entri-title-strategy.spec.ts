import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { provideRouter, Router, TitleStrategy } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { EntriTitleStrategy } from './entri-title-strategy';

describe('EntriTitleStrategy', () => {
  let titleMock: { setTitle: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    titleMock = { setTitle: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'events', title: 'Events', children: [] },
          { path: 'no-title', children: [] },
        ]),
        { provide: Title, useValue: titleMock },
        { provide: TitleStrategy, useExisting: EntriTitleStrategy },
      ],
    });
  });

  it('should be created', () => {
    const strategy = TestBed.inject(TitleStrategy);
    expect(strategy).toBeTruthy();
  });

  it('sets a suffixed title when the matched route defines one', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/events');

    expect(titleMock.setTitle).toHaveBeenCalledWith('Events | entri');
  });

  it('falls back to the default title when the matched route has none', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/no-title');

    expect(titleMock.setTitle).toHaveBeenCalledWith('entri - Sell tickets for any event');
  });

  it('directly builds the suffixed title from a router state snapshot', () => {
    const strategy = TestBed.inject(TitleStrategy) as EntriTitleStrategy;
    const router = TestBed.inject(Router);

    strategy.updateTitle(router.routerState.snapshot);

    expect(titleMock.setTitle).toHaveBeenCalledWith('entri - Sell tickets for any event');
  });
});
