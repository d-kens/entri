import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AnalyticsService } from './analytics-service';
import { OrganizerSummaryMetrics } from './models/analytics.models';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AnalyticsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET the organizer summary metrics', () => {
    const metrics: OrganizerSummaryMetrics = {
      totalRevenue: 10000,
      totalTicketsSold: 200,
      upcomingEventsCount: 3,
      liveEventsCount: 1,
    };
    let response: OrganizerSummaryMetrics | undefined;

    service.getOrganizerSummaryMetrics().subscribe((res) => (response = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/analytics/summary`);
    expect(req.request.method).toBe('GET');
    req.flush(metrics);

    expect(response).toEqual(metrics);
  });
});
