import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrganizerSummaryMetrics } from './models/analytics.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);

  getOrganizerSummaryMetrics(): Observable<OrganizerSummaryMetrics> {
    return this.http.get<OrganizerSummaryMetrics>(`${environment.apiBaseUrl}/analytics/summary`);
  }
}
