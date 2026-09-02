import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  OrganizerSummaryMetrics,
  PlatformSummaryMetrics,
  SalesTrendResponse,
} from './models/analytics.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);

  getOrganizerSummaryMetrics(): Observable<OrganizerSummaryMetrics> {
    return this.http.get<OrganizerSummaryMetrics>(`${environment.apiBaseUrl}/analytics/summary`);
  }

  getOrganizerSalesTrend(period = '30d'): Observable<SalesTrendResponse> {
    const params = new HttpParams().set('period', period);
    return this.http.get<SalesTrendResponse>(`${environment.apiBaseUrl}/analytics/sales-trend`, {
      params,
    });
  }

  getPlatformSummaryMetrics(): Observable<PlatformSummaryMetrics> {
    return this.http.get<PlatformSummaryMetrics>(
      `${environment.apiBaseUrl}/analytics/platform/summary`,
    );
  }

  getPlatformSalesTrend(period = '30d'): Observable<SalesTrendResponse> {
    const params = new HttpParams().set('period', period);
    return this.http.get<SalesTrendResponse>(
      `${environment.apiBaseUrl}/analytics/platform/sales-trend`,
      { params },
    );
  }
}
