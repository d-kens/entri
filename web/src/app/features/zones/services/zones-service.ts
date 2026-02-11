import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import {Agent, Zone} from '@features/zones/models/zones.model';


@Injectable({
  providedIn: 'root',
})
export class ZonesService {
  private http: HttpClient = inject(HttpClient);

  getAllZones(): Observable<Zone[]> {
    return this.http.get<Zone[]>(
      `${environment.apiBaseUrl}/zones`
    );
  }

  getAgentsByZone(
    zoneId: number
  ): Observable<Agent[]> {
    return this.http.get<Agent[]>(
      `${environment.apiBaseUrl}/zones/${zoneId}/agents`
    );
  }

  getAllAgents(): Observable<Agent[]> {
    return this.http.get<Agent[]>(
      `${environment.apiBaseUrl}/agents`
    );
  }
}
