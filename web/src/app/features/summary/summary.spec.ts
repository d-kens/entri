import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Summary } from './summary';
import { AnalyticsService } from './analytics-service';
import { WalletService } from '@features/wallet/wallet-service';
import { AuthService } from '@features/auth/auth-service';
import { OrganizerSummaryMetrics } from './models/analytics.models';

function buildMetrics(overrides: Partial<OrganizerSummaryMetrics> = {}): OrganizerSummaryMetrics {
  return {
    totalRevenue: 10000,
    totalTicketsSold: 50,
    upcomingEventsCount: 2,
    liveEventsCount: 1,
    ...overrides,
  };
}

describe('Summary', () => {
  let component: Summary;
  let fixture: ComponentFixture<Summary>;
  let analyticsService: { getOrganizerSummaryMetrics: ReturnType<typeof vi.fn> };
  let walletService: { getWallet: ReturnType<typeof vi.fn> };
  let authService: { getExternalId: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    analyticsService = { getOrganizerSummaryMetrics: vi.fn().mockReturnValue(of(buildMetrics())) };
    walletService = { getWallet: vi.fn().mockReturnValue(of({ externalId: 'w1', balance: 5000 })) };
    authService = { getExternalId: vi.fn().mockReturnValue('org-1') };
  });

  async function createComponent() {
    await TestBed.configureTestingModule({
      imports: [Summary],
      providers: [
        provideRouter([]),
        { provide: AnalyticsService, useValue: analyticsService },
        { provide: WalletService, useValue: walletService },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Summary);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('should create', async () => {
    await createComponent();
    expect(component).toBeTruthy();
  });

  it('should load organizer metrics on init', async () => {
    await createComponent();

    expect(analyticsService.getOrganizerSummaryMetrics).toHaveBeenCalled();
    expect(component.metrics()).toEqual(buildMetrics());
    expect(component.metricsLoading()).toBe(false);
    expect(component.metricsError()).toBe(false);
  });

  it('should mark metrics as errored when the request fails', async () => {
    analyticsService.getOrganizerSummaryMetrics.mockReturnValue(
      throwError(() => new Error('boom')),
    );
    vi.spyOn(console, 'error').mockImplementation(() => {});

    await createComponent();

    expect(component.metricsError()).toBe(true);
    expect(component.metricsLoading()).toBe(false);
  });

  it('should load the wallet balance for the current organizer', async () => {
    await createComponent();

    expect(walletService.getWallet).toHaveBeenCalledWith('org-1');
    expect(component.walletBalance()).toBe(5000);
    expect(component.walletLoading()).toBe(false);
  });

  it('should skip loading the wallet when there is no external id', async () => {
    authService.getExternalId.mockReturnValue(null);

    await createComponent();

    expect(walletService.getWallet).not.toHaveBeenCalled();
    expect(component.walletLoading()).toBe(true);
  });

  it('should stop the wallet spinner when the wallet request fails', async () => {
    walletService.getWallet.mockReturnValue(throwError(() => new Error('boom')));

    await createComponent();

    expect(component.walletLoading()).toBe(false);
    expect(component.walletBalance()).toBeNull();
  });
});
