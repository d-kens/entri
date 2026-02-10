import { inject, Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Zone {
  id: number;
  zoneName: string;
  city: string;
  isActive: boolean;
  isCbd: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Agent {
  id: number;
  name: string;
  zoneId: number;
  latitude: number;
  longitude: number;
  addressDescription: string;
  isActive: boolean;
  phone: string;
  openingTime: string;
  closingTime: string;
  createdAt: string;
  updatedAt: string;
}

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
